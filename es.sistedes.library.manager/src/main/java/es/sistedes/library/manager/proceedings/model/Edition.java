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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.sistedes.library.manager.HandleGenerator;

public class Edition extends AbstractProceedingsElement {

	volatile public static String EDITION_DEFAULT_FILENAME_PATTERN = "{acronym}-{year}-0-EDITION.json";
	
	protected String acronym;
	
	protected String conferenceName;

	protected String fullName;

	protected String shortName;

	protected int year;
	
	protected String date;
	
	protected String location;

	protected List<String> editors = new ArrayList<>();
	
	protected String preliminariesSistedesUuid;

	protected String preliminariesSistedesHandle;
	
	protected String preliminariesInternalHandle;
	
	protected String _tracksFilenamePattern = "{acronym}-{year}-0-TRACKS.json";
	
	protected String _preliminariesFilenamePattern = "{acronym}-{year}-1-{id}.json";
	
	protected String _preliminariesDocsFilenamePattern = "{acronym}-{year}-1-{id}.md";
	
	protected String _submissionsFilenamePattern = "{acronym}-{year}-2-{id}.json";
	
	protected String _submissionsDocsFilenamePattern = "{acronym}-{year}-2-{id}.pdf";

	/**
	 * @return the tracksFilenamePattern
	 */
	public String getTracksFilenamePattern() {
		return _tracksFilenamePattern;
	}

	/**
	 * @param tracksFilenamePattern the tracksFilenamePattern to set
	 */
	public void setTracksFilenamePattern(String tracksFilenamePattern) {
		this._tracksFilenamePattern = tracksFilenamePattern;
	}

	/**
	 * @return the preliminariesFilenamePattern
	 */
	public String getPreliminariesFilenamePattern() {
		return _preliminariesFilenamePattern;
	}

	/**
	 * @param preliminariesFilenamePattern the preliminariesFilenamePattern to set
	 */
	public void setPreliminariesFilenamePattern(String preliminariesFilenamePattern) {
		this._preliminariesFilenamePattern = preliminariesFilenamePattern;
	}

	/**
	 * @return the preliminariesDocsFilenamePattern
	 */
	public String getPreliminariesDocsFilenamePattern() {
		return _preliminariesDocsFilenamePattern;
	}

	/**
	 * @param preliminariesDocsFilenamePattern the preliminariesDocsFilenamePattern to set
	 */
	public void setPreliminariesDocsFilenamePattern(String preliminariesDocsFilenamePattern) {
		this._preliminariesDocsFilenamePattern = preliminariesDocsFilenamePattern;
	}

	/**
	 * @return the submissionsFilenamePattern
	 */
	public String getSubmissionsFilenamePattern() {
		return _submissionsFilenamePattern;
	}

	/**
	 * @param submissionsFilenamePattern the submissionsFilenamePattern to set
	 */
	public void setSubmissionsFilenamePattern(String submissionsFilenamePattern) {
		this._submissionsFilenamePattern = submissionsFilenamePattern;
	}

	/**
	 * @return the submissionsDocsFilenamePattern
	 */
	public String getSubmissionsDocsFilenamePattern() {
		return _submissionsDocsFilenamePattern;
	}

	/**
	 * @param submissionsDocsFilenamePattern the submissionsDocsFilenamePattern to set
	 */
	public void setSubmissionsDocsFilenamePattern(String submissionsDocsFilenamePattern) {
		this._submissionsDocsFilenamePattern = submissionsDocsFilenamePattern;
	}

	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}

	/**
	 * @param acronym the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	/**
	 * @return the conferenceName
	 */
	public String getConferenceName() {
		return conferenceName;
	}

	/**
	 * @param conferenceName the conferenceName to set
	 */
	public void setConferenceName(String conferenceName) {
		this.conferenceName = conferenceName;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return the name
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param name the name to set
	 */
	public void setShortName(String name) {
		this.shortName = name;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the editors
	 */
	public List<String> getEditors() {
		return editors;
	}

	/**
	 * @return the preliminariesSistedesUuid
	 */
	public String getPreliminariesSistedesUuid() {
		return preliminariesSistedesUuid;
	}

	/**
	 * @param preliminariesSistedesUuid the preliminariesSistedesUuid to set
	 */
	public void setPreliminariesSistedesUuid(String preliminariesSistedesUuid) {
		this.preliminariesSistedesUuid = preliminariesSistedesUuid;
	}

	/**
	 * @return the preliminariesSistedesHandle
	 */
	public String getPreliminariesSistedesHandle() {
		return preliminariesSistedesHandle;
	}

	/**
	 * @param preliminariesSistedesHandle the preliminariesSistedesHandle to set
	 */
	public void setPreliminariesSistedesHandle(String preliminariesSistedesHandle) {
		this.preliminariesSistedesHandle = preliminariesSistedesHandle;
	}

	/**
	 * @return the preliminariesInternalHandle
	 */
	public String getPreliminariesInternalHandle() {
		return preliminariesInternalHandle;
	}

	/**
	 * @param preliminariesInternalHandle the preliminariesInternalHandle to set
	 */
	public void setPreliminariesInternalHandle(String preliminariesInternalHandle) {
		this.preliminariesInternalHandle = preliminariesInternalHandle;
	}

	/**
	 * @return the full name of the proceedings of this edition
	 */
	@JsonIgnore
	public String getFullProceedingsName() {
		return MessageFormat.format("Actas de las {0}", fullName);
	}
	
	/**
	 * Generates a new {@link Edition} with the given <code>acronym</code> and for
	 * the given <code>year</code> that serves as a template to be later manually
	 * customized
	 * 
	 * @param acronym
	 * @param year
	 * @return
	 */
	public static Edition createTemplate(String acronym, int year) {
		Edition edition = new Edition();
		edition.setId(1);
		edition.setAcronym(acronym);
		edition.setConferenceName(MessageFormat.format("Jornadas de <CONFERENCE NAME> ({0,number,#})", year));
		edition.setShortName(MessageFormat.format("{0} {1,number,#}", acronym, year));
		edition.setShortName(MessageFormat.format("{0} {1,number,#}", acronym, year));
		edition.setFullName(MessageFormat.format("<EDITION NUMBER> <FULL CONFERENCE NAME> ({0} {1,number,#})", acronym, year));
		edition.setYear(year);
		edition.setDate(year + "-MM-DD");
		edition.setLocation("<LOCATION>");
		edition.setAbstract(MessageFormat.format("Las <EDITION NUMBER> <FULL CONFERENCE NAME> ({0} {1,number,#}) se han celebrado en <LOCATION> del <START> "
				+ "al <END> de <MONTH> de {1,number,#}, como parte de las Jornadas Sistedes.\n" +
				"El programa de {0} {1,number,#} se ha organizado en torno a sesiones temáticas o tracks.", acronym, year));
		edition.getEditors().add("Doe, J.");
		HandleGenerator.generateHandle(edition, acronym, year).ifPresent(edition::setSistedesHandle);
		return edition;
	}
}
