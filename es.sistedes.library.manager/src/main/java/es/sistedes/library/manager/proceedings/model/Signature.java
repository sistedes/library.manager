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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Signature {

	protected Integer author;

	protected String sistedesUuid;
	
	protected String givenName;

	protected String familyName;

	protected String email;
	
	protected String affiliation;

	protected String country;
	
	protected String web;
	
	protected SortedSet<Integer> submissions = new TreeSet<>();

	public Signature() {
	}
	
	/**
	 * @return the author
	 */
	public Integer getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(Integer author) {
		this.author = author;
	}

	/**
	 * @return the sistedesUuid
	 */
	public String getSistedesUuid() {
		return sistedesUuid;
	}

	/**
	 * @param sistedesUuid the sistedesUuid to set
	 */
	public void setSistedesUuid(String sistedesUuid) {
		this.sistedesUuid = sistedesUuid;
	}
	
	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @return the {@link Signature}'s full name (familyName, givenName )
	 */
	@JsonIgnore
	public String getFullName() {
		return familyName + ", " + givenName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the affiliation
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * @param affiliation the affiliation to set
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the full affiliation (i.e., including country)
	 */
	@JsonIgnore
	public String getFullAffiliation() {
		return StringUtils.defaultString(getAffiliation()) + ", " + StringUtils.defaultString(getCountry());
	}
	
	/**
	 * @return the web
	 */
	public String getWeb() {
		return web;
	}

	/**
	 * @param web the web to set
	 */
	public void setWeb(String web) {
		this.web = web;
	}

	/**
	 * @return the submissions
	 */
	public SortedSet<Integer> getSubmissions() {
		return submissions;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}, {1} <{2}> ({3}, {4})", familyName, givenName, email, affiliation, country);
	}
}
