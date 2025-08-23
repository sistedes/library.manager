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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.markdown4j.Markdown4jProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.DSpaceConnectionManager.DSpaceConnection;
import es.sistedes.library.manager.dspace.model.DSAuthor;
import es.sistedes.library.manager.dspace.model.DSBundle;
import es.sistedes.library.manager.dspace.model.DSCollection;
import es.sistedes.library.manager.dspace.model.DSCommunity;
import es.sistedes.library.manager.dspace.model.DSItem;
import es.sistedes.library.manager.dspace.model.DSProcess;
import es.sistedes.library.manager.dspace.model.DSProcess.DSParameter;
import es.sistedes.library.manager.dspace.model.DSPublication;
import es.sistedes.library.manager.dspace.model.DSResourcePolicy;
import es.sistedes.library.manager.dspace.model.DSRoot;
import es.sistedes.library.manager.dspace.model.RelationshipType;
import es.sistedes.library.manager.proceedings.model.AbstractProceedingsDocument;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import es.sistedes.library.manager.proceedings.model.Edition;
import es.sistedes.library.manager.proceedings.model.Preliminaries;
import es.sistedes.library.manager.proceedings.model.Signature;
import es.sistedes.library.manager.proceedings.model.Submission;
import es.sistedes.library.manager.proceedings.model.Track;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "publish", 
		description = "Publishes the specified edition in the Sistedes Digital Library. Published elements will be recorded locally to avoid recreating them.")
				
