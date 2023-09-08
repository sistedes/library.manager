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

package es.sistedes.library.manager.dspace.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {
	
	public static DateFormat DATE_FORMAT_SIMPLE_W_HOUR = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static DateFormat DATE_FORMAT_SIMPLE = new SimpleDateFormat("yyyy-MM-dd");
//	public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00'Z'");
	
	@JsonProperty("dc.title")
	protected List<MetadataEntry> titles = new ArrayList<>();

	@JsonProperty("dc.identifier.sistedes")
	protected List<MetadataEntry> sistedesIdentifiers = new ArrayList<>();

	@JsonProperty("dc.description")
	protected List<MetadataEntry> descriptions = new ArrayList<>();
	
	@JsonProperty("dc.description.abstract")
	protected List<MetadataEntry> abstracts = new ArrayList<>();
	
	@JsonProperty("dc.description.provenance")
	protected List<MetadataEntry> descriptionsProvenance = new ArrayList<>();
	
	@JsonProperty("dc.description.tableofcontents")
	protected List<MetadataEntry> tocs = new ArrayList<>();

	@JsonProperty("dc.relation.ispartof")
	protected List<MetadataEntry> isPartOf = new ArrayList<>();

	@JsonProperty("dc.subject")
	protected List<MetadataEntry> subjects = new ArrayList<>();
	
	@JsonProperty("dc.rights")
	protected List<MetadataEntry> rights = new ArrayList<>();
	
	@JsonProperty("dc.rights.license")
	protected List<MetadataEntry> licenses = new ArrayList<>();

	@JsonProperty("dc.rights.uri")
	protected List<MetadataEntry> rightsUris = new ArrayList<>();
	
	@JsonProperty("dc.date.accessioned")
	protected List<MetadataEntry> datesAccessioned = new ArrayList<>();
	
	@JsonProperty("dc.date.available")
	protected List<MetadataEntry> datesAvailable = new ArrayList<>();
	
	@JsonProperty("dc.date.issued")
	protected List<MetadataEntry> datesIssued = new ArrayList<>();
	
	@JsonProperty("dc.publisher")
	protected List<MetadataEntry> publishers = new ArrayList<>();
	
	@JsonProperty("dc.relation.isformatof")
	protected List<MetadataEntry> isFormatOf = new ArrayList<>();

	@JsonProperty("dspace.entity.type")
	protected List<MetadataEntry> type = new ArrayList<>();
	
	@JsonProperty("person.givenName")
	protected List<MetadataEntry> personGivenNames = new ArrayList<>();
	
	@JsonProperty("person.familyName")
	protected List<MetadataEntry> personFamilyNames = new ArrayList<>();
	
	@JsonProperty("person.name.variant")
	protected List<MetadataEntry> personNameVariants = new ArrayList<>();
	
	@JsonProperty("person.affiliation.name")
	protected List<MetadataEntry> personAffiliations = new ArrayList<>();

	@JsonProperty("person.email")
	protected List<MetadataEntry> personEmails = new ArrayList<>();
	
	@JsonProperty("person.web")
	protected List<MetadataEntry> personWebs = new ArrayList<>();
	
	@JsonProperty("dc.contributor.signature")
	protected List<MetadataEntry> contributorsSignatures = new ArrayList<>();
	
	@JsonProperty("dc.contributor.email")
	protected List<MetadataEntry> contributorsEmails = new ArrayList<>();
	
	@JsonProperty("dc.contributor.affiliation")
	protected List<MetadataEntry> contributorsAffiliations = new ArrayList<>();

	@JsonProperty("dc.contributor.bio")
	protected List<MetadataEntry> contributorsBios = new ArrayList<>();
	
	@JsonProperty("bs.conference.name")
	protected List<MetadataEntry> bsConferenceNames = new ArrayList<>();

	@JsonProperty("bs.conference.acronym")
	protected List<MetadataEntry> bsConferenceAcronyms = new ArrayList<>();
	
	@JsonProperty("bs.edition.name")
	protected List<MetadataEntry> bsEditionNames = new ArrayList<>();

	@JsonProperty("bs.edition.date")
	protected List<MetadataEntry> bsEditionDates = new ArrayList<>();

	@JsonProperty("bs.edition.location")
	protected List<MetadataEntry> bsEditionLocations = new ArrayList<>();

	@JsonProperty("bs.proceedings.editor")
	protected List<MetadataEntry> bsProceedingsEditors = new ArrayList<>();

	@JsonProperty("bs.proceedings.name")
	protected List<MetadataEntry> bsProceedingsNames = new ArrayList<>();
	
	
	public String getTitle() {
		return titles.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setTitle(String title) {
		if (StringUtils.isBlank(title)) return;
		this.titles.clear();
		this.titles.add(new MetadataEntry(title));
	}

	public String getSistedesIdentifier() {
		return sistedesIdentifiers.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setSistedesIdentifier(String id) {
		if (StringUtils.isBlank(id)) return;
		this.sistedesIdentifiers.clear();
		this.sistedesIdentifiers.add(new MetadataEntry(id));
	}

	public String getDescription() {
		return descriptions.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setDescription(String description) {
		if (StringUtils.isBlank(description)) return;
		this.descriptions.clear();
		this.descriptions.add(new MetadataEntry(description));
	}

	public String getProvenance() {
		return descriptionsProvenance.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setProvenance(String provenance) {
		if (StringUtils.isBlank(provenance)) return;
		this.descriptionsProvenance.clear();
		this.descriptionsProvenance.add(new MetadataEntry(provenance));
	}
	
	
	public String getAbstract() {
		return abstracts.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setAbstract(String abs) {
		if (StringUtils.isBlank(abs)) return;
		this.abstracts.clear();
		this.abstracts.add(new MetadataEntry(abs));
	}
	
	public String getToc() {
		return tocs.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setToc(String toc) {
		if (StringUtils.isBlank(toc)) return;
		this.tocs.clear();
		this.tocs.add(new MetadataEntry(toc));
	}

	public String getIsPartOf() {
		return isPartOf.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setIsPartOf(String isPartOf) {
		if (StringUtils.isBlank(isPartOf)) return;
		this.isPartOf.clear();
		this.isPartOf.add(new MetadataEntry(isPartOf));
	}
	
	public List<String> getSubjects() {
		return subjects.stream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}

	@JsonIgnore
	public void setSubjects(List<String> subjects) {
		if (subjects == null) return;
		this.subjects.clear();
		for (int i = 0; i < subjects.size(); i++) {
			this.subjects.add(new MetadataEntry(subjects.get(i), i + 1));
		}
	}

	public String getRights() {
		return rights.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setRights(String rights) {
		if (StringUtils.isBlank(rights)) return;
		this.rights.clear();
		this.rights.add(new MetadataEntry(rights));
	}
	
	public String getLicense() {
		return licenses.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setLicense(String license) {
		if (StringUtils.isBlank(license)) return;
		this.licenses.clear();
		this.licenses.add(new MetadataEntry(license));
	}
	
	public String getPublisher() {
		return publishers.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setPublisher(String publisher) {
		if (StringUtils.isBlank(publisher)) return;
		this.publishers.clear();
		this.publishers.add(new MetadataEntry(publisher));
	}
	
	public String getRightsUri() {
		return rightsUris.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setRightsUri(String rightsUri) {
		if (StringUtils.isBlank(rightsUri)) return;
		this.rightsUris.clear();
		this.rightsUris.add(new MetadataEntry(rightsUri));
	}
	
	public Date getDate() {
		return datesIssued.stream().findFirst().map(d ->  {
			try {
				return DATE_FORMAT_SIMPLE.parse(d.getValue());
			} catch (ParseException e1) {
				try {
					return DATE_FORMAT_SIMPLE_W_HOUR.parse(d.getValue());
				} catch (ParseException e2) {
					throw new RuntimeException(e2);
				}
			}
		}).orElse(null);
	}

	@SuppressWarnings("deprecation")
	@JsonIgnore
	public void setDate(Date date) {
		if (date == null) return;
		this.datesIssued.clear();
		if (date.getHours() == 0 && date.getMinutes() == 0) {
			this.datesIssued.add(new MetadataEntry(DATE_FORMAT_SIMPLE.format(date)));
		} else {
			this.datesIssued.add(new MetadataEntry(DATE_FORMAT_SIMPLE_W_HOUR.format(date)));
		}
	}

	public String getIsFormatOf() {
		return isFormatOf.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setIsFormatOf(String isFormatOf) {
		if (StringUtils.isBlank(isFormatOf)) return;
		this.isFormatOf.clear();
		this.isFormatOf.add(new MetadataEntry(isFormatOf));
	}
	
	public String getType() {
		return type.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}

	@JsonIgnore
	public void setType(String type) {
		if (StringUtils.isBlank(type)) return;
		this.type.clear();
		this.type.add(new MetadataEntry(type));
	}

	public String getPersonGivenName() {
		return personGivenNames.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setPersonGivenName(String givenName) {
		if (StringUtils.isBlank(givenName)) return;
		this.personGivenNames.clear();
		this.personGivenNames.add(new MetadataEntry(givenName));
	}
	
	public String getPersonFamilyName() {
		return personFamilyNames.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setPersonFamilyName(String familyName) {
		if (StringUtils.isBlank(familyName)) return;
		this.personFamilyNames.clear();
		this.personFamilyNames.add(new MetadataEntry(familyName));
	}
	
	public List<String> getPersonNameVariants() {
		return personNameVariants.stream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	
	public void addPersonNameVariant(String nameVariant) {
		if (StringUtils.isBlank(nameVariant)) return;
		this.personNameVariants.add(new MetadataEntry(nameVariant));
	}
	
	@JsonIgnore
	public void setPersonNameVariants(List<String> nameVariants) {
		if (nameVariants == null) return;
		this.personNameVariants.clear();
		for (int i = 0; i < nameVariants.size(); i++) {
			this.personNameVariants.add(new MetadataEntry(nameVariants.get(i), i + 1));
		}
	}

	public List<String> getPersonAffiliations() {
		return personAffiliations.stream().map(a ->  a.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	public void addPersonAffiliation(String affiliation) {
		if (StringUtils.isBlank(affiliation)) return;
		this.personAffiliations.add(new MetadataEntry(affiliation));
	}
	
	@JsonIgnore
	public void setPersonAffiliations(List<String> affiliations) {
		if (this.personAffiliations == null) return;
		this.personAffiliations.clear();
		for (int i = 0; i < affiliations.size(); i++) {
			this.personAffiliations.add(new MetadataEntry(affiliations.get(i), i + 1));
		}
	}
	
	public List<String> getPersonEmails() {
		return personEmails.stream().map(e ->  e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	public void addPersonEmail(String email) {
		if (StringUtils.isBlank(email)) return;
		this.personEmails.add(new MetadataEntry(email));
	}
	
	@JsonIgnore
	public void setPersonEmails(List<String> emails) {
		if (this.personEmails == null) return;
		this.personEmails.clear();
		for (int i = 0; i < emails.size(); i++) {
			this.personEmails.add(new MetadataEntry(emails.get(i), i + 1));
		}
	}
	
	public List<String> getPersonWebs() {
		return personWebs.stream().map(w ->  w.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	public void addPersonWeb(String web) {
		if (StringUtils.isBlank(web)) return;
		this.personWebs.add(new MetadataEntry(web));
	}
	
	@JsonIgnore
	public void setPersonWebs(List<String> webs) {
		if (this.personWebs == null) return;
		this.personWebs.clear();
		for (int i = 0; i < webs.size(); i++) {
			this.personWebs.add(new MetadataEntry(webs.get(i), i + 1));
		}
	}
	
	public List<String> getContributorsSignatures() {
		return contributorsSignatures.stream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	@JsonIgnore
	public void setContributorsSignatures(List<String> authors) {
		if (authors == null) return;
		this.contributorsSignatures.clear();
		for (int i = 0; i < authors.size(); i++) {
			this.contributorsSignatures.add(new MetadataEntry(authors.get(i), i + 1));
		}
	}
	
	public List<String> getContributorsEmails() {
		return contributorsEmails.stream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	@JsonIgnore
	public void setContributorsEmails(List<String> emails) {
		if (emails == null) return;
		this.contributorsEmails.clear();
		for (int i = 0; i < emails.size(); i++) {
			this.contributorsEmails.add(new MetadataEntry(emails.get(i), i + 1));
		}
	}
	
	public List<String> getContributorsAffiliations() {
		return contributorsAffiliations.stream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	@JsonIgnore
	public void setContributorsAffiliations(List<String> affiliations) {
		if (affiliations == null) return;
		this.contributorsAffiliations.clear();
		for (int i = 0; i < affiliations.size(); i++) {
			this.contributorsAffiliations.add(new MetadataEntry(affiliations.get(i), i + 1));
		}
	}
	
	public String getContributorBio() {
		return contributorsBios.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setContributorBio(String bio) {
		if (StringUtils.isBlank(bio)) return;
		this.contributorsBios.clear();
		this.contributorsBios.add(new MetadataEntry(bio));
	}
	
	public String getSistedesConferenceName() {
		return bsConferenceNames.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setSistedesConferenceName(String conferenceName) {
		if (StringUtils.isBlank(conferenceName)) return;
		this.bsConferenceNames.clear();
		this.bsConferenceNames.add(new MetadataEntry(conferenceName));
	}
	
	public String getSistedesConferenceAcronym() {
		return bsConferenceAcronyms.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setSistedesConferenceAcronym(String conferenceAcronym) {
		if (StringUtils.isBlank(conferenceAcronym)) return;
		this.bsConferenceAcronyms.clear();
		this.bsConferenceAcronyms.add(new MetadataEntry(conferenceAcronym));
	}
	
	public String getSistedesEditionName() {
		return bsEditionNames.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setSistedesEditionName(String editionName) {
		if (StringUtils.isBlank(editionName)) return;
		this.bsEditionNames.clear();
		this.bsEditionNames.add(new MetadataEntry(editionName));
	}

	public String getSistedesEditionDate() {
		return bsEditionDates.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setSistedesEditionDate(String editionDate) {
		if (StringUtils.isBlank(editionDate)) return;
		this.bsEditionDates.clear();
		this.bsEditionDates.add(new MetadataEntry(editionDate));
	}
	
	public String getSistedesEditionLocation() {
		return bsEditionLocations.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setSistedesEditionLocation(String editionLocation) {
		if (StringUtils.isBlank(editionLocation)) return;
		this.bsEditionLocations.clear();
		this.bsEditionLocations.add(new MetadataEntry(editionLocation));
	}
	
	public List<String> getSistedesProceedingsEditor() {
		return bsProceedingsEditors.stream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	@JsonIgnore
	public void setSistedesProceedingsEditor(List<String> proceedingsEditors) {
		if (proceedingsEditors == null) return;
		this.bsProceedingsEditors.clear();
		for (int i = 0; i < proceedingsEditors.size(); i++) {
			this.bsProceedingsEditors.add(new MetadataEntry(proceedingsEditors.get(i), i + 1));
		}
	}
	
	public String getSistedesProceedingsName() {
		return bsProceedingsNames.stream().findFirst().map(e ->  e.getValue()).orElse(null);
	}
	
	@JsonIgnore
	public void setSistedesProceedingsName(String ProceedingsName) {
		if (StringUtils.isBlank(ProceedingsName)) return;
		this.bsProceedingsNames.clear();
		this.bsProceedingsNames.add(new MetadataEntry(ProceedingsName));
	}
}
