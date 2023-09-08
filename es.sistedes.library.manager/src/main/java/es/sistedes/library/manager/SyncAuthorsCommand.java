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

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.simplenamematcher.SimpleNameMatcher;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.dspace.model.DSAuthor;
import es.sistedes.library.manager.dspace.model.DSRoot;
import es.sistedes.library.manager.proceedings.model.Author;
import es.sistedes.library.manager.proceedings.model.Signature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "sync-authors", 
		description = "Synchronizes the authors information between the local submissions "
				+ "and the Sistedes Digital Library, trying to match existing authors in "
				+ "the library with local authors. In case the authors do not exist in the "
				+ "library, creates them. Already identified authors will be skipped when "
				+ "running in normal mode. In forced mode, information about  already identified "
				+ "authors will be discarded and a new match will be attempted.")
				
// @formatter:on
class SyncAuthorsCommand implements Callable<Integer> {

	private static final Double NAME_SIMILARITY_THRESHOLD_WHEN_MAIL_MATCHES = 50.0d;
	private static final Double NAME_SIMILARITY_THRESHOLD_WHEN_NAME_MATCHES = 90.0d;

	private static final Logger logger = LoggerFactory.getLogger(SyncAuthorsCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "DIR", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	@Option(names = { "-u", "--uri" }, paramLabel = "URI", required = true, description = "URI of the root endpoint of the DSpace API.")
	private URI uri;

	@Option(names = { "-i", "--interactive" }, description = "Ask interactively whether the found element (when in doubt) is a match or not.")
	private boolean interactive = false;

	@Option(names = { "-r", "--dry-run" }, description = "Do not perform any modifications.")
	private boolean dryRun = false;
	
	@Option(names = { "-c", "--force" }, 
			description = "Force execution, discarding existing information about identified authors already existing in the Sistedes Digital Library.")
	private boolean force = false;
	
	@Option(names = { "-e",
	"--email" }, paramLabel = "E-MAIL", required = true, description = "E-mail of the account required to log in the Sistedes Digital Library to create the authors.")
	private String email;

	@Option(names = { "-p",
	"--password" }, paramLabel = "PASSWORD", required = true, description = "Password of the account in the Sistedes Digital Library.")
	private String password;
	
	private ConferenceData conferenceData;
	private DSRoot dsRoot;

	@Override
	public Integer call() throws Exception {
		conferenceData = new ConferenceData(editionFile);

		dsRoot = DSRoot.create(uri);
		dsRoot.getAuthnEndpoint().doLogin(email, password);
		
		
		try {
			for (Author author : conferenceData.getAuthors().values()) {
				syncAuthor(author);
			}
		} finally {
			if (!dryRun) {
				conferenceData.save(true);
			}
		}
		// Return success
		return 0;
	}

	private void syncAuthor(Author author) {
		Optional<DSAuthor> dsAuthorOpt = Optional.empty();
		if (author.getSistedesUuid() == null || force) {
			dsAuthorOpt = findAuthor(author);
			if (dsAuthorOpt.isPresent()) {
				author.setSistedesUuid(dsAuthorOpt.get().getUuid());
				logger.info(MessageFormat.format("Sistedes UUID ''{0}'' found for ''{1}''", author.getSistedesUuid(), author));
			} else {
				logger.warn(MessageFormat.format("Unable to find a match for ''{0}''", author));
			}
		}
		if (dryRun) {
			logger.debug("Running in dry-run mode, skipping author retrieval, creation and update");
		} else {
			if (!force && author.getSistedesUuid() != null && dsAuthorOpt.isEmpty()) {
				// We didn't retrieve an existing author because we're running in normal
				// mode (i.e., not forced)
				// We must retrieve it before continuing...
				dsAuthorOpt = dsRoot.getItemsEndpoint().getAuthor(author.getSistedesUuid());
			}
			if (dsAuthorOpt.isEmpty()) {
				// No existing author has been found, we must create it
				logger.debug(MessageFormat.format("Creating Author for ''{0}''...", author));
				DSAuthor dsAuthor = DSAuthor.createAuthor(dsRoot, author);
				author.setSistedesUuid(dsAuthor.getUuid());
				logger.info(MessageFormat.format("Created Author for ''{0}''", author));
			} else {
				// We must update an existing author
				logger.debug(MessageFormat.format("Updating Author for ''{0}''...", author));
				DSAuthor dsAuthor = dsAuthorOpt.get();
				for (Signature signature : author.getSignatures()) {
					// @formatter:off
					if (DSAuthor.shouldUpdateName(dsAuthor.getFullName(), signature.getFullName())) {
						// Set the current name as a variant
						dsAuthor.addNameVariant(dsAuthor.getFullName());
						// Update the name
						dsAuthor.setGivenName(signature.getGivenName());
						dsAuthor.setFamilyName(signature.getFamilyName());
						dsAuthor.setName(dsAuthor.getFullName());
						// Make sure that the current name is not listed as a variant
						// which was added in the past
						dsAuthor.setNameVariants(dsAuthor.getNameVariants().stream().filter(variant -> !variant.equals(dsAuthor.getFullName())).toList());
					} else if (!StringUtils.equals(
									StringUtils.stripAccents(dsAuthor.getFullName().toLowerCase()), 
									StringUtils.stripAccents(signature.getFullName().toLowerCase()))
								&& !dsAuthor.getNameVariants().contains(signature.getFullName())) {
						// TODO: Check this function again in the future, this condition
						// was added after the 2023 proceedings were produced because some
						// name variants were not saved properly
						dsAuthor.addNameVariant(signature.getFullName());
						
					}
					// @formatter:on
					// If all existing affiliations are different (90% or less) to the one in the signature, add it to the list
					// Do the computation ignoring casing, accents, punctuation marks, and normalizing the spaces
					if (dsAuthor.getAffiliations().stream().allMatch(aff -> { 
						String affiliation1 = StringUtils.normalizeSpace(StringUtils.stripAccents(aff).replaceAll("\\p{Punct}", "")).toLowerCase();
						String affiliation2 = StringUtils.normalizeSpace(StringUtils.stripAccents(signature.getFullAffiliation()).replaceAll("\\p{Punct}", "")).toLowerCase();
						return (SimpleNameMatcher.compareNamesSafe(affiliation1, affiliation2) <= 90);
						})) {
						dsAuthor.addAffiliation(signature.getFullAffiliation().trim());
					}
					// Add the e-mail if it doesn't exist yet
					if (!dsAuthor.getEmails().stream().map(em -> em.toLowerCase()).toList().contains(signature.getEmail().toLowerCase().trim())) {
						dsAuthor.addEmail(signature.getEmail().toLowerCase().trim());
					}
					// Add the web if it doesn't exist yet
					if (!dsAuthor.getWebs().stream().map(web -> web.toLowerCase()).toList().contains(signature.getWeb().toLowerCase().trim())) {
						dsAuthor.addWeb(signature.getWeb().toLowerCase().trim());
					}
				}
				dsAuthor.save();
				logger.info(MessageFormat.format("Updated Author for ''{0}''", author));
			}
		}
	}

	private Optional<DSAuthor> findAuthor(Author author) {
		Optional<DSAuthor> result = null;
		for (Signature signature : author.getSignatures()) {
			// First try to search by e-mail
			result = searchAuthorByEmail(signature);
			// Next, if not find, try by full name
			if (!result.isPresent())
				result = searchAuthorByName(signature);
			// Still not found, continue and try with another signature...
			if (!result.isPresent())
				continue;
			// Found a match, stop searching...
			else
				break;
		}
		;
		return result;
	}

	private Optional<DSAuthor> searchAuthorByEmail(Signature signature) {
		Optional<DSAuthor> result = dsRoot.searchAuthor(signature.getEmail());
		if (result.isPresent()) {
			DSAuthor dsAuthor = result.get();
			if (dsAuthor.getEmails().contains(signature.getEmail().toLowerCase())) {
				// @formatter:off
				Double maxSimilarity = getMaxSignatureNameSimilarity(signature, dsAuthor);
				if (maxSimilarity > NAME_SIMILARITY_THRESHOLD_WHEN_MAIL_MATCHES) {
					logger.debug(MessageFormat.format(
							"Exact match found:\n"
									+ " - Searched signature: {0}\n"
									+ " - Found author:    {1}", 
									signature, dsAuthor));
					return Optional.of(dsAuthor);
				} else {
					String message = MessageFormat.format(
							"E-mail match found, but name similarity ({0}%) is below the threshold ({1}%):\n"
									+ " - Searched signature: {2}\n"
									+ " - Found author:       {3}", 
									maxSimilarity, NAME_SIMILARITY_THRESHOLD_WHEN_MAIL_MATCHES, signature, dsAuthor);
					if (interactive) {
						System.out.println(message);
						if (readConfirmation("Is it a match?")) {
							logger.info(MessageFormat.format(
									"Approximate match found with similarity ({0}%) below the threshold ({1}%), but manually overriden:\n"
											+ " - Searched signature: {2}\n"
											+ " - Found author:       {3}", 
											maxSimilarity, NAME_SIMILARITY_THRESHOLD_WHEN_MAIL_MATCHES, signature, dsAuthor));
							return Optional.of(dsAuthor);
						}
					} else {
						logger.warn(message);
					}
				}
				// @formatter:on
			}
		}
		return Optional.empty();
	}

	private Optional<DSAuthor> searchAuthorByName(Signature signature) {
		Optional<DSAuthor> result = dsRoot.searchAuthor(signature.getFullName());
		if (result.isPresent()) {
			DSAuthor dsAuthor = result.get();
			// @formatter:off
			Double maxSimilarity = getMaxSignatureNameSimilarity(signature, dsAuthor);
			if (maxSimilarity > NAME_SIMILARITY_THRESHOLD_WHEN_NAME_MATCHES) {
				logger.debug(MessageFormat.format(
						"Exact match found:\n"
								+ " - Searched signature: {0}\n"
								+ " - Found author:    {1}", 
								signature, dsAuthor));
				return Optional.of(dsAuthor);
			} else {
				String message = MessageFormat.format(
						"Name match found, but name similarity ({0}%) is below the threshold ({1}%):\n"
								+ " - Searched signature: {2}\n"
								+ " - Found author:       {3}", 
								maxSimilarity, NAME_SIMILARITY_THRESHOLD_WHEN_NAME_MATCHES, signature, dsAuthor);
				if (interactive) {
					System.out.println(message);
					if (readConfirmation("Is it a match?")) {
						logger.info(MessageFormat.format(
								"Approximate match found with similarity ({0}%) below the threshold ({1}%), but manually overriden:\n"
										+ " - Searched signature: {2}\n"
										+ " - Found author:       {3}", 
										maxSimilarity, NAME_SIMILARITY_THRESHOLD_WHEN_NAME_MATCHES, signature, dsAuthor));
						return Optional.of(dsAuthor);
					}
				} else {
					logger.warn(message);
				}
			}
			// @formatter:on
		}
		return Optional.empty();
	}

	private boolean readConfirmation(String message) {
		Toolkit.getDefaultToolkit().beep();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			do {
				System.out.print(message + " [y/N]: ");
				switch (reader.readLine()) {
				case "Y":
				case "y":
					return true;
				case "":
				case "N":
				case "n":
					return false;
				}
			} while (true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Double getMaxSignatureNameSimilarity(Signature signature, DSAuthor dsAuthor) {
		List<String> allAuthorNames = new ArrayList<>(dsAuthor.getNameVariants());
		allAuthorNames.add(dsAuthor.getFullName());
		Optional<Double> maxSimilarity = allAuthorNames.stream().map(variant -> SimpleNameMatcher.compareNamesSafe(signature.getFullName(), variant))
				.max(Comparator.naturalOrder());
		return maxSimilarity.orElse(0.0d);
	}
}