// @formatter:on
class PublishEditionCommand implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(PublishEditionCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "FILE", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	@Option(names = { "-u", "--uri" }, paramLabel = "URI", required = true, description = "URI of the root endpoint of the DSpace API.")
	private URI uri;

	@Option(names = { "-e",
			"--email" }, paramLabel = "E-MAIL", required = true, description = "E-mail of the account required to log in the Sistedes Digital Library to create the authors.")
	private String email;

	@Option(names = { "-p", "--password" }, paramLabel = "PASSWORD", required = true, description = "Password of the account in the Sistedes Digital Library.")
	private String password;

	@Option(names = { "-a", "--admin-only" }, description = "Publish with administrator-only permissions (i.e., hidden to the general public).")
	private boolean private_ = false;
	
	@Option(names = { "-c", "--curate" }, description = "Also launch curation tasks that may be applicable to the newly created communities, "
			+ "collections and items (i.e., registerexternalhandle, filtermedia, generatecitation, generatebibcitation).")
	private boolean curate = false;

	private ConferenceData conferenceData;
	private DSpaceConnection connection;
	private DSRoot dsRoot;

	@Override
	public Integer call() throws Exception {

		conferenceData = new ConferenceData(editionFile);
		
		if (!ValidateCommand.validateAuthorsHaveSistedesId(conferenceData)) {
			System.err.println("ERROR: Some authors are not in sync with the Sistedes Digital Library. Execute the 'sync-authors' command first.");
			mainCmd.spec.subcommands().get("sync-authors").getCommandSpec().commandLine().usage(System.err);
			return 1;
		}

		connection = DSpaceConnectionManager.createConnection(uri, email, password);
		dsRoot = connection.getDsRoot();

		// Get the top level community
		DSCommunity conferenceCommunity = findConferenceTopCommunity();

		// Get or create the edition community
		Edition edition = conferenceData.getEdition();
		
		DSCommunity editionCommunity = getEditionCommunity(conferenceCommunity, edition).orElseGet(() -> { 
			DSCommunity comm = createEditionCommunity(conferenceCommunity, edition);
			if (private_) {
				deleteReadResourcePolicies(comm.getUuid());
			}
			edition.setInternalHandle(comm.getHandle());
			edition.setSistedesUuid(comm.getUuid());
			edition.save();
			return comm;
		});

		// Publish the preliminaries
		publishPreliminaries(editionCommunity, edition);

		// Publish the papers
		publishTracks(editionCommunity, edition);
		
		// @formatter:off
		if (curate) {
			DSProcess process;
			process = dsRoot.getScriptsEndpoint().executeScript("curate", Arrays.asList(
					new DSParameter("-t", "registerexternalhandle"), 
					new DSParameter("-i", editionCommunity.getHandle()),
					new DSParameter("-p", "force=true")));
			
			logger.info(MessageFormat.format("Process ''curate registerexternalhandle'' created with id ''{0}'' and status ''{1}''", 
					process.getProcessId(), process.getProcessStatus()));

			process = dsRoot.getScriptsEndpoint().executeScript("curate", Arrays.asList(
					new DSParameter("-t", "filtermedia"), 
					new DSParameter("-i", editionCommunity.getHandle())));
			
			logger.info(MessageFormat.format("Process ''curate filtermedia'' created with id ''{0}'' and status ''{1}''", 
					process.getProcessId(), process.getProcessStatus()));
			
			process = dsRoot.getScriptsEndpoint().executeScript("curate", Arrays.asList(
					new DSParameter("-t", "generatecitation"), 
					new DSParameter("-i", editionCommunity.getHandle())));

			logger.info(MessageFormat.format("Process ''curate generatecitation'' created with id ''{0}'' and status ''{1}''", 
					process.getProcessId(), process.getProcessStatus()));

			process = dsRoot.getScriptsEndpoint().executeScript("curate", Arrays.asList(
					new DSParameter("-t", "generatebibcitation"), 
					new DSParameter("-i", editionCommunity.getHandle())));

			logger.info(MessageFormat.format("Process ''curate generatebibcitation'' created with id ''{0}'' and status ''{1}''", 
					process.getProcessId(), process.getProcessStatus()));
		}
		// @formatter:on

		// Return success
		return 0;
	}

	private void publishPreliminaries(DSCommunity editionCommunity, Edition edition) {
		DSCollection preliminariesCollection = getPreliminariesCollection(editionCommunity, edition).orElseGet(() -> {
			DSCollection coll = createPreliminariesCollection(editionCommunity, edition);
			if (private_) {
				deleteReadResourcePolicies(coll.getUuid());
			}
			edition.setPreliminariesSistedesUuid(coll.getUuid());
			edition.setPreliminariesSistedesHandle(coll.getSistedesIdentifier());
			edition.setPreliminariesInternalHandle(coll.getHandle());
			edition.save();
			return coll;
		});
		

		// Start iterating over the preliminaries
		for (Preliminaries prelim : conferenceData.getPreliminaries().values()) {
			DSPublication dsPublication = null;
			if (StringUtils.isEmpty(prelim.getSistedesUuid())) {
				// Create the preliminaries only if it has not been created yet
				logger.debug(MessageFormat.format("Creating publication for preliminaries ''{0}''", prelim.getTitle()));
				dsPublication = DSPublication.createPublication(dsRoot, preliminariesCollection, edition, prelim);
				if (private_) {
					deleteReadResourcePolicies(dsPublication.getUuid());
				}
				logger.info(MessageFormat.format("Created publication for preliminaries ''{0}'' with UUID ''{1}''", dsPublication.getName(), dsPublication.getUuid()));
				prelim.setSistedesUuid(dsPublication.getUuid());
				prelim.setInternalHandle(dsPublication.getHandle());
				prelim.save();
			} else {
				dsPublication = dsRoot.getItemsEndpoint().getPublication(prelim.getSistedesUuid()).orElseThrow();
				logger.info(MessageFormat.format("Publication for prelimiaries ''{0}'' ({1}) already exists with UUID ''{2}''",
						prelim.getId(), prelim.getTitle(), prelim.getSistedesUuid()));
			}

			managePublicationBitstreams(prelim, dsPublication);
		}
	}

	private void publishTracks(DSCommunity editionCommunity, Edition edition) {
		for (Track track : conferenceData.getTracks().values()) {
			// If there's no abstract, set a default one
			if (StringUtils.isEmpty(track.getAbstract())) {
				track.setAbstract(MessageFormat.format("Artículos en la categoría <em>{0}</em> publicados en las Actas de las <em>{1}</em>.", track.getName(),
						edition.getFullName(), edition.getShortName()));
			}

			// Get (or create, if it does not exist) the collection which will hold the
			// publications
			DSCollection trackCollection = getTrackCollection(editionCommunity, track).orElseGet(() -> {
				DSCollection coll = createTrackCollection(editionCommunity, track);
				if (private_) {
					deleteReadResourcePolicies(coll.getUuid());
				}
				track.setSistedesUuid(coll.getUuid());
				track.setInternalHandle(coll.getHandle());
				track.save();
				return coll;
			});
			
			// Start iterating over the submissions
			for (int submissionId : track.getSubmissions()) {
				Submission submission = conferenceData.getSubmissions().get(submissionId);
				DSPublication dsPublication;
				if (StringUtils.isEmpty(submission.getSistedesUuid())) {
					// Create the submission only if it has not been created yet 
					logger.debug(MessageFormat.format("Creating publication for ''{0}''", submission.getTitle()));
					dsPublication = DSPublication.createPublication(dsRoot, trackCollection, edition, submission);
					if (private_) {
						deleteReadResourcePolicies(dsPublication.getUuid());
					}
					logger.info(MessageFormat.format("Created publication for ''{0}'' with UUID ''{1}''", dsPublication.getName(), dsPublication.getUuid()));
					submission.setSistedesUuid(dsPublication.getUuid());
					submission.setInternalHandle(dsPublication.getHandle());
					submission.save();
				} else {
					dsPublication = dsRoot.getItemsEndpoint().getPublication(submission.getSistedesUuid()).orElseThrow();
					logger.info(MessageFormat.format("Publication for prelimiaries ''{0}'' ({1}) already exists with UUID ''{2}''",
							submission.getId(), submission.getTitle(), submission.getSistedesUuid()));
					}
				manageSubmissionAuthorships(submission, dsPublication);
				managePublicationBitstreams(submission, dsPublication);
			}
		}
	}

	private void manageSubmissionAuthorships(Submission submission, DSPublication dsPublication) {
		if (dsPublication.getRelationships().size() == submission.getSignatures().size()) {
			logger.info(MessageFormat.format("Authors for submission ''{0}'' ({1}) are already registered, skipping...", submission.getId(), submission.getTitle()));
		} else if (dsPublication.getRelationships().size() > submission.getSignatures().size()) {
			throw new RuntimeException(
					MessageFormat.format("Submission ''{0}'' ({1}) has more relationships than signatures!", submission.getId(), submission.getTitle()));
		} else {
			// First clean all existing relationships
			dsPublication.getRelationships().forEach(r -> {
				logger.info(MessageFormat.format("Deleting authorship relationship with id ''{0,number,#}''", r.getId()));
				dsRoot.getRelationshipsEndpoint().deleteRelationship(r.getId());
			});
			// Now recreate authorships
			for (Signature signature : submission.getSignatures()) {
				logger.debug(MessageFormat.format("Creating authorship for ''{0}''", signature));
				DSAuthor dsAuthor = dsRoot.getItemsEndpoint().getAuthor(signature.getSistedesUuid()).orElseThrow();
				if (submission.getType().getPublicationType() == DSItem.Type.ABSTRACT) {
					dsRoot.getRelationshipsEndpoint().createRelationship(getIsAuthorOfAbstractRelationship(), dsPublication, dsAuthor);
				} else if (submission.getType().getPublicationType() == DSItem.Type.PAPER) {
					dsRoot.getRelationshipsEndpoint().createRelationship(getIsAuthorOfPaperRelationship(), dsPublication, dsAuthor);
				} else {
					throw new RuntimeException(MessageFormat.format("Unexpected type of publication ''{0}'' in submission ''{1}''",
							submission.getType().getPublicationType(), submission.getId()));
				}
				logger.info(MessageFormat.format("Created authorship for ''{0}''", signature));
			}
		}
	}

	private void managePublicationBitstreams(AbstractProceedingsDocument document, DSPublication dsPublication) {
		DSBundle originalBundle = dsPublication.getOriginalBundle().orElseGet(() -> dsPublication.createOriginalBundle());

		File originalFile = new File(editionFile.getParent(), document.getFilename());
		File otherFile = null;
		if (FilenameUtils.getExtension(originalFile.getAbsolutePath()).equals("md")) {
			File pdfFile = new File(FilenameUtils.removeExtension(originalFile.getAbsolutePath()) + ".pdf");
			if (pdfFile.exists()) {
				logger.info(MessageFormat.format("File for document ''{0}'' is in markdown, but a PDF file with the same name already exists. Skipping file conversion...", document.getTitle()));
			} else {
				logger.info(MessageFormat.format("File for document ''{0}'' is in markdown, a conversion to PDF will be attempted", document.getTitle()));
				writePdfFile(document.getTitle(), originalFile, pdfFile);
			}
			otherFile = originalFile;
			originalFile = pdfFile;
		}

		if (originalBundle.getBitstreams().size() == 1) {
			logger.info(MessageFormat.format("Main file for submission ''{0}'' ({1}) is already uploaded, skipping...", document.getId(), document.getTitle()));	
		} else if (originalBundle.getBitstreams().size() > 1) {
			throw new RuntimeException(
					MessageFormat.format("Submission ''{0}'' ({1}) has more than one ORIGINAL bitstream!", document.getId(), document.getTitle()));
		} else {
			logger.debug(MessageFormat.format("Uploading main file ''{0}''", originalFile));
			originalBundle.createBitstreamFrom(new FileSystemResource(originalFile),
					document.getSistedesHandle().replaceAll("/", "-") + "." + FilenameUtils.getExtension(originalFile.getAbsolutePath()));
			logger.info(MessageFormat.format("Uploaded main file ''{0}''", originalFile));
		}
		
		if (otherFile != null) {
			DSBundle otherBundle = dsPublication.getOtherBundle().orElseGet(() -> dsPublication.createOtherBundle());
			if (otherBundle.getBitstreams().size() == 1) {
				logger.info(MessageFormat.format("Alternative file for submission ''{0}'' ({1}) is already uploaded, skipping...", document.getId(), document.getTitle()));	
			} else if (otherBundle.getBitstreams().size() > 1) {
				throw new RuntimeException(
						MessageFormat.format("Submission ''{0}'' ({1}) has more than one OTHER bitstream!", document.getId(), document.getTitle()));
			} else {
				logger.debug(MessageFormat.format("Uploading alternative file ''{0}''", otherFile));
				otherBundle.createBitstreamFrom(new FileSystemResource(otherFile),
						document.getSistedesHandle().replaceAll("/", "-") + "." + FilenameUtils.getExtension(otherFile.getAbsolutePath()));
				logger.info(MessageFormat.format("Uploaded alternative file ''{0}''", otherFile));
			}
		}
	}
	
	private Optional<DSCommunity> getEditionCommunity(DSCommunity conferenceCommunity, Edition edition) {
		if (edition.getSistedesUuid() == null) {
			return Optional.empty();
		}
		DSCommunity editionCommunity = dsRoot.getCommunitiesEndpoint().getCommunity(edition.getSistedesUuid());
		logger.info(MessageFormat.format("Found community for ''{0}'' with UUID ''{1}''", editionCommunity.getName(), editionCommunity.getUuid()));
		return Optional.of(editionCommunity);
	}

	private DSCommunity createEditionCommunity(DSCommunity conferenceCommunity, Edition edition) {
		logger.debug(MessageFormat.format("Creating community for ''{0}''", edition.getShortName()));
		DSCommunity editionCommunity = DSCommunity.createSubCommunity(dsRoot, conferenceCommunity, edition);
		edition.setSistedesUuid(editionCommunity.getUuid());
		edition.setInternalHandle(editionCommunity.getHandle());
		logger.info(MessageFormat.format("Created community for ''{0}'' with UUID ''{1}''", editionCommunity.getName(), editionCommunity.getUuid()));
		return editionCommunity;
	}
	
	private Optional<DSCollection> getPreliminariesCollection(DSCommunity editionCommunity, Edition edition) {
		if (edition.getPreliminariesSistedesUuid() == null) {
			return Optional.empty();
		}
		DSCollection preliminariesCollection = dsRoot.getCollectionsEndpoint().getCollection(edition.getPreliminariesSistedesUuid());
		logger.info(MessageFormat.format("Found collection for ''{0}'' with UUID ''{1}''", preliminariesCollection.getName(), preliminariesCollection.getUuid()));
		return Optional.of(preliminariesCollection);
	}

	private DSCollection createPreliminariesCollection(DSCommunity editionCommunity, Edition edition) {
		Track preliminaries = new Track();
		// @formatter:off
		preliminaries.setName("Preliminares");
		preliminaries.setAcronym("PRELIMINARES");
		preliminaries.setSistedesHandle(edition.getSistedesHandle() + "/PRELIMINARES");
		preliminaries.setAbstract(MessageFormat.format(
						"Preliminares de las Actas de las {0}. Los preliminares de las actas incluyen "
						+ "información adicional sobre las jornadas, tales como la presentación de las jornadas, "
						+ "los comités participantes, las conferencias invitadas, o los agradecimientos, entre "
						+ "otras secciones.",
						edition.getFullName()));
		logger.debug(MessageFormat.format("Creating collection for ''{0}''", preliminaries.getName()));
		DSCollection preliminariesCollection = DSCollection.createCollection(dsRoot, editionCommunity, preliminaries);
		logger.info(MessageFormat.format("Created collection for ''{0}'' with UUID ''{1}''", preliminariesCollection.getName(), preliminariesCollection.getUuid()));
		// @formatter:on

		return preliminariesCollection;
	}
	
	private Optional<DSCollection> getTrackCollection(DSCommunity editionCommunity, Track track) {
		if (track.getSistedesUuid() == null) {
			return Optional.empty();
		}
		DSCollection trackCollection = dsRoot.getCollectionsEndpoint().getCollection(track.getSistedesUuid());
		logger.info(MessageFormat.format("Found collection for ''{0}'' with UUID ''{1}''", trackCollection.getName(), trackCollection.getUuid()));
		return Optional.of(trackCollection);
	}

	private DSCollection createTrackCollection(DSCommunity editionCommunity, Track track) {
		logger.debug(MessageFormat.format("Creating collection for ''{0}''", track.getName()));
		DSCollection trackCollection = DSCollection.createCollection(dsRoot, editionCommunity, track);
		logger.info(MessageFormat.format("Created collection for ''{0}'' with UUID ''{1}''", trackCollection.getName(), trackCollection.getUuid()));
		return trackCollection;
	}

	private DSCommunity findConferenceTopCommunity() {
		logger.debug("Retrieving conference community...");
		Collection<DSCommunity> communities = dsRoot.getCommunitiesEndpoint().getSearchEndpoint().getSearchTopEndpoint().getAll();
		DSCommunity conferenceCommunity = communities.stream().filter(c -> c.getName().endsWith("(" + conferenceData.getEdition().getAcronym() + ")")).findAny()
				.orElseThrow();
		logger.info(MessageFormat.format("Found community for ''{0}'' with UUID ''{1}''", conferenceCommunity.getName(), conferenceCommunity.getUuid()));
		return conferenceCommunity;
	}

	private void deleteReadResourcePolicies(String uuid) {
		List<DSResourcePolicy> policies = dsRoot.getResourcePoliciesEndpoint().getResourcePoliciesFor(uuid);
		policies.stream().filter(p -> DSResourcePolicy.ACTION_READ.equals(p.getAction())).forEach(p -> {
			Integer id = p.getId();
			dsRoot.getResourcePoliciesEndpoint().deleteResourcePolicy(id);
		});
	}

	private void writePdfFile(String title, File originalFile, File pdfFile) {
		try {
			// Convert Markdown to HTML
			String htmlString = "<title>" + title + "</title>\n" + new Markdown4jProcessor().process(originalFile);
			// Cleanup HTML
			Writer cleanHtmlWriter = new StringWriter();
			Tidy tidy = new Tidy();
			tidy.setQuiet(true);
			tidy.setShowWarnings(false);
			tidy.setXHTML(true);
			tidy.setInputEncoding(StandardCharsets.UTF_8.displayName());
			tidy.setOutputEncoding(StandardCharsets.UTF_8.displayName());
			tidy.parseDOM(new StringReader(htmlString), cleanHtmlWriter);
			// Convert to PDF
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(cleanHtmlWriter.toString());
			renderer.layout();
			renderer.createPDF(new FileOutputStream(pdfFile));
		} catch (IOException | DocumentException e) {
			logger.error(MessageFormat.format("Unable to convert ''{0}'' to PDF", originalFile));
		}
	}

	/*
	 * Utility methods to cache the relationship types that are used when creating
	 * new editions of a conference
	 */
	private RelationshipType isAuthorOfPaperRelationship;

	private RelationshipType getIsAuthorOfPaperRelationship() {
		if (isAuthorOfPaperRelationship == null) {
			isAuthorOfPaperRelationship = dsRoot.getRelationshipTypesEndpoint().getIsAuthorOfPaperRelationship();
		}
		return isAuthorOfPaperRelationship;
	}

	private RelationshipType isAuthorOfAbstractRelationship;

	private RelationshipType getIsAuthorOfAbstractRelationship() {
		if (isAuthorOfAbstractRelationship == null) {
			isAuthorOfAbstractRelationship = dsRoot.getRelationshipTypesEndpoint().getIsAuthorOfAbstractRelationship();
		}
		return isAuthorOfAbstractRelationship;
	}
}
