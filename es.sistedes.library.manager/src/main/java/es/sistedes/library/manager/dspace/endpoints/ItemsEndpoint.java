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

import java.util.Optional;

import org.springframework.http.MediaType;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.DSAuthor;
import es.sistedes.library.manager.dspace.model.DSCollection;
import es.sistedes.library.manager.dspace.model.DSPublication;

public class ItemsEndpoint extends AbstractPageableResponse {

	public Optional<DSAuthor> getAuthor(String uuid) {
		return getItem(DSAuthor.class, uuid);
	}
	
	public Optional<DSPublication> getPublication(String uuid) {
		return getItem(DSPublication.class, uuid);
	}

	protected <T> Optional<T> getItem(Class<T> clazz, String uuid) {
		T item = DSpaceConnectionManager.buildClient(getSelfUri()).get().uri(uriBuilder -> uriBuilder.pathSegment(uuid).build()).retrieve()
				.bodyToMono(clazz).block();
		return Optional.ofNullable(item);
	}
	
	public DSAuthor createAuthor(DSAuthor author, DSCollection owningCollection) {
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getSelfUri())
				.post()
				.uri((uriBuilder) -> uriBuilder.queryParam("owningCollection", owningCollection.getUuid()).build())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(author)
				.retrieve()
				.bodyToMono(DSAuthor.class)
				.block();
		// @formatter:on
	}
	
	public DSPublication createPublication(DSPublication publication, DSCollection owningCollection) {
		// @formatter:off
		return DSpaceConnectionManager
			.buildClient(getSelfUri())
			.post()
			.uri((uriBuilder) -> uriBuilder.queryParam("owningCollection", owningCollection.getUuid()).build())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(publication)
			.retrieve()
			.bodyToMono(DSPublication.class)
			.block();
		// @formatter:on
	}
}
