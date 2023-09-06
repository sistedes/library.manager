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
import es.sistedes.library.manager.dspace.model.DSCommunity;

public class CommunitiesEndpoint extends AbstractHateoas {

	public CommunitiesSearchEndpoint getSearchEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("search").get()).retrieve().bodyToMono(CommunitiesSearchEndpoint.class).block();
	}

	public static CommunityCollectionsEndpoint getCollectionsEndpoint(DSCommunity community) {
		return DSpaceConnectionManager.buildClient(community.getSelfUri()).get().uri(uriBuilder -> uriBuilder.pathSegment("collections").build()).retrieve()
				.bodyToMono(CommunityCollectionsEndpoint.class).block();
	}

	public DSCommunity getCommunity(String uuid) {
		return DSpaceConnectionManager.buildClient(getSelfUri()).get().uri(uriBuilder -> uriBuilder.pathSegment(uuid).build()).retrieve()
				.bodyToMono(DSCommunity.class).block();
	}

	public DSCommunity createTopCommunity(DSCommunity topCommunity) {
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getSelfUri())
				.post()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(topCommunity)
				.retrieve()
				.bodyToMono(DSCommunity.class)
				.block();
		// @formatter:on
	}

	public DSCommunity createSubCommunity(DSCommunity subCommunity, DSCommunity parent) {
		// @formatter:off
		return DSpaceConnectionManager
			.buildClient(getSelfUri())
			.post()
			.uri((uriBuilder) -> uriBuilder.queryParam("parent", parent.getUuid()).build())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(subCommunity)
			.retrieve()
			.bodyToMono(DSCommunity.class)
			.block();
		// @formatter:on
	}
}
