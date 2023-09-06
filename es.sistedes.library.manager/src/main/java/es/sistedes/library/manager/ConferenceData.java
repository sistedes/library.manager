/*******************************************************************************
* Copyright (c) 2023 Sistedes
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
* Abel Gómez - initial API and implementation
*******************************************************************************/

package es.sistedes.library.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import es.sistedes.library.manager.excel.NoSuchSheetException;
import es.sistedes.library.manager.excel.SheetReader;
import es.sistedes.library.manager.proceedings.model.AbstractProceedingsDocument;
import es.sistedes.library.manager.proceedings.model.AbstractProceedingsElement;
import es.sistedes.library.manager.proceedings.model.Author;
import es.sistedes.library.manager.proceedings.model.Edition;
import es.sistedes.library.manager.proceedings.model.Preliminaries;
import es.sistedes.library.manager.proceedings.model.Signature;
import es.sistedes.library.manager.proceedings.model.Submission;
import es.sistedes.library.manager.proceedings.model.Submission.Type;
import es.sistedes.library.manager.proceedings.model.Track;
import es.sistedes.library.manager.proceedings.model.Track.TracksIndex;

public class ConferenceData {

	private static final Logger logger = LoggerFactory.getLogger(ConferenceData.class);

	/**
	 * {@link Edition} data of the conference
	 */
	private Edition edition;

	/**
	 * {@link Map} with the tracks of the conference, identified by track acronym
	 */
	private Map<Integer, Track> tracks = new TreeMap<>();

	/**
	 * {@link Map} with the submissions of the conference, identified by submission
	 * number
	 */
	private Map<Integer, Submission> submissions = new TreeMap<>();

	/**
	 * {@link List} with the preliminaries of the conference
	 */
	private List<Preliminaries> preliminaries = new ArrayList<>();

	/**
	 * {@link ListValuedMap} with the authors' information, identified by person id.
	 * The same person may sign a submission with different Author information. This
	 * is because authors may appear more than once in the 'Authors' sheet, since
	 * there will be as many occurrences as submissions they participate in. Also,
	 * authors' information may vary from submission to submission, since they may
	 * have introduced a different signature name or affiliation in each.
	 */
	private Map<Integer, Author> authors = new TreeMap<>();

	/**
	 * Acronym of the conference this {@link ConferenceData} represents
	 */
	private String acronym;

	/**
	 * Year of the edition this {@link ConferenceData} represents
	 */
	private int year;

	/**
	 * Edition file from where the {@link ConferenceData} will be loaded. Additional
	 * conference files (Tracks, Preliminaries, Submissions, etc.) will be saved in
	 * the same directory.
	 */
	private File editionFile;

	/**
	 * Default {@link JsonMapper} used to serialize the conference info to the
	 * intermediate files
	 */
	private JsonMapper mapper;

	/**
	 * Private default constructor
	 */
	public ConferenceData() {
		mapper = JsonMapper.builder().build();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
	}

	/**
	 * Load the conference data from a non-existing edition {@link File}. Used only
	 * internally when importing from an EasyChair dump.
	 * 
	 * @param editionFile
	 * @param acronym
	 * @param year
	 * @throws IOException
	 */
	private ConferenceData(File editionFile, String acronym, int year) throws IOException {
		this();
		this.edition = Edition.createTemplate(acronym, year);
		this.acronym = edition.getAcronym();
		this.year = edition.getYear();
		this.editionFile = editionFile;
		doLoad();
	}

	/**
	 * Load the conference data from an existing edition {@link File}
	 * 
	 * @param editionFile
	 * @throws IOException
	 */
	public ConferenceData(File editionFile) throws IOException {
		this();
		this.edition = mapper.readValue(editionFile, Edition.class);
		this.acronym = edition.getAcronym();
		this.year = edition.getYear();
		this.editionFile = editionFile;
		doLoad();
	}

	private File getWorkingDir() {
		return editionFile.getParentFile();
	}

	public Edition getEdition() {
		return edition;
	}

	/**
	 * Return an unmodifiable view of the tracks map
	 * 
	 * @return
	 */
	public Map<Integer, Track> getTracks() {
		return Collections.unmodifiableMap(tracks);
	}

