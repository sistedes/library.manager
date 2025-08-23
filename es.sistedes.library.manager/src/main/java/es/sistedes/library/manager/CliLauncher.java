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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

public class CliLauncher {

	// @formatter:off
	@Command(name = "java -jar <this-file.jar>",
			description = "Manage the Sistedes Digital Library.",
			mixinStandardHelpOptions = true, version = "20250819.0",
			subcommands = { InitializeCommand.class, SyncAuthorsCommand.class, ValidateCommand.class, 
					PublishEditionCommand.class, ListCommand.class, CurateAuthorsCommand.class,
					DiscardUuidsCommand.class, SplitCommand.class, })
	// @formatter:on
	static class Commands {
		@Spec
		CommandSpec spec;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int exitCode = new CommandLine(new Commands()).execute(args);
		System.exit(exitCode);
	}

}
