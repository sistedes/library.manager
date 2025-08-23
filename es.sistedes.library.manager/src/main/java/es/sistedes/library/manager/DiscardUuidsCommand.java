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
import java.util.concurrent.Callable;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "discard-uuids", 
		description = "Deletes the Sistedes UUIDs for the specified elements of the proceedings.")
// @formatter:on
class DiscardUuidsCommand implements Callable<Integer> {

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "FILE", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	@Option(names = { "-a", "--authors" }, description = "Discard stored UUID for the authors.")
	private boolean authors;
	
	@Option(names = { "-e", "--edition" }, description = "Discard stored UUID for the edition.")
	private boolean edition;
	
	@Option(names = { "-t", "--tracks" }, description = "Discard stored UUID for the tracks.")
	private boolean tracks;
	
	@Option(names = { "-p", "--preliminaries" }, description = "Discard stored UUID for the preliminaries.")
	private boolean preliminaries;
	
	@Option(names = { "-s", "--submissions" }, description = "Discard stored UUID for the submission.")
	private boolean submissions;
	
	private ConferenceData conferenceData;

	@Override
	public Integer call() throws Exception {
		conferenceData = new ConferenceData(editionFile);
		if (authors) {
			conferenceData.getAuthors().values().forEach(author -> {
				author.setSistedesUuid(null);
				author.save();
			});
		}
		if (edition) {
			conferenceData.getEdition().setSistedesUuid(null);
			conferenceData.getEdition().save();
		}
		if (tracks) {
			conferenceData.getTracks().values().forEach(track -> {
				track.setSistedesUuid(null);
			});
			conferenceData.getTracks().save();
		}
		if (preliminaries) {
			conferenceData.getPreliminaries().values().forEach(prelim -> {
				prelim.setSistedesUuid(null);
				prelim.save();
			});
		}
		if (submissions) {
			conferenceData.getSubmissions().values().forEach(submission -> {
				submission.setSistedesUuid(null);
				submission.save();
			});
		}

		return 0;
	}

}
