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

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;
import es.sistedes.library.manager.dspace.model.DSCollection;
import es.sistedes.library.manager.dspace.model.DSCommunity;

public class CollectionsEndpoint extends AbstractHateoas {

	public DSCollection getCollection(String uuid) {
		return DSpaceConnectionManager.buildClient(getSelfUri()).get().uri(uriBuilder -> uriBuilder.pathSegment(uuid).build()).retrieve()
				.bodyToMono(DSCollection.class).block();
	}
	public DSCollection createCollection(DSCollection collection, DSCommunity parent) {
		// @formatter:off
		return DSpaceConnectionManager
			.buildClient(getSelfUri())
			.post()
			.uri((uriBuilder) -> uriBuilder.queryParam("parent", parent.getUuid()).build())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(collection)
			.retrieve()
			.bodyToMono(DSCollection.class)
			.block();
		// @formatter:on
	}
}
