package es.sistedes.library.manager.proceedings.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.HandleGenerator;
import es.sistedes.library.manager.IConferenceDataImporter;
import es.sistedes.library.manager.excel.NoSuchSheetException;
import es.sistedes.library.manager.excel.SheetReader;

public class EasyChairImporter implements IConferenceDataImporter {

	private static final Logger logger = LoggerFactory.getLogger(EasyChairImporter.class);

	/**
	 * The imported {@link ConferenceData}
	 */
	private ConferenceData conferenceData;

	/**
	 * Create new {@link EasyChairImporter} to import the conference data from the
	 * {@link InitializeCommand#xslxFile} with the EasyChair dump
	 * 
	 * @param xslxFile
	 * @param sourceDir
	 * @param pattern
	 * @param force
	 * @throws IOException
	 */
	public EasyChairImporter(File xslxFile, File inputDir, File outputDir, String prefix, String acronym, int year, String pattern, boolean force) throws IOException {
		File editionFile = new File(outputDir, Edition.EDITION_DEFAULT_FILENAME_PATTERN.replace("{acronym}", acronym).replace("{year}", String.valueOf(year)));
		conferenceData = new ConferenceData(editionFile, prefix, acronym, year);
		try (Workbook workbook = new XSSFWorkbook(new FileInputStream(xslxFile))) {
			// Create a dummy edition
			conferenceData.setEdition(Edition.createTemplate(prefix, acronym, year));

			// Tracks are optional, it depends on the conference whether they exist or not,
			// but if they exist, they **must** be read before the Submissions are processed
			Optional.ofNullable(workbook.getSheet("Tracks")).ifPresent(s -> conferenceData.setTracks(readTracks(s)));
			conferenceData.getTracks().values().forEach(track -> HandleGenerator.generateHandle(track, prefix, acronym, year).ifPresent(track::setSistedesHandle));

			// Now, try to read Authors and Submission, which should always exist
			if (workbook.getSheet("Authors") == null)
				throw new NoSuchSheetException("Authors");
			if (workbook.getSheet("Submissions") == null)
				throw new NoSuchSheetException("Submissions");

			// Read authors into a temporary Map, since the mapping between Submissions and
			// Authors is specified in the Authors tab
			ListValuedMap<Integer, Signature> submissionsSignatures = readAuthors(workbook.getSheet("Authors"));

			// Now read the submissions, and pass the tracks and authoring information so
			// that submissions can be completely defined
			conferenceData.setSubmissions(readSubmissions(workbook.getSheet("Submissions"), submissionsSignatures, conferenceData.getTracks()));
			conferenceData.getSubmissions().values().stream().forEach(submission -> {
				HandleGenerator.generateHandle(submission, prefix, acronym, year).ifPresent(submission::setSistedesHandle);
				importSubmissionFile(submission, inputDir, pattern, force);
			});
		}
	}

	/**
	 * Import tracks information from the 'Tracks' {@link Sheet}
	 * 
	 * @param sheet
	 * @return
	 */
	private static Map<Integer, Track> readTracks(Sheet sheet) {
		Map<Integer, Track> tracks = new HashMap<>();
		new SheetReader(sheet).forEach(rowReader -> {
			Track track = new Track();
			try {
				logger.debug("Reading '#'");
				rowReader.get("#", Double.class).map(Double::intValue).ifPresent(track::setId);
				logger.debug("Reading 'name'");
				rowReader.get("name", String.class).map(StringUtils::stripAccents).ifPresent(track::setAcronym);
				logger.debug("Reading 'long name'");
				rowReader.get("long name", String.class).ifPresent(track::setName);
				tracks.put(track.getId(), track);
			} catch (Exception e) {
				logger.error(MessageFormat.format("Error reading Track # ''{0}''", track.getId()));
			}
		});
		return tracks;
	}

