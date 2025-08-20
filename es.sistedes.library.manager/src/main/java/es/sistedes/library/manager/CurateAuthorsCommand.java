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

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.DSpaceConnectionManager.DSpaceConnection;
import es.sistedes.library.manager.dspace.model.DSProcess;
import es.sistedes.library.manager.dspace.model.DSProcess.DSParameter;
import es.sistedes.library.manager.dspace.model.DSRoot;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "curate-authors", 
		description = "Launches all the curation tasks that may be applicable to newly created "
				+ "authors in the Sistedes Digital Library. Since the process is executed "
				+ "asynchonously by DSPace, no feedback about the execution result is given. "
				+ "The DSpace UI can be used to get more feedback.")
				
// @formatter:on
class CurateAuthorsCommand implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(CurateAuthorsCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-u", "--uri" }, paramLabel = "URI", required = true, description = "URI of the root endpoint of the DSpace API.")
	private URI uri;

	@Option(names = { "-e",
	"--email" }, paramLabel = "E-MAIL", required = true, description = "E-mail of the account required to log in the Sistedes Digital Library.")
	private String email;

	@Option(names = { "-p",
	"--password" }, paramLabel = "PASSWORD", required = true, description = "Password of the account in the Sistedes Digital Library.")
	private String password;
	

	private DSpaceConnection connection;
	private DSRoot dsRoot;

	@Override
	public Integer call() throws Exception {
		connection = DSpaceConnectionManager.createConnection(uri, email, password);
		dsRoot = connection.getDsRoot();
		
		// @formatter:off
		DSProcess process = dsRoot.getScriptsEndpoint().executeScript("curate", Arrays.asList(
						new DSParameter("-t", "refreshsistedesauthortitle"), 
						new DSParameter("-i", "11705/2")));
		// @formatter:on
		
		logger.info(MessageFormat.format("Process ''curate'' created with id ''{0}'' and status ''{1}''", process.getProcessId(), process.getProcessStatus()));
		
		// Return success
		return 0;
	}
}
