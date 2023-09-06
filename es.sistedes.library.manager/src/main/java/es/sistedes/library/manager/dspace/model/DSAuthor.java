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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import es.sistedes.library.manager.proceedings.model.Author;
import es.sistedes.library.manager.proceedings.model.Signature;

public class DSAuthor extends DSItem {

	public DSAuthor() {
		metadata.setType(Type.AUTHOR.getName());
	}

	public String getGivenName() {
		return metadata.getPersonGivenName();
	}

	public void setGivenName(String name) {
		metadata.setPersonGivenName(name);
	}

	public String getFamilyName() {
		return metadata.getPersonFamilyName();
	}

	public void setFamilyName(String familyName) {
		metadata.setPersonFamilyName(familyName);
	}

	public String getFullName() {
		return StringUtils.defaultString(getFamilyName()) + ", " + StringUtils.defaultString(getGivenName());
	}

	public List<String> getEmails() {
		return metadata.getPersonEmails().stream().map(e -> e.toLowerCase()).collect(Collectors.toList());
	}

	public void addEmail(String email) {
		metadata.addPersonEmail(email);
	}

	public void setEmails(List<String> emails) {
		metadata.setPersonEmails(emails);
	}

	public List<String> getAffiliations() {
		return metadata.getPersonAffiliations();
	}

	public void addAffiliation(String affiliation) {
		metadata.addPersonAffiliation(affiliation);
	}

	public void setAffiliations(List<String> affiliations) {
		metadata.setPersonAffiliations(affiliations);
	}

	public List<String> getNameVariants() {
		return metadata.getPersonNameVariants();
	}

	public void addNameVariant(String nameVariant) {
		metadata.addPersonNameVariant(nameVariant);
	}

	public void setNameVariants(List<String> nameVariants) {
		metadata.setPersonNameVariants(nameVariants);
	}

	@Override
	public String toString() {
		return getFamilyName() + ", " + getGivenName() + " " + getEmails().stream().collect(Collectors.joining(", ", "<", ">")) + " "
				+ getAffiliations().stream().collect(Collectors.joining("; ", "(", ")"));
	}

	public static DSAuthor createAuthor(DSRoot dsRoot, Author author) {
		DSCollection authorsCollection = getAuthorsCollection(dsRoot);
		DSAuthor result = new DSAuthor();
		for (Signature signature : author.getSignatures()) {
			if (StringUtils.equals(signature.getFullName(), result.getFullName())) {
				continue;
			} else if (shouldUpdateName(result.getFullName(), signature.getFullName())) {
				if (result.getGivenName() != null || result.getFamilyName() != null) {
					result.addNameVariant(result.getFullName());
				}
				result.setGivenName(signature.getGivenName());
				result.setFamilyName(signature.getFamilyName());
				result.setName(result.getFullName());
			} else {
				result.addNameVariant(signature.getFullName());
			}
		}
		result.setAffiliations(author.getSignatures().stream().map(s -> s.getFullAffiliation().trim()).collect(Collectors.toSet()).stream().toList());
		result.setEmails(author.getSignatures().stream().map(s -> s.getEmail().toLowerCase().trim()).collect(Collectors.toSet()).stream().toList());
		return dsRoot.getItemsEndpoint().createAuthor(result, authorsCollection);
	}

	// Typically, this application wil run on a single DSpace site, so dsRoot will
	// be always the same
	// Thus, we will use a simple cache to avoid querying for the Author collection
	// continuously
	private static DSRoot cachedDSRoot;
	private static DSCollection cachedAuthorsCollection;

	synchronized private static DSCollection getAuthorsCollection(DSRoot dsRoot) {
		if (dsRoot.equals(cachedDSRoot) && cachedAuthorsCollection != null) {
			return cachedAuthorsCollection;
		}
		Collection<DSCommunity> topCommunities = dsRoot.getCommunitiesEndpoint().getSearchEndpoint().getSearchTopEndpoint().getAll();
		DSCommunity sistedesCommunity = topCommunities.stream().filter(c -> c.getName().equals("Archivo documental de Sistedes")).findFirst().orElseThrow();
		List<DSCollection> collections = sistedesCommunity.getCollections();
		cachedAuthorsCollection = collections.stream().filter(c -> c.getName().equals("Autores")).findAny().orElseThrow();
		cachedDSRoot = dsRoot;
		return cachedAuthorsCollection;
	}

	public static boolean shouldUpdateName(String existingName, String newName) {
		return newName.length() > existingName.length()
				|| newName.replaceAll("[^áéíóúàèìòùäëïöüâêîôûãẽĩõũ]", "").length() > existingName.replaceAll("[^áéíóúàèìòùäëïöüâêîôûãẽĩõũ]", "").length();
	}
}