	/**
	 * Import authors information from the 'Authors' {@link Sheet}
	 * 
	 * @param sheet
	 * @return
	 */
	private static ListValuedMap<Integer, Signature> readAuthors(Sheet sheet) {
		ListValuedMap<Integer, Signature> submissionsSignatures = new ArrayListValuedHashMap<>();
		new SheetReader(sheet).forEach(rowReader -> {
			Signature signature = new Signature();
			try {
				logger.debug("Reading 'person #'");
				rowReader.get("person #", Double.class).map(Double::intValue).ifPresent(id -> signature.setAuthor(id));
				logger.debug("Reading 'first name'");
				rowReader.get("first name", String.class).map(StringUtils::normalizeSpace).ifPresent(signature::setGivenName);
				logger.debug("Reading 'last name'");
				rowReader.get("last name", String.class).map(StringUtils::normalizeSpace).ifPresent(signature::setFamilyName);
				logger.debug("Reading 'email'");
				rowReader.get("email", String.class).map(StringUtils::normalizeSpace).ifPresent(signature::setEmail);
				logger.debug("Reading 'country'");
				rowReader.get("country", String.class).map(StringUtils::normalizeSpace).ifPresent(signature::setCountry);
				logger.debug("Reading 'affiliation'");
				rowReader.get("affiliation", String.class).map(StringUtils::normalizeSpace).ifPresent(signature::setAffiliation);
				logger.debug("Reading 'submission #'");
				rowReader.get("submission #", Double.class).map(Double::intValue).ifPresent(id -> submissionsSignatures.put(id, signature));
				logger.debug("Reading 'Web page'");
				rowReader.get("Web page", String.class).map(StringUtils::normalizeSpace).ifPresent(wp -> {
					if (wp.contains("orcid.org")) {
						Matcher matcher = Pattern.compile("https?://orcid\\.org/(?<orcid>[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4})").matcher(wp);
						if (matcher.matches()) {
							signature.setOrcid(matcher.group("orcid"));
						}
					} else {
						signature.setWeb(wp);
					}
				});
			} catch (Exception e) {
				logger.error(MessageFormat.format("Error reading Author # ''{0}''", signature.getAuthor()));
			}
		});
		return submissionsSignatures;
	}

	/**
	 * Reads the submissions information from the 'Submissions' {@link Sheet}
	 * 
	 * @param sheet
	 * @return
	 */
	private static Map<Integer, Submission> readSubmissions(Sheet sheet, ListValuedMap<Integer, Signature> submissionsSignatures, Map<Integer, Track> tracks) {
		Map<Integer, Submission> submissions = new HashMap<>();
		new SheetReader(sheet).forEach(rowReader -> {
			Submission submission = new Submission();
			try {
				rowReader.get("decision", String.class).ifPresent(decision -> {
					if (decision.contains("accept")) { // we also include "conditionally accept"
						logger.debug("Reading '#'");
						rowReader.get("#", Double.class).map(Double::intValue).ifPresent(submission::setId);
						logger.debug("Reading 'track #'");
						rowReader.get("track #", Double.class).map(Double::intValue).ifPresent(track -> tracks.get(track).getSubmissions().add(submission.getId()));
						logger.debug("Reading 'title'");
						rowReader.get("title", String.class).map(StringUtils::normalizeSpace).ifPresent(submission::setTitle);
						logger.debug("Reading 'keywords'");
						rowReader.get("keywords", String.class).map(Submission::extractKeywordsList)
								.ifPresent(list -> list.stream().forEach(kw -> submission.getKeywords().add(kw)));
						logger.debug("Reading 'form fields'");
						rowReader.get("form fields", String.class).map(Submission::extractFormFields).ifPresent(submission::setFormFields);
						logger.debug("Reading 'abstract'");
						rowReader.get("abstract", String.class).ifPresent(submission::setAbstract);
						submission.getSignatures().addAll(submissionsSignatures.get(submission.getId()));
						logger.debug("Reading 'authors'");
						rowReader.get("authors", String.class).ifPresent(str -> {
							// Ensure that the authors signature are in the right order
							// To ease the comparison, we can assume that the only ' and ' will be the
							// separator between the last and the next-to-last signatures
							if (!str.replaceAll(" and ", ", ").equals(
									submission.getSignatures().stream().map(s -> s.getGivenName() + " " + s.getFamilyName()).collect(Collectors.joining(", ")))) {
								logger.warn(MessageFormat.format(
										"Authors in 'Authors' sheet are not in the same order than in the 'Submissions' sheet for submission #{0}",
										submission.getId()));
							}
						});
						submissions.put(submission.getId(), submission);
					}
				});
			} catch (Exception e) {
				logger.error(MessageFormat.format("Error reading Submission # ''{0}''", submission.getId()));
			}
		});
		return submissions;
	}

