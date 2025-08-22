package es.sistedes.library.manager.proceedings.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.http.codec.xml.Jaxb2XmlEncoder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

import es.sistedes.library.manager.IConferenceDataImporter;
import es.sistedes.library.manager.proceedings.model.Submission.Type;

public class PdfImporter implements IConferenceDataImporter {

	private static final Logger logger = LoggerFactory.getLogger(PdfImporter.class);

	private ConferenceData conferenceData;
	
	private URL grobidUrl;
	private String grobidUser;
	private String grobidPass;

	@Override
	public ConferenceData getData() {
		return conferenceData;
	}

	// @formatter:off
	public PdfImporter(
			File pdfFile,
			File outputDir,
			String prefix,
			String acronym,
			int year,
			List<Range<Integer>> frontmatter,
			List<List<Range<Integer>>> contributions,
			List<Range<Integer>> backmatter,
			URL url) throws IOException {
		
		this.conferenceData = createConferenceData(outputDir, prefix, acronym, year);
		this.grobidUrl = new URL(url.getProtocol(), url.getHost(), url.getFile());
		if (url.getUserInfo() != null && url.getUserInfo().contains(":")) {
			this.grobidUser = url.getUserInfo().split(":")[0];
			this.grobidPass = url.getUserInfo().split(":")[1];
		}
		initializeEdition();
		inititalizeFrontmatter(pdfFile, frontmatter);
		initializeContents(pdfFile, contributions);
		consolidateMetadata();
	}
	// @formatter:on

	private ConferenceData createConferenceData(File outputDir, String prefix, String acronym, int year) throws IOException {
		File editionFile = new File(outputDir, Edition.EDITION_DEFAULT_FILENAME_PATTERN.replace("{acronym}", acronym).replace("{year}", String.valueOf(year)));
		return new ConferenceData(editionFile, prefix, acronym, year);
	}
	
	private void initializeEdition() {
		Edition edition = conferenceData.getEdition();
		edition.setPreliminariesDocsFilenamePattern(edition.getPreliminariesDocsFilenamePattern().replace(".md", ".pdf"));
	}

	private void inititalizeFrontmatter(File pdfFile, List<Range<Integer>> frontmatter) throws IOException {
		this.conferenceData.setPreliminaries(createPreliminaries(pdfFile, frontmatter));
	}

	private void initializeContents(File pdfFile, List<List<Range<Integer>>> pages) throws IOException {
		Edition edition = conferenceData.getEdition();
		List<Track> tracks = new ArrayList<>();
		List<Submission> submissions = new ArrayList<>();
		AtomicInteger submissionId = new AtomicInteger();
		for (int i = 0; i < pages.size(); i++) {
			Track track = Track.createTemplate(edition.getSistedesHandle().split("/")[0], edition.getAcronym(), edition.getYear());
			Integer trackId = i + 1;
			track.setId(trackId);
			track.setAcronym("TRACK" + trackId);
			track.setName("Categoría " + trackId);
			logger.info(MessageFormat.format("Starting new Section ''{0}''", track.getName()));
			List<Submission> trackSubmissions = createSubmissions(pdfFile, pages.get(i));
			trackSubmissions.forEach(s -> {
				s.setId(submissionId.addAndGet(1));
				String oldFilename = s.getFilename();
				String newFilename = edition.getSubmissionsDocsFilenamePattern().replace("{acronym}", edition.getAcronym())
						.replace("{year}", String.valueOf(edition.getYear())).replace("{id}", submissionId.toString());
				new File(conferenceData.getWorkingDir(), oldFilename).renameTo(new File(conferenceData.getWorkingDir(), newFilename));
				s.setFilename(newFilename);
			});
			track.getSubmissions().addAll(trackSubmissions.stream().map(s -> s.getId()).toList());
			tracks.add(track);
			trackSubmissions.forEach(s -> submissions.add(s));
		}
		this.conferenceData.setTracks(tracks);
		this.conferenceData.setSubmissions(submissions);
	}
	
	private void consolidateMetadata() {
		conferenceData.getSubmissions().values().forEach(submission -> {
			logger.info(MessageFormat.format("Updating metadata for submission {0}...", submission.getId()));
			TeiMetadata teiMetadata = getMetadata(grobidUrl, grobidUser, grobidPass, new File(conferenceData.getWorkingDir(), submission.getFilename()));
			teiMetadata.getTitle().ifPresentOrElse(
				submission::setTitle, 
				() -> logger.error(MessageFormat.format("Unable to get a title for submission {0}...", submission.getId()))
			); 
			teiMetadata.getAbstract().ifPresentOrElse(
				submission::setAbstract, 
				() -> logger.error(MessageFormat.format("Unable to get an abstract for submission {0}...", submission.getId()))
			);
			teiMetadata.getSignatures().forEach(submission.getSignatures()::add);
		});
	}
	
	private List<Preliminaries> createPreliminaries(File pdfFile, List<Range<Integer>> pages) throws IOException {
		List<Preliminaries> preliminaries = new ArrayList<>();
		Edition edition = conferenceData.getEdition();
		PDDocument document = PDDocument.load(pdfFile);
		for (int i = 0; i < pages.size(); i++) {
			int id = i + 1;
			Preliminaries prelim = Preliminaries.createTemplate(edition.getPreliminariesFile(id), 
					edition.getSistedesHandle().split("/")[0], edition.getAcronym(), edition.getYear());
			prelim.setId(id);
			prelim.setFilename(edition.getPreliminariesDocsFilenamePattern().replace("{acronym}", edition.getAcronym())
					.replace("{year}", String.valueOf(edition.getYear())).replace("{id}", String.valueOf(id)));
			Integer start = pages.get(i).getMinimum();
			Integer end = pages.get(i).getMaximum();
			Splitter splitter = createSplitter(start, end);
			File prelimFile = edition.getPreliminariesFile(id);
			logger.info(MessageFormat.format("Saving pages {0}-{1} to file ''{2}''", start, end, prelimFile));
			splitter.split(document).get(0).save(prelimFile);
			preliminaries.add(prelim);
		}
		return preliminaries;
	}
	
