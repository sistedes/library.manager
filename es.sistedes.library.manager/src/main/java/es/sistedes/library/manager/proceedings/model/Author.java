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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Author {

	private static final Logger logger = LoggerFactory.getLogger(Author.class);
	
	protected Integer id;
	
	protected String orcid;

	protected Set<Signature> signatures = new HashSet<>();
	
	protected Set<Submission> submissions = new HashSet<>();

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the orcid
	 */
	public String getOrcid() {
		Set<String> orcids = this.signatures.stream().map(s -> s.getOrcid()).filter(orcid -> orcid != null).collect(Collectors.toSet());
		if (orcids.size() == 0) {
			return null;
		} if (orcids.size() == 1) {
			return orcids.stream().findAny().get();
		} else {
			throw new RuntimeException("Authors information is inconsistent! More than one ORCID has been associated to author with id " + id);
		}
	}

	/**
	 * @param orcid the orcid to set
	 */
	public void setOrcid(String orcid) {
		this.signatures.forEach(s -> s.setOrcid(orcid));
	}	
	
	/**
	 * @return the sistedesUuid
	 */
	public String getSistedesUuid() {
		Set<String> uuids = this.signatures.stream().map(s -> s.getSistedesUuid()).filter(uuid -> uuid != null).collect(Collectors.toSet());
		if (uuids.size() == 0) {
			return null;
		} if (uuids.size() == 1) {
			return uuids.stream().findAny().get();
		} else {
			throw new RuntimeException("Authors information is inconsistent! More than one Sistedesd UUID has been associated to author with id " + id);
		}
	}

	/**
	 * @param sistedesUuid the sistedesUuid to set
	 */
	public void setSistedesUuid(String sistedesUuid) {
		this.signatures.forEach(s -> s.setSistedesUuid(sistedesUuid));
	}

	/**
	 * @return the signatures
	 */
	public Set<Signature> getSignatures() {
		return signatures;
	}

	/**
	 * @return the submissions where the {@link Author} appears
	 */
	public Set<Submission> getSubmissions() {
		return submissions;
	}

	@Override
	public String toString() {
		return signatures.stream().map(s -> s.toString()).collect(Collectors.joining("; "));
	}
	
	public void save() {
		getSubmissions().forEach(submission -> {
			submission.save();
			logger.debug(MessageFormat.format("Submission ''{0}'' of author ''{1}'' saved", submission.getId(), getId()));
		});
	}
}