	/**
	 * Return an unmodifiable view of the authors map
	 * 
	 * @return
	 */
	public Map<Integer, Author> getAuthors() {
		return Collections.unmodifiableMap(authors);
	}

	/**
	 * Return an unmodifiable view of the submissions map
	 * 
	 * @return
	 */
	public Map<Integer, Submission> getSubmissions() {
		return Collections.unmodifiableMap(submissions);
	}

	/**
	 * Return an unmodifiable view of the preliminaries
	 * 
	 * @return
	 */
	public List<Preliminaries> getPreliminaries() {
		return Collections.unmodifiableList(preliminaries);
	}

	/**
	 * Returns an unmodifiable view of all the elements in the proceedings
	 * 
	 * @return
	 */
	public List<AbstractProceedingsElement> getAllProceedingsElements() {
		List<AbstractProceedingsElement> elements = new ArrayList<>();
		elements.add(getEdition());
		elements.addAll(getPreliminaries());
		elements.addAll(getTracks().values());
		elements.addAll(getSubmissions().values());
		return Collections.unmodifiableList(elements);
	}

	/**
	 * Import the conference data from the {@link InitializeCommand#xslxFile} with
	 * the EasyChair dump
	 * 
	 * @param xslxFile
	 * @param sourceDir
	 * @param pattern
	 * @param force
	 * @throws IOException
	 */
	public static ConferenceData createFrom(File xslxFile, File inputDir, File outputDir, String acronym, int year, String pattern, boolean force)
			throws IOException {
		File editionFile = new File(outputDir, Edition.EDITION_DEFAULT_FILENAME_PATTERN.replace("{acronym}", acronym).replace("{year}", String.valueOf(year)));
		ConferenceData conferenceData = new ConferenceData(editionFile, acronym, year);
		try (Workbook workbook = new XSSFWorkbook(new FileInputStream(xslxFile))) {
			conferenceData.tracks.clear();
			conferenceData.submissions.clear();
			conferenceData.authors.clear();

			// Create a dummy edition
			conferenceData.edition = Edition.createTemplate(acronym, year);

			// Tracks are optional, it depends on the conference whether they exist or not,
			// but if they exist, they **must** be read before the Submissions are processed
			Optional.ofNullable(workbook.getSheet("Tracks")).ifPresent(s -> conferenceData.tracks.putAll(readTracks(s)));
			conferenceData.tracks.values().forEach(track -> HandleGenerator.generateHandle(track, acronym, year).ifPresent(track::setSistedesHandle));

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
			conferenceData.submissions.putAll(readSubmissions(workbook.getSheet("Submissions"), submissionsSignatures, conferenceData.tracks));
			conferenceData.submissions.values().stream().forEach(submission -> {
				HandleGenerator.generateHandle(submission, acronym, year).ifPresent(submission::setSistedesHandle);
				conferenceData.importSubmissionFile(submission, inputDir, pattern, force);
			});

			// Now that all data has been imported, we can recreate the authors map from the
			// submissions information
			conferenceData.authors.putAll(buildAuthorsMap(conferenceData.submissions));
		}
		return conferenceData;
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
		String filename = StringUtils.replaceEachRepeatedly(pattern, new String[] { "{acronym}", "{year}", "{id}" },
				new String[] { acronym, String.valueOf(year), String.valueOf(submission.getId()) });
		File sourceDoc = sourceDir.toPath().resolve(filename).toFile();
		File targetDoc = new File(getWorkingDir(), getDataFilename(submission).orElseThrow());
		if (!targetDoc.exists() || force) {
			copyData(submission, sourceDoc, targetDoc);
		} else {
			logger.warn(MessageFormat.format("File ''{0}'' already exists, skipping...", targetDoc));
		}
	}

