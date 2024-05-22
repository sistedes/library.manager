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

package es.sistedes.library.manager.proceedings.model;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

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
	 * {@link Map} with the authors' information, identified by person id.
	 * The same person may sign a submission with different Author information. 
	 */
	private Map<Integer, Author> authors = null;

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
	private ConferenceData() {
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
	protected ConferenceData(File editionFile, String prefix, String acronym, int year) throws IOException {
		this();
		this.edition = Edition.createTemplate(prefix, acronym, year);
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

	protected File getWorkingDir() {
		return editionFile.getParentFile();
	}

	public Edition getEdition() {
		return edition;
	}

	protected void setEdition(Edition edition) {
		this.edition = edition;
	}
	
	/**
	 * Return an unmodifiable view of the tracks map
	 * 
	 * @return
	 */
	public Map<Integer, Track> getTracks() {
		return Collections.unmodifiableMap(tracks);
	}
	
	protected void setTracks(Map<Integer, Track> tracks) {
		this.tracks.clear();
		this.tracks.putAll(tracks);
	}

	/**
	 * Return an unmodifiable view of the authors map
	 * 
	 * @return
	 */
	public Map<Integer, Author> getAuthors() {
		if (authors == null) {
			authors = buildAuthorsMap(submissions);
		}
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

	protected void setSubmissions(Map<Integer, Submission> submissions) {
		this.submissions.clear();
		this.submissions.putAll(submissions);
	}
	
	/**
	 * Return an unmodifiable view of the preliminaries
	 * 
	 * @return
	 */
	public List<Preliminaries> getPreliminaries() {
		return Collections.unmodifiableList(preliminaries);
	}

	protected void setPreliminaries(List<Preliminaries> preliminaries) {
		this.preliminaries.clear();
		this.preliminaries.addAll(preliminaries);
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

	private void doLoad() throws IOException {
		tracks.clear();
		submissions.clear();
		authors = null;
		getWorkingDir().mkdirs();

		for (File file : getWorkingDir().listFiles(
				(f, s) -> s.matches(edition.getSubmissionsFilenamePattern().replace("{acronym}", "\\w+").replace("{year}", "\\d+").replace("{id}", "\\d+")))) {
			Submission submission = mapper.readValue(file, Submission.class);
			submissions.put(submission.getId(), submission);
		}

		File tracksFile = new File(getWorkingDir(), edition.getTracksFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)));
		if (tracksFile.exists()) {
			TracksIndex tracksIndex = mapper.readValue(tracksFile, TracksIndex.class);
			tracksIndex.getTracks().forEach(t -> tracks.put(t.getId(), t));
			if (tracksIndex.getTracks().size() != tracks.size()) {
				throw new RuntimeException("Inconsistent number of Tracks in Tracks Index! Please check that Track identifiers are unique!");
			}
		}

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
		// Force load the authors map before saving, just in case it was not initialized yet
		getAuthors();
		String prefix = edition.getSistedesHandle().split("/")[0];
		saveMetadata(edition, force);
		saveMetadata(!tracks.isEmpty() ? TracksIndex.from(tracks.values()) : TracksIndex.from(Track.createTemplate(prefix, acronym, year)), force);
		if (preliminaries.isEmpty()) {
			saveMetadata(Preliminaries.createTemplate(prefix, acronym, year), true);
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
