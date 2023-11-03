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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.proceedings.model.AbstractProceedingsDocument;
import es.sistedes.library.manager.proceedings.model.Edition;

public class DSPublication extends DSItem {

	private static final Logger logger = LoggerFactory.getLogger(DSPublication.class);

	public String getPublicationType() {
		return metadata.getType();
	}

	public void setPublicationType(String type) {
		metadata.setType(type);
	}
	
	public String getHandle() {
		return handle;
	}

	public String getHandleUrl() {
		return "https://hdl.handle.net/" + handle;
	}

	public String getSistedesIdentifier() {
		return this.metadata.getSistedesIdentifier();
	}

	public String getSistedesUri() {
		return "https?://hdl.handle.net/" + this.metadata.getSistedesIdentifier();
	}

	public void setSistedesIdentifier(String id) {
		this.metadata.setSistedesIdentifier(id);
	}

	public String getTitle() {
		return this.metadata.getTitle();
	}

	public void setTitle(String title) {
		this.metadata.setTitle(title);
	}

	public String getDescription() {
		return this.metadata.getDescription();
	}

	public void setDescription(String html) {
		this.metadata.setDescription(html);
	}

	public String getProvenance() {
		return this.metadata.getProvenance();
	}

	public void setProvenance(String provenance) {
		this.metadata.setProvenance(provenance);
	}

	public String getAbstract() {
		return this.metadata.getAbstract();
	}

	public void setAbstract(String _abstract) {
		this.metadata.setAbstract(_abstract);
	}

	public List<String> getKeywords() {
		return Collections.unmodifiableList(this.metadata.getSubjects());
	}

	public void setKeywords(List<String> keywords) {
		this.metadata.setSubjects(keywords);
	}

	public String getIsPartOf() {
		return this.metadata.getIsPartOf();
	}

	public void setIsPartOf(String isPartOf) {
		this.metadata.setIsPartOf(isPartOf);
	}

	public String getLicense() {
		return this.metadata.getLicense();
	}

	public void setLicense(String license) {
		this.metadata.setLicense(license);
	}

	public String getIsFormatOf() {
		return this.metadata.getIsFormatOf();
	}

	public void setIsFormatOf(String isFormatOF) {
		this.metadata.setIsFormatOf(isFormatOF);
	}

	public String getRightsUri() {
		return this.metadata.getRightsUri();
	}

	public void setRightsUri(String uri) {
		this.metadata.setRightsUri(uri);
	}

	public Date getDate() {
		return this.metadata.getDate();
	}

	public void setDate(Date date) {
		this.metadata.setDate(date);
	}

	public String getPublisher() {
		return this.metadata.getPublisher();
	}

	public void setPublisher(String publisher) {
		this.metadata.setPublisher(publisher);
	}
	
	public String getSistedesEditionDate() {
		return this.metadata.getSistedesEditionDate();
	}
	
	public void setSistedesEditionDate(String date) {
		this.metadata.setSistedesEditionDate(date);
	}

	public List<String> getSistedesProceedingsEditor() {
		return Collections.unmodifiableList(this.metadata.getSistedesProceedingsEditor());
	}
	
	public void setSistedesProceedingsEditor(List<String> editors) {
		this.metadata.setSistedesProceedingsEditor(editors);
	}

	public String getSistedesProceedingsName() {
		return this.metadata.getSistedesProceedingsName();
	}
	
	public void setSistedesProceedingsName(String fullProceedingsName) {
		this.metadata.setSistedesProceedingsName(fullProceedingsName);
	}

	public String getSistedesEditionLocation() {
		return this.metadata.getSistedesEditionLocation();
	}
	
	public void setSistedesEditionLocation(String location) {
		this.metadata.setSistedesEditionLocation(location);
	}

	public String getSistedesEditionName() {
		return this.metadata.getSistedesEditionName();
	}
	
	public void setSistedesEditionName(String fullName) {
		this.metadata.setSistedesEditionName(fullName);
	}

	public String getSistedesConferenceAcronym() {
		return this.metadata.getSistedesConferenceAcronym();
	}
	
	public void setSistedesConferenceAcronym(String acronym) {
		this.metadata.setSistedesConferenceAcronym(acronym);
	}

	public String getSistedesConferenceName() {
		return this.metadata.getSistedesConferenceName();
	}
	
	public void setSistedesConferenceName(String conferenceName) {
		this.metadata.setSistedesConferenceName(conferenceName);
	}
	
	public List<String> getContributorsSignatures() {
		return this.metadata.getContributorsSignatures();
	}
	
	public void setContributorsSignatures(List<String> signatures) {
		this.metadata.setContributorsSignatures(signatures);
	}

	public List<String> getContributorsEmails() {
		return this.metadata.getContributorsEmails();
	}
	
	public void setContributorsEmails(List<String> emails) {
		this.metadata.setContributorsEmails(emails);
	}

	public List<String> getContributorsAffiliations() {
		return this.metadata.getContributorsAffiliations();
	}

	public void setContributorsAffiliations(List<String> affiliations) {
		this.metadata.setContributorsAffiliations(affiliations);
	}
	
	public static DSPublication createPublication(DSRoot dsRoot, DSCollection parent, Edition edition, AbstractProceedingsDocument document) {
		DSPublication result = new DSPublication();
		result.setTitle(StringUtils.normalizeSpace(document.getTitle()));
		result.setAbstract(StringUtils.normalizeSpace(document.getAbstract()));
		result.setKeywords(document.getKeywords());
		result.setIsPartOf(edition.getFullProceedingsName());
		result.setPublisher("Sistedes");
		result.setProvenance(("Automatically imported from an EasyChair dump using the Sistedes Library Manager (see https://github.com/sistedes/) on "
				+ ZonedDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneId.of("GMT"))).replace("[GMT]", " (GMT)"));
		result.setSistedesIdentifier(document.getSistedesHandle());
		result.setLicense(document.getLicense());
		result.setRightsUri(document.getRightsUri());
		result.setPublicationType(document.getType().getPublicationTypeName());
		result.setSistedesConferenceName(edition.getConferenceName());
		result.setSistedesConferenceAcronym(edition.getAcronym());
		result.setSistedesEditionName(edition.getFullName());
		result.setSistedesEditionDate(edition.getDate());
		result.setSistedesEditionLocation(edition.getLocation());
		result.setSistedesProceedingsName(edition.getFullProceedingsName());
		result.setSistedesProceedingsEditor(edition.getEditors());
		result.setContributorsSignatures(document.getSignatures().stream().map(s -> s.getFullName()).toList());
		if (document.getSignatures().stream().filter(s -> StringUtils.isNotEmpty(s.getEmail())).count() > 0) {
			result.setContributorsEmails(document.getSignatures().stream().map(s -> StringUtils.defaultIfEmpty(s.getEmail(), "")).toList());
		}
		if (document.getSignatures().stream().filter(s -> StringUtils.isNotEmpty(s.getAffiliation())).count() > 0) {
			result.setContributorsAffiliations(document.getSignatures().stream().map(s -> StringUtils.defaultIfEmpty(s.getFullAffiliation(), "")).toList());
		}		try {
			result.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(edition.getDate()));
		} catch (ParseException e) {
			logger.error("Unable to parse date: " + edition.getDate());
		}

		return dsRoot.getItemsEndpoint().createPublication(result, parent);
	}


}
