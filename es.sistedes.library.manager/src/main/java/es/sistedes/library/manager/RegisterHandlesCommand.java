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
import java.security.PrivateKey;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.AbstractProceedingsElement;
import es.sistedes.library.manager.proceedings.model.Edition;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractRequest;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.ModifyValueRequest;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.ResolutionRequest;
import net.handle.hdllib.ResolutionResponse;
import net.handle.hdllib.Util;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "register-handles", 
		description = "Registers the Sistedes Handles such that they point to the Digital Library internal Handles.")
				
// @formatter:on
class RegisterHandlesCommand implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(RegisterHandlesCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-f", "--edition-file" }, paramLabel = "FILE", required = true, description = "JSON file including the conference edition metadata.")
	private File editionFile;

	@Option(names = { "-x", "--prefix" }, paramLabel = "PREFIX", required = true, description = "Handle prefix.")
	private String prefix;

	@Option(names = { "-k",
			"--key-file" }, paramLabel = "FILE", required = true, description = "File with the secure key to authenticate in the Handle system.")
	private File keyFile;

	@Option(names = { "-p", "--password" }, paramLabel = "PASSWORD", description = "Password to unlock the key file.")
	private String password;

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

		PublicKeyAuthenticationInfo auth = getAuth(prefix, keyFile, password);

		List<AbstractProceedingsElement> elements = conferenceData.getAllProceedingsElements();
		elements.stream().forEach(elt -> {
			try {
				registerHandleMapping(elt.getSistedesHandle(), elt.getInternalHandle(), prefix, auth);
				if (elt instanceof Edition ed) {
					registerHandleMapping(ed.getPreliminariesSistedesHandle(), ed.getPreliminariesInternalHandle(), prefix, auth);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// Return success
		return 0;
	}

	private static PublicKeyAuthenticationInfo getAuth(String prefix, File keyFile, String password) throws Exception {
		byte key[] = IOUtils.toByteArray(keyFile.toURI());
		PrivateKey privkey = null;
		if (Util.requiresSecretKey(key) && StringUtils.isBlank(password)) {
			throw new Exception(MessageFormat.format("Private key in ''{0}'' requires a password", keyFile));
		}
		key = Util.decrypt(key, password.getBytes());
		privkey = Util.getPrivateKeyFromBytes(key, 0);
		return new PublicKeyAuthenticationInfo(Util.encodeString("0.NA/" + prefix), 300, privkey);
	}

	private static void registerHandleMapping(String sourceHandle, String targetHandle, String prefix, PublicKeyAuthenticationInfo auth) throws Exception {
		String targetUrl = "https://hdl.handle.net/" + targetHandle;
		logger.debug(MessageFormat.format("Creating handle ''{0}'' -> ''{1}''", sourceHandle, targetUrl));
		HandleResolver resolver = new HandleResolver();
		int timestamp = (int) (System.currentTimeMillis() / 1000);

		HandleValue urlVal = new HandleValue(1, Util.encodeString("URL"), Util.encodeString(targetUrl), HandleValue.TTL_TYPE_RELATIVE, 86400, timestamp, null,
				true, true, true, false);

		AbstractRequest request = null;
		if (handleExists(sourceHandle)) {
			request = new ModifyValueRequest(Util.encodeString(sourceHandle), urlVal, auth);
			request.authoritative = true;
		} else {
			AdminRecord adminRecord = new AdminRecord(Util.encodeString("0.NA/" + prefix), 300, true, true, true, true, true, true, true, true, true, true,
					true, true);
			HandleValue[] values = { urlVal, new HandleValue(100, Util.encodeString("HS_ADMIN"), Encoder.encodeAdminRecord(adminRecord),
					HandleValue.TTL_TYPE_RELATIVE, 86400, timestamp, null, true, true, true, false) };
			request = new CreateHandleRequest(Util.encodeString(sourceHandle), values, auth);
		}

		AbstractResponse response = resolver.processRequest(request);
		logger.info(MessageFormat.format("Created handle ''{0}'' -> ''{1}''", sourceHandle, targetUrl));
		if (response.responseCode != AbstractMessage.RC_SUCCESS) {
			throw new Exception("Unable to create / update URL for handle " + sourceHandle);
		}
	}

	private static boolean handleExists(String handle) throws HandleException {
		HandleResolver resolver = new HandleResolver();
		ResolutionRequest request = new ResolutionRequest(Util.encodeString(handle), null, null, null);
		request.authoritative = true;
		AbstractResponse response = resolver.processRequest(request);
		return Arrays.asList(((ResolutionResponse) response).getHandleValues()).stream()
				// The Sistedes Handle server uses a template (see "config.dct" in the Sistedes
				// Handle server) that automatically generates non-existing Handles on the fly.
				// Thus, in order to detect that a Handle exists (i.e., it has been explicitly
				// created registered in the database), we must check that the record does not
				// include an entry of type "SISTEDES_GENERATED" at the index 999.
				// This value is explicitly created by the template configured in the server.
				.allMatch(val -> val.getIndex() != 999 && !"SISTEDES_GENERATED".equals(val.getTypeAsString()));
	}
}
