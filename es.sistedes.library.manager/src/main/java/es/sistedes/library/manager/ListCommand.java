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
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "list", 
		description = "Generates different listings of the conference data.")
// @formatter:on
class ListCommand implements Callable<Integer> {

//	private static final Logger logger = LoggerFactory.getLogger(ListCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "FILE", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	@Option(names = { "-t", "--paper-titles" }, description = "List all the titles of the papers.")
	private boolean paperTitles = false;
	
	@Option(names = { "-n", "--authors-with-different-names" }, description = "List the authors that have more than one different name in his/her signature.")
	private boolean authorsWithDifferentNames = false;

	@Option(names = { "-e",
			"--authors-with-different-emails" }, description = "List the authors that have more than one different e-mail in his/her signature.")
	private boolean authorsWithDifferentEmails = false;

	private ConferenceData conferenceData;

	@Override
	public Integer call() throws Exception {
		conferenceData = new ConferenceData(editionFile);

		if (paperTitles) listPaperTitles(conferenceData);
		if (authorsWithDifferentNames) listAuthorsWithDifferentNames(conferenceData);
		if (authorsWithDifferentEmails) listAuthorsWithDifferentEmails(conferenceData);
		
		// Return success
		return 0;
	}
	
	public static void listPaperTitles(ConferenceData conferenceData) {
		conferenceData.getSubmissions().values().stream().forEach(submission-> {
			System.out.println(MessageFormat.format("Submission ''{0,number,#}'' has title: {1}", submission.getId(), submission.getTitle()));
		});
	}

	public static void listAuthorsWithDifferentNames(ConferenceData conferenceData) {
		conferenceData.getAuthors().values().stream().forEach(author -> {
			ListValuedMap<String, Integer> signatures = new ArrayListValuedHashMap<>();
			author.getSignatures().stream().forEach(signature -> signatures.putAll(signature.getFullName(), signature.getSubmissions()));
			if (signatures.asMap().size() > 1) {
				System.out.println(MessageFormat.format("Author ''{0}'' has {1} different names: {2}", author.getId(), signatures.asMap().size(),
						signatures.asMap().entrySet().stream().map(e -> e.getKey()).collect(Collectors.joining("; "))));
			}
		});
	}

	public static void listAuthorsWithDifferentEmails(ConferenceData conferenceData) {
		conferenceData.getAuthors().values().stream().forEach(author -> {
			ListValuedMap<String, Integer> emails = new ArrayListValuedHashMap<>();
			author.getSignatures().stream().forEach(signature -> emails.putAll(StringUtils.toRootLowerCase(signature.getEmail()), signature.getSubmissions()));
			if (emails.asMap().size() > 1) {
				System.out.println(MessageFormat.format("Author ''{0}'' has {1} different e-mails: {2}", author.getId(), emails.asMap().size(),
						emails.asMap().entrySet().stream().map(e -> e.getKey()).collect(Collectors.joining("; "))));
			}
		});
	}
}
