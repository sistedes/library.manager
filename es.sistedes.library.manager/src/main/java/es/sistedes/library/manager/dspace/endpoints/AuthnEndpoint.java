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

package es.sistedes.library.manager.dspace.endpoints;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;

public class AuthnEndpoint extends AbstractHateoas {

	public LoginEndpoint doLogin(String email, String password) {
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("user", email);
		data.add("password", password);
		LoginEndpoint result = DSpaceConnectionManager
				.buildClient()
				.post()
				.uri(getLinkUri("login").get())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData(data))
				.retrieve()
				.bodyToMono(LoginEndpoint.class)
				.block();
		return result;
	}

	public ResponseEntity<Void> doLogout() {
		return DSpaceConnectionManager
				.buildClient()
				.post()
				.uri(getLinkUri("logout").get())
				.retrieve()
				.toBodilessEntity()
				.block();
	}

	public ResponseEntity<Void> refreshAuth() {
		return DSpaceConnectionManager
				.buildClient()
				.post()
				.uri(getLinkUri("login").get())
				.retrieve()
				.toBodilessEntity()
				.block();
	}
}
