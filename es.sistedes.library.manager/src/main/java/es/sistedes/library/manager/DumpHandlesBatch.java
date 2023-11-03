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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.AbstractProceedingsElement;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import es.sistedes.library.manager.proceedings.model.Edition;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "dump-handles-batch", 
		description = "Dumps a set of Handle commands that can be run as a batch file to (optionally delete) and create all the Handles in the specified edition.")
				
// @formatter:on
class DumpHandlesBatch implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(DumpHandlesBatch.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "FILE", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	private PrintStream output = System.out;
	@Option(names = { "-o", "--output" }, paramLabel = "FILE", required = false, description = "If specified, the Handle commands will be saved in FILE rather than shown in stdout.")
	private void setYear(String output) throws FileNotFoundException {
		this.output = new PrintStream(new File(output));
	}

	@Option(names = { "-x", "--prefix" }, paramLabel = "PREFIX", required = true, description = "Handle prefix.")
	private String prefix;

	@Option(names = { "-d", "--delete" }, description = "Also issue an initial DELETE command in order to CREATE the Handles from scratch.")
	private boolean delete = false;

	private ConferenceData conferenceData;

	@Override
	public Integer call() throws Exception {
		conferenceData = new ConferenceData(editionFile);

		logger.info("Validating conference data...");
		if (!ValidateCommand.validateProceedingsEltsHaveHandles(conferenceData)) {
			System.err.println("ERROR: Some elements do not have a Handle. Execute the 'publish' command first.");
			mainCmd.spec.subcommands().get("publish").getCommandSpec().commandLine().usage(System.err);
			return 1;
		}

		if (!ValidateCommand.validateNotDuplicateHandles(conferenceData)) {
			System.err.println("ERROR: Some Handles are duplicated. Check the Sistedes Handles first.");
			return 1;
		}

		List<AbstractProceedingsElement> elements = conferenceData.getAllProceedingsElements();

		if (delete) {
			elements.stream().forEach(elt -> {
				printDeleteHandle(elt.getSistedesHandle());
				if (elt instanceof Edition ed) {
					printDeleteHandle(ed.getPreliminariesSistedesHandle());
				}
			});
		}
		elements.stream().forEach(elt -> {
			printCreateHandle(prefix, elt.getSistedesHandle(), elt.getInternalHandle());
			if (elt instanceof Edition ed) {
				printCreateHandle(prefix, ed.getPreliminariesSistedesHandle(), ed.getPreliminariesInternalHandle());
			}
		});

		// Return success
		return 0;
	}

	private void printDeleteHandle(String sistedesHandle) {
		output.println("DELETE " + sistedesHandle);
		output.println();
	}

	private void printCreateHandle(String prefix, String sistedesHandle, String internalHandle) {
		output.println("CREATE " + sistedesHandle);
		output.println("100 HS_ADMIN 86400 1110 ADMIN 300:111111111111:0.NA/" + prefix);
		output.println("1 URL 86400 1110 UTF8 https://hdl.handle.net/" + internalHandle);
		output.println();
	}
}
