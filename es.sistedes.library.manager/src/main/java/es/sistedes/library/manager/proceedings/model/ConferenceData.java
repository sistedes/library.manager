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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class ConferenceData {

	/**
	 * {@link Edition} data of the conference
	 */
	private Edition edition;

	/**
	 * {@link Track}s of the conference, stored in a {@link TracksIndex}
	 */
	private TracksIndex tracksIndex;

	/**
	 * {@link Map} with the submissions of the conference, identified by submission
	 * number
	 */
	private Map<Integer, Submission> submissions = new TreeMap<>();

	/**
	 * {@link Map} with the preliminaries of the conference
	 */
	private Map<Integer, Preliminaries> preliminaries = new TreeMap<>();

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
		this.edition = Edition.createTemplate(editionFile, prefix, acronym, year);
		this.acronym = edition.getAcronym();
		this.year = edition.getYear();
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
		this.edition = Edition.load(editionFile);
		this.acronym = edition.getAcronym();
		this.year = edition.getYear();
		doLoad();
	}

	protected File getWorkingDir() {
		return edition.getFile().getParentFile();
	}

	public Edition getEdition() {
		return edition;
	}

	protected void setEdition(Edition edition) {
		this.edition = edition;
	}
	
	/**
	 * Return an unmodifiable view of the tracks 
	 * 
	 * @return
	 */
	public Map<Integer, Track> getTracks() {
		if (tracksIndex == null) {
			tracksIndex = TracksIndex.create(edition.getTracksFile(), Collections.emptyList());
		}
		return tracksIndex;
	}
	
	protected void setTracks(Collection<Track> tracks) {
		this.tracksIndex = TracksIndex.create(edition.getTracksFile(), tracks);

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

	protected void setSubmissions(Collection<Submission> submissions) {
		this.submissions.clear();
		submissions.forEach(submission -> this.submissions.put(submission.getId(), submission));
		authors = buildAuthorsMap(this.submissions);
	}
	
	/**
	 * Return an unmodifiable view of the preliminaries map
	 * 
	 * @return
	 */
	public Map<Integer, Preliminaries> getPreliminaries() {
		return Collections.unmodifiableMap(preliminaries);
	}

	protected void setPreliminaries(Collection<Preliminaries> preliminaries) {
		this.preliminaries.clear();
		preliminaries.forEach(prelim -> this.preliminaries.put(prelim.getId(), prelim));
	}
	
	/**
	 * Returns an unmodifiable view of all the elements in the proceedings
	 * 
	 * @return
	 */
	public List<AbstractProceedingsElement> getAllProceedingsElements() {
		List<AbstractProceedingsElement> elements = new ArrayList<>();
		elements.add(getEdition());
		elements.addAll(getPreliminaries().values());
		elements.addAll(getTracks().values());
		elements.addAll(getSubmissions().values());
		return Collections.unmodifiableList(elements);
	}

	private void doLoad() throws IOException {
		submissions.clear();
		preliminaries.clear();
		getWorkingDir().mkdirs();

		File tracksFile = new File(getWorkingDir(), edition.getTracksFilenamePattern().replace("{acronym}", acronym).replace("{year}", String.valueOf(year)));
		if (tracksFile.exists()) {
			tracksIndex = TracksIndex.load(tracksFile);
		}
		
		for (File file : getWorkingDir().listFiles(
				(f, s) -> s.matches(edition.getSubmissionsFilenamePattern().replace("{acronym}", "\\w+").replace("{year}", "\\d+").replace("{id}", "\\d+")))) {
			Submission submission = Submission.load(file);
			submissions.put(submission.getId(), submission);
		}
		authors = buildAuthorsMap(submissions);

		for (File file : getWorkingDir().listFiles((f, s) -> s
				.matches(edition.getPreliminariesFilenamePattern().replace("{acronym}", "\\w+").replace("{year}", "\\d+").replace("{id}", "\\d+")))) {
			Preliminaries preliminarie = Preliminaries.load(file);
			preliminaries.put(preliminarie.getId(), preliminarie);
		}
	}

	/**
	 * Save the conference data to disk. 
	 */
	public synchronized void save() {
		String prefix = edition.getSistedesHandle().split("/")[0];
		edition.save();
		tracksIndex.save();
		if (preliminaries.isEmpty()) {
			setPreliminaries(Arrays.asList(Preliminaries.createTemplate(edition.getPreliminariesFile(1), prefix, acronym, year)));
		}
		preliminaries.values().stream().forEach(elt -> elt.save());
		submissions.values().stream().forEach(elt -> elt.save());
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
				Author author;
				if (authors.containsKey(id)) { 
					author = authors.get(id);
					author.getSignatures().add(signature);
				} else {
					author = new Author() {{ 
						setId(signature.getAuthor()); 
						getSignatures().add(signature);
					}};
					authors.put(id, author);
				}
				author.getSubmissions().add(submission);
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
