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
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "validate", 
		description = "Validates that the conference data is ready for submission without performing any modification.")
// @formatter:on
class ValidateCommand implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(ValidateCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "FILE", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	private ConferenceData conferenceData;

	@Override
	public Integer call() throws Exception {
		conferenceData = new ConferenceData(editionFile);
		// @formatter:off
		boolean success = 
				validateAuthorsHaveSistedesId(conferenceData) 
				&& validateSubmissionsHaveType(conferenceData)
				&& validateProceedingsEltsHaveSistedesHandles(conferenceData)
				&& validateNotDuplicateHandles(conferenceData);
		// @formatter:on
		return success ? 0 : 1;
	}

	public static boolean validateAuthorsHaveSistedesId(ConferenceData conferenceData) {
		final AtomicBoolean isValid = new AtomicBoolean(true);
		conferenceData.getSubmissions().values().stream().forEach(submission -> {
			submission.getSignatures().forEach(signature -> {
				if (StringUtils.isBlank(conferenceData.getAuthors().get(signature.getAuthor()).getSistedesUuid())) {
					logger.error(MessageFormat.format("Signature ''{0}'' of submission ''{1,number,#}'' does not refer to any Sistedes author",
							signature.getFullName(), submission.getId()));
					isValid.set(false);
				}
			});
		});
		return isValid.get();
	}

	public static boolean validateSubmissionsHaveType(ConferenceData conferenceData) {
		final AtomicBoolean isValid = new AtomicBoolean(true);
		conferenceData.getSubmissions().values().stream().forEach(submission -> {
			if (submission.getType() == null) {
				logger.error(MessageFormat.format("Submission ''{0}'' has no type", submission.getId()));
				isValid.set(false);
			}
		});
		return isValid.get();
	}

	public static boolean validateProceedingsEltsHaveSistedesHandles(ConferenceData conferenceData) {
		final AtomicBoolean isValid = new AtomicBoolean(true);
		if (StringUtils.isEmpty(conferenceData.getEdition().getSistedesHandle())) {
			logger.error(MessageFormat.format("Edition community ''{0}'' has no Sistedes Handle", conferenceData.getEdition().getId()));
			isValid.set(false);
		}
		if (StringUtils.isEmpty(conferenceData.getEdition().getPreliminariesSistedesHandle())) {
			logger.error("Preliminaries collection has no Sistedes Handle");
			isValid.set(false);
		}
		conferenceData.getPreliminaries().stream().forEach(prelim -> {
			if (StringUtils.isEmpty(prelim.getSistedesHandle())) {
				logger.error(MessageFormat.format("Preliminaries ''{0}'' ({1}) has no Sistedes Handle", prelim.getTitle(), prelim.getId()));
				isValid.set(false);
			}
		});
		conferenceData.getTracks().values().stream().forEach(track -> {
			if (StringUtils.isEmpty(track.getSistedesHandle())) {
				logger.error(MessageFormat.format("Track ''{0}'' ({1}) has no Sistedes Handle", track.getAcronym(), track.getId()));
				isValid.set(false);
			}
		});
		conferenceData.getSubmissions().values().stream().forEach(submission -> {
			if (StringUtils.isEmpty(submission.getSistedesHandle())) {
				logger.error(MessageFormat.format("Submission ''{0}'' ({1,number,#}) has no Sistedes Handle", submission.getTitle(), submission.getId()));
				isValid.set(false);
			}
		});
		return isValid.get();
	}
	
	/**
	 * This method should be no longer needed, since internal handles were required
	 * to register the Sistedes Handle to Internal Handle redirection which is now
	 * deprecated
	 * 
	 * @param conferenceData
	 * @return
	 */
	@Deprecated
	public static boolean validateProceedingsEltsHaveInternalHandles(ConferenceData conferenceData) {
		final AtomicBoolean isValid = new AtomicBoolean(true);
		if (StringUtils.isEmpty(conferenceData.getEdition().getInternalHandle())) {
			logger.error(MessageFormat.format("Edition community ''{0}'' has no internal Handle", conferenceData.getEdition().getShortName()));
			isValid.set(false);
		}
		if (StringUtils.isEmpty(conferenceData.getEdition().getPreliminariesInternalHandle())) {
			logger.error("Preliminaries collection has no internal Handle");
			isValid.set(false);
		}
		conferenceData.getPreliminaries().stream().forEach(prelim -> {
			if (StringUtils.isEmpty(prelim.getInternalHandle())) {
				logger.error(MessageFormat.format("Preliminaries ''{0}'' ({1}) has no internal Handle", prelim.getTitle(), prelim.getId()));
				isValid.set(false);
			}
		});
		conferenceData.getTracks().values().stream().forEach(track -> {
			if (StringUtils.isEmpty(track.getInternalHandle())) {
				logger.error(MessageFormat.format("Track ''{0}'' ({1}) has no internal Handle", track.getAcronym(), track.getId()));
				isValid.set(false);
			}
		});
		conferenceData.getSubmissions().values().stream().forEach(submission -> {
			if (StringUtils.isEmpty(submission.getInternalHandle())) {
				logger.error(MessageFormat.format("Submission ''{0}'' ({1,number,#}) has no internal Handle", submission.getTitle(), submission.getId()));
				isValid.set(false);
			}
		});
		return isValid.get();
	}
	
	public static boolean validateNotDuplicateHandles(ConferenceData conferenceData) {
		Set<String> handles = new HashSet<>();
		final AtomicBoolean isValid = new AtomicBoolean(true);
		
		if (handles.contains(conferenceData.getEdition().getSistedesHandle())) {
			logger.error(MessageFormat.format("Duplicate Handle found ''{0}''!", conferenceData.getEdition().getId()));
			isValid.set(false);
		}
		handles.add(conferenceData.getEdition().getSistedesHandle());
		
		if (handles.contains(conferenceData.getEdition().getPreliminariesSistedesHandle())) {
			logger.error(MessageFormat.format("Duplicate Handle found ''{0}''!", conferenceData.getEdition().getPreliminariesSistedesHandle()));
			isValid.set(false);
		}
		handles.add(conferenceData.getEdition().getPreliminariesSistedesHandle());
		
		conferenceData.getPreliminaries().stream().forEach(prelim -> {
			if (handles.contains(prelim.getSistedesHandle())) {
				logger.error(MessageFormat.format("Duplicate Handle found ''{0}''!", prelim.getId()));
				isValid.set(false);
			}
			handles.add(prelim.getSistedesHandle());
		});
		
		conferenceData.getTracks().values().stream().forEach(track -> {
			if (handles.contains(track.getSistedesHandle())) {
				logger.error(MessageFormat.format("Duplicate Handle found ''{0}''!", track.getId()));
				isValid.set(false);
			}
			handles.contains(track.getSistedesHandle());
		});
		conferenceData.getSubmissions().values().stream().forEach(submission -> {
			if (handles.contains(submission.getSistedesHandle())) {
				logger.error(MessageFormat.format("Duplicate Handle found ''{0}''!", submission.getId()));
				isValid.set(false);
			}
			handles.contains(submission.getSistedesHandle());
		});
		return isValid.get();
	}
}