	private void doLoad() throws IOException {
		tracks.clear();
		submissions.clear();
		authors.clear();
		getWorkingDir().mkdirs();

		for (File file : getWorkingDir().listFiles(
				(f, s) -> s.matches(edition.getSubmissionsFilenamePattern().replace("{acronym}", "\\w+").replace("{year}", "\\d+").replace("{id}", "\\d+")))) {
			Submission submission = mapper.readValue(file, Submission.class);
			submissions.put(submission.getId(), submission);
		}

		File tracksFile = new File(getWorkingDir(), edition.getTracksFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)));
		if (tracksFile.exists()) {
			tracks.putAll(mapper.readValue(tracksFile, TracksIndex.class).getTracks());
		}

		authors.putAll(buildAuthorsMap(submissions));

		for (File file : getWorkingDir().listFiles((f, s) -> s
				.matches(edition.getPreliminariesFilenamePattern().replace("{acronym}", "\\w+").replace("{year}", "\\d+").replace("{id}", "\\d+")))) {
			Preliminaries preliminarie = mapper.readValue(file, Preliminaries.class);
			preliminaries.add(preliminarie);
		}
	}

	/**
	 * Save the conference data to disk. In forced mode, existing files of
	 * submissions are overwritten. Preliminaries are never overwritten.
	 * 
	 * @param force
	 */
	public void save(boolean force) {
		saveMetadata(edition, force);
		saveMetadata(!tracks.isEmpty() ? TracksIndex.from(tracks) : TracksIndex.from(Track.createTemplate(acronym, year)), force);
		if (preliminaries.isEmpty()) {
			saveMetadata(Preliminaries.createTemplate(acronym, year), true);
		} else {
			preliminaries.stream().forEach(elt -> {
				saveMetadata(elt, force);
			});
		}
		submissions.values().stream().forEach(elt -> {
			saveMetadata(elt, force);
		});
	}

	/**
	 * Save the metadata files in JSON into the {@link ConferenceData#workingDir}
	 * 
	 * @param elt
	 * @param force
	 */
	private void saveMetadata(AbstractProceedingsElement elt, boolean force) {
		File file = new File(getWorkingDir(), getMetadataFilename(elt).orElseThrow());
		if (!file.exists() || force) {
			saveObject(elt, file);
		} else {
			logger.warn(MessageFormat.format("File ''{0}'' already exists, skipping...", file));
		}

	}

	/**
	 * Saves the given {@link Object} as a JSON document into the given {@link File}
	 * 
	 * @param obj
	 * @param file
	 */
	private void saveObject(Object obj, File file) {
		File target = file;
		try {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				if (target.exists()) {
					target = new File(FilenameUtils.removeExtension(file.getPath()) + ".bak" + i + "." + FilenameUtils.getExtension(file.getName()));
				} else {
					break;
				}
			}
			if (file.exists()) {
				file.renameTo(target);
				logger.warn(MessageFormat.format("File ''{0}'' exists, creating a backup on ''{1}''", file, target));
			}
			mapper.writeValue(file, obj);
		} catch (IOException e) {
			logger.error(MessageFormat.format("Unable to write ''{0}''! ({1})", file, e.getLocalizedMessage()));
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
	 * Returns a suitable Json filename for the given
	 * {@link AbstractProceedingsElement}
	 * 
	 * @param elt
	 * @return
	 */
	private Optional<String> getMetadataFilename(AbstractProceedingsElement elt) {
		String rawName = null;
		if (elt instanceof Edition ed) {
			rawName = editionFile.getName();
		} else if (elt instanceof TracksIndex traIdx) {
			rawName = edition.getTracksFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)).replace("{id}",
					String.valueOf(traIdx.getId()));
		} else if (elt instanceof Preliminaries pre) {
			rawName = edition.getPreliminariesFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)).replace("{id}",
					String.valueOf(pre.getId()));
		} else if (elt instanceof Submission sub) {
			rawName = edition.getSubmissionsFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)).replace("{id}",
					String.valueOf(sub.getId()));
		}
		return Optional.of(StringUtils.stripAccents(rawName));
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
			rawName = edition.getPreliminariesDocsFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)).replace("{id}",
					String.valueOf(pre.getId()));
		} else if (doc instanceof Submission sub) {
			rawName = edition.getSubmissionsDocsFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)).replace("{id}",
					String.valueOf(sub.getId()));
		}
		return Optional.of(StringUtils.stripAccents(rawName));
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
			rowReader.get("#", Double.class).map(Double::intValue).ifPresent(track::setId);
			rowReader.get("name", String.class).map(StringUtils::stripAccents).ifPresent(track::setAcronym);
			rowReader.get("long name", String.class).ifPresent(track::setName);
			tracks.put(track.getId(), track);
		});
		return tracks;
	}

	/**
	 * Import tracks information from the 'Authors' {@link Sheet}
	 * 
	 * @param sheet
	 * @return
	 */
	private static ListValuedMap<Integer, Signature> readAuthors(Sheet sheet) {
		ListValuedMap<Integer, Signature> submissionsSignatures = new ArrayListValuedHashMap<>();
		new SheetReader(sheet).forEach(rowReader -> {
			Signature signature = new Signature();
			rowReader.get("first name", String.class).ifPresent(signature::setGivenName);
			rowReader.get("last name", String.class).ifPresent(signature::setFamilyName);
			rowReader.get("email", String.class).ifPresent(signature::setEmail);
			rowReader.get("country", String.class).ifPresent(signature::setCountry);
			rowReader.get("affiliation", String.class).ifPresent(signature::setAffiliation);
			rowReader.get("Web page", String.class).ifPresent(signature::setWeb);
			rowReader.get("submission #", Double.class).map(Double::intValue).ifPresent(id -> submissionsSignatures.put(id, signature));
			rowReader.get("person #", Double.class).map(Double::intValue).ifPresent(id -> signature.setAuthor(id));

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
			rowReader.get("decision", String.class).ifPresent(decision -> {
				if (decision.equals("accept")) {
					rowReader.get("#", Double.class).map(Double::intValue).ifPresent(submission::setId);
					rowReader.get("track #", Double.class).map(Double::intValue).ifPresent(track -> tracks.get(track).getSubmissions().add(submission.getId()));
					rowReader.get("title", String.class).ifPresent(submission::setTitle);
					rowReader.get("keywords", String.class).map(Submission::extractKeywordsList)
							.ifPresent(list -> list.stream().forEach(kw -> submission.getKeywords().add(kw)));
					// FIXME: IMPORTANT! The field type specifying the type of submission will
					// probably change over the years. Thus, these lines and
					// "es.sistedes.library.manager.proceedings.model.Submission.Type"
					// may need to be adapted in the future.
					// JISBD
					rowReader.get("form fields", String.class).map(Submission::extractFormFields).map(map -> map.get("tipo")).map(Type::from)
							.ifPresent(submission::setType);
					// PROLE
					rowReader.get("form fields", String.class).map(Submission::extractFormFields).map(map -> map.get("Categoría")).map(Type::from)
							.ifPresent(submission::setType);
					// JCIS
					rowReader.get("form fields", String.class).map(Submission::extractFormFields).map(map -> map.get("Category.")).map(Type::from)
							.ifPresent(submission::setType);

					rowReader.get("abstract", String.class).ifPresent(submission::setAbstract);
					submission.getSignatures().addAll(submissionsSignatures.get(submission.getId()));
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
		});
		return submissions;
	}

	/**
	 * Builds an {@link Author}s {@link Map} out of the information of the given
	 * {@link Submission}s {@link Map}
	 * 
	 * @param submissions
	 * @return
	 */
	private static Map<Integer, Author> buildAuthorsMap(Map<Integer, Submission> submissions) {
		Map<Integer, Author> authors = new TreeMap<>();
		// First, populate the list of authors with their signatures...
		submissions.values().stream().forEach(submission -> {
			submission.getSignatures().forEach(signature -> {
				// @formatter:off
				Integer id = signature.getAuthor();
				if (authors.containsKey(id)) { 
					authors.get(id).getSignatures().add(signature);
				} else authors.put(id, new Author() {{ 
					setId(signature.getAuthor()); 
					getSignatures().add(signature);
				}});
				// @formatter:on
			});
		});
		// Second, add the corresponding submissions to all Author Signatures
		// for an easy navigation from submission to submission
		submissions.values().stream().forEach(submission -> {
			submission.getSignatures().forEach(signature -> {
				Integer id = signature.getAuthor();
				authors.get(id).getSignatures().stream().forEach(s -> s.getSubmissions().add(submission.getId()));
			});
		});
		return authors;
	}
}