	/**
	 * Import the actual submission file (i.e., typically, the PDF document of the
	 * paper) into the {@link ConferenceData#workingDir}. In order to detect the
	 * submission file, the given <code>pattern</code> is used.
	 * 
	 * @param submission
	 * @param sourceDir
	 * @param pattern
	 * @param force
	 */
	private void importSubmissionFile(Submission submission, File sourceDir, String pattern, boolean force) {
		String filename = getSourceSubmissionFileName(submission, conferenceData.getEdition().getAcronym(), conferenceData.getEdition().getYear(), pattern);
		File sourceDoc = sourceDir.toPath().resolve(filename).toFile();
		File targetDoc = new File(conferenceData.getWorkingDir(), getDataFilename(submission).orElseThrow());
		if (!targetDoc.exists() || force) {
			copyData(submission, sourceDoc, targetDoc);
		} else {
			logger.warn(MessageFormat.format("File ''{0}'' already exists, skipping...", targetDoc));
		}
	}

	/**
	 * Copy the data files (i.e., the documents, typically PDFs) from the
	 * <code>sourceDoc</code> to the <code>targetDoc</code>, updating the
	 * corresponding filename in the given {@link AbstractProceedingsElement}
	 * 
	 * @param elt
	 * @param sourceDoc
	 * @param targetDoc
	 */
	private void copyData(AbstractProceedingsDocument elt, File sourceDoc, File targetDoc) {
		boolean existed = targetDoc.exists();
		try {
			FileUtils.copyFile(sourceDoc, targetDoc);
			elt.setFilename(targetDoc.getName());
			if (existed) {
				logger.warn(MessageFormat.format("File ''{0}'' overwritten", targetDoc));
			} else {
				logger.info(MessageFormat.format("File ''{0}'' copied from ''{1}''", targetDoc, sourceDoc));
			}
		} catch (IOException e) {
			logger.error(MessageFormat.format("Unable to copy file from ''{0}'' to ''{1}'' ({2})", sourceDoc, targetDoc, e.getLocalizedMessage()));
		}
	}

	/**
	 * Returns a suitable filename for the document associated to the given
	 * {@link AbstractProceedingsDocument}
	 * 
	 * @param doc
	 * @return
	 */
	private Optional<String> getDataFilename(AbstractProceedingsDocument doc) {
		String rawName = null;
		if (doc instanceof Preliminaries pre) {
			rawName = conferenceData.getEdition().getPreliminariesDocsFilenamePattern().replace("{acronym}", conferenceData.getEdition().getAcronym())
					.replace("{year}", String.valueOf(conferenceData.getEdition().getYear())).replace("{id}", String.valueOf(pre.getId()));
		} else if (doc instanceof Submission sub) {
			rawName = conferenceData.getEdition().getSubmissionsDocsFilenamePattern().replace("{acronym}", conferenceData.getEdition().getAcronym())
					.replace("{year}", String.valueOf(conferenceData.getEdition().getYear())).replace("{id}", String.valueOf(sub.getId()));
		}
		return Optional.of(StringUtils.stripAccents(rawName));
	}

	/**
	 * Returns the imported {@link ConferenceData}
	 * 
	 * @return
	 */
	@Override
	public ConferenceData getData() {
		return conferenceData;
	}

	/**
	 * Returns the original file name of the {@link Submission}
	 * 
	 * @param submission
	 * @param acronym
	 * @param year
	 * @param pattern
	 * @return the original file name of the {@link Submission}
	 */
	public static String getSourceSubmissionFileName(Submission submission, String acronym, int year, String pattern) {
		return StringUtils.replaceEachRepeatedly(pattern, new String[] { "{acronym}", "{year}", "{id}" },
				new String[] { acronym, String.valueOf(year), String.valueOf(submission.getId()) });
	}
}
