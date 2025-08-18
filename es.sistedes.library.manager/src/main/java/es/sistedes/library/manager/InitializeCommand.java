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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import es.sistedes.library.manager.proceedings.model.EasyChairImporter;
import es.sistedes.library.manager.proceedings.model.Submission;
import es.sistedes.library.manager.proceedings.model.Submission.Type;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "init", 
		description = "Initializes the JSON files required to generate the proceedings of a Sistedes conference from EasyChair data.")
// @formatter:on
class InitializeCommand implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(InitializeCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-a", "--acronym" }, paramLabel = "ACRONYM", required = true,
			description = "Acronym of the conference to be prepared.")
	private String acronym;
	
	private int year;
	@Option(names = { "-y", "--year" }, paramLabel = "YEAR", required = true,
			description = "Year of the edition to be prepared.")
	private void setYear(int year) {
		if (year < 2000 || year > 2099) {
			throw new ParameterException(spec.commandLine(), 
					MessageFormat.format("Invalid value ''{0}'' for option '--year': valid years range from 2000 to 2099.", year));
        }
        this.year = year;
	}

	@Option(names = { "-P", "--prefix" }, paramLabel = "PREFIX", required = true, description = "Handle prefix.")
	private String prefix;
	
	@Option(names = { "-x", "--xslx" }, paramLabel = "FILE", required = true, 
			description = "XSLX file as downloaded from the EasyChair 'Conference data download' menu.")
	private File xslxFile;

	@Option(names = { "-i", "--input" }, paramLabel = "DIR", defaultValue = "",
			description = "Input directory where the source PDF files must be looked for.")
	private File inputDir;

	@Option(names = { "-o", "--output" }, paramLabel = "DIR", defaultValue = "",
			description = "Ouput directory where the generated conference files should be placed.")
	private File outputDir;
	
	@Option(names = { "-p", "--pattern" }, paramLabel = "PATTERN", defaultValue = "{acronym}_{year}_paper_{id}.pdf",
			description = "Pattern describing the names of the submission files. {acronym} {year} and {id} will be substituted by the corresponding values. "
					+ "Default value is {acronym}_{year}_paper_{id}.pdf.")
	private String pattern;

	@Option(names = { "-F", "--force" }, 
			description = "Force execution, even if submission files are overwritten.")
	private boolean force = false;
	

	@Override
	public Integer call() throws Exception {
		logger.info(MessageFormat.format("Importing EasyChair data from ''{0}''", xslxFile));
		ConferenceData conferenceData = new EasyChairImporter(xslxFile, inputDir, outputDir, prefix, acronym, year, pattern, force).getData();
		
		for (Submission submission : conferenceData.getSubmissions().values()) {
			setSubmissionType(submission);
		}
		
		logger.info(MessageFormat.format("Saving conference data to ''{0}''", outputDir));
		conferenceData.save(force);

		// Return success
		return 0;
	}


	private void setSubmissionType(Submission submission) throws IOException {
		File submissionFile = new File(inputDir, EasyChairImporter.getSourceSubmissionFileName(submission, acronym, year, pattern));
		PDDocument document = null;
		try {
			document = PDDocument.load(submissionFile);
		} catch (FileNotFoundException e) {
			logger.error(MessageFormat.format("Submission ''{0}'' has no file!", submissionFile));
			return;
		}
		int pages = document.getNumberOfPages();
		logger.info(MessageFormat.format("Submission ''{0}'' has the following custom fields: {1}", submissionFile, Arrays.asList(submission.getFormFields()).toString()));
		if (pages == 1) {
			submission.setType(Type.ABSTRACT);
		} else if (document.getNumberOfPages() < 4) {
			System.out.print(MessageFormat.format("Submission ''{0}'' has ''{1}'' pages. Is it an ABSTRACT (A) or a PAPER (P)? ", submissionFile, pages));
			Toolkit.getDefaultToolkit().beep();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			Type type = null;
			try {
				do {
					System.out.print("[A/P]: ");
					switch (reader.readLine()) {
					case "A":
					case "a":
						type = Type.ABSTRACT;
						break;
					case "P":
					case "p":
						type = Type.PAPER;
						break;
					}
				} while (type == null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			submission.setType(type);
		} else {
			submission.setType(Type.PAPER);
		}
		logger.info(
				MessageFormat.format("Submission ''{0}'' has ''{1}'' page(s) and has been marked as {2}", submissionFile, pages, submission.getType()));
	}
}