	private List<Submission> createSubmissions(File pdfFile, List<Range<Integer>> pages) throws IOException {
		List<Submission> submissions = new ArrayList<>();
		Edition edition = conferenceData.getEdition();
		PDDocument document = PDDocument.load(pdfFile);
		for (int i = 0; i < pages.size(); i++) {
			Integer start = pages.get(i).getMinimum();
			Integer end = pages.get(i).getMaximum();
			Integer submissionId = Integer.valueOf(start.toString() + end.toString());
			Submission submission = new Submission(edition.getSubmissionFile(submissionId));
			submission.setId(submissionId);
			submission.setFilename(edition.getSubmissionsDocsFilenamePattern().replace("{acronym}", edition.getAcronym())
					.replace("{year}", String.valueOf(edition.getYear())).replace("{id}", submissionId.toString()));
			if (end - start <= 1) {
				submission.setType(Type.ABSTRACT);
			} else {
				submission.setType(Type.PAPER);
			}
			Splitter splitter = createSplitter(start, end);
			File submissionFile = edition.getSubmissionFile(submissionId);
			logger.info(MessageFormat.format("Saving pages {0}-{1} to file ''{2}''", start, end, submissionFile));
			splitter.split(document).get(0).save(submissionFile);
			submissions.add(submission);
		}
		return submissions;
	}

	private static Splitter createSplitter(Integer start, Integer end) {
		Splitter splitter = new Splitter();
		splitter.setStartPage(start);
		splitter.setEndPage(end);
		splitter.setSplitAtPage(end - start + 1);
		return splitter;
	}
	
	private static TeiMetadata getMetadata(URL url, String user, String pass, File file) {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("input", new FileSystemResource(file));
		
		// @formatter:off
		try {
			return new TeiMetadata(WebClient.builder()
						.baseUrl(url.toString() + "/processFulltextDocument")
						.exchangeStrategies(ExchangeStrategies.builder().codecs((configurer) -> {
				            configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder());
				            configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder());
				        }).build())
						.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(1024 * 1024))
					.build()
					.post()
					.headers(headers -> { if (user != null && pass != null) headers.setBasicAuth(user, pass); })
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.accept(MediaType.APPLICATION_XML)
					.body(BodyInserters.fromMultipartData(builder.build()))
					.retrieve()
					.bodyToMono(String.class)
					.block());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.error("Unable to parse TEI metadata for " + file);
		}
		return new TeiMetadata();
		// @formatter:on
	}
	
	protected static class TeiMetadata {
		
		private Document teiDocument;
		
		private TeiMetadata() {
		}
		
		public TeiMetadata(String document) throws SAXException, IOException, ParserConfigurationException {
			this.teiDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(document.getBytes()));
		}
		
		public Optional<String> getTitle() {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				String expression = "//teiHeader/fileDesc/titleStmt/title/text()";
				String title = (String) xPath.compile(expression).evaluate(teiDocument, XPathConstants.STRING);
				return Optional.ofNullable(StringUtils.defaultIfBlank(title, null));
			} catch (XPathExpressionException e) {
			}
			return Optional.empty();
		}

		public Optional<String> getAbstract() {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				String expression = "//teiHeader/profileDesc/abstract";
				Node abstractHtml = (Node) xPath.compile(expression).evaluate(teiDocument, XPathConstants.NODE);
				String abstractMd = FlexmarkHtmlConverter.builder().build().convert(abstractHtml.getTextContent());
				return Optional.ofNullable(StringUtils.defaultIfBlank(abstractMd, null));
			} catch (XPathExpressionException e) {
			}
			return Optional.empty();
		}
		
		public List<Signature> getSignatures() {
			List<Signature> signatures = new ArrayList<>();
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				String expression = "//teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author";
				NodeList authorNodes = (NodeList) xPath.compile(expression).evaluate(teiDocument, XPathConstants.NODESET);
				for (int i = 0; i < authorNodes.getLength(); i++) {
					Node node = authorNodes.item(i);
					Signature signature = new Signature();
					signatures.add(signature);
					signature.setGivenName(StringUtils.defaultIfBlank(
							(String) xPath.compile("./persName/forename/text()").evaluate(node, XPathConstants.STRING), null));
					signature.setFamilyName(StringUtils.defaultIfBlank(
							(String) xPath.compile("./persName/surname/text()").evaluate(node, XPathConstants.STRING), null));
					signature.setAffiliation(StringUtils.defaultIfBlank(
							(String) xPath.compile("./affiliation/orgName[@type='institution']/text()").evaluate(node, XPathConstants.STRING), null));
					signature.setCountry(StringUtils.defaultIfBlank(
							(String) xPath.compile("./affiliation/address/country/text()").evaluate(node, XPathConstants.STRING), null));
					signature.setEmail(StringUtils.defaultIfBlank(
							(String) xPath.compile("./email/text()").evaluate(node, XPathConstants.STRING), null));
				}
			} catch (XPathExpressionException e) {
			}
			return signatures;
		}
	}
}
