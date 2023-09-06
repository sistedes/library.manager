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
import org.springframework.web.reactive.function.BodyInserters;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.DSItem;
import es.sistedes.library.manager.dspace.model.RelationshipType;

public class RelationshipsEndpoint extends AbstractPageableResponse {

	public String createRelationship(RelationshipType type, DSItem elt1, DSItem elt2) {
		String data = elt1.getSelfUri() + " \n " + elt2.getSelfUri();
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getSelfUri())
				.post()
				.uri(uriBuilder -> uriBuilder.queryParam("relationshipType", type.getId()).build())
				.contentType(MediaType.valueOf("text/uri-list"))
				.body(BodyInserters.fromValue(data))
				.retrieve()
				.bodyToMono(String.class)
				.block();
		// @formatter:on
	}
}
