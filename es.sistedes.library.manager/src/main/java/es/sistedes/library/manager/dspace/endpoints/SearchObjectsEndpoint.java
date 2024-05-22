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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;
import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.DSAuthor;
import es.sistedes.library.manager.dspace.model.DSItem;

public class SearchObjectsEndpoint<T> extends AbstractHateoas {

	@JsonProperty("_embedded")
	protected SearchObjectsResponseEmbedded<T> embedded;

	protected static class SearchObjectsResponseEmbedded<T> {
		
		@JsonProperty
		protected SearchResult<T> searchResult;

		public static class SearchResult<T> extends AbstractPageableResponse {

			@JsonProperty("_embedded")
			protected SearchResultEmbedded<T> embedded;

			protected static class SearchResultEmbedded<T> {
				
				@JsonProperty
				protected List<SearchResultObject<T>> objects;
				
				protected static class SearchResultObject<T> extends AbstractHateoas {
					
					@JsonProperty
					protected String highlights;
					
					@JsonProperty
					protected String type;
					
					@JsonProperty("_embedded")
					protected SearchResultObjectEmbedded<T> embedded;
					
					protected static class SearchResultObjectEmbedded<T> {
						@JsonProperty
						protected T indexableObject;
					}
				}
			};
			public Collection<T> getAll() {
				if (page.getTotalPages() > 1) {
					throw new UnsupportedOperationException("Results with more than one page are not yet supported");
				}
				return embedded.objects.stream().map(o -> o.embedded.indexableObject).collect(Collectors.toUnmodifiableList());
			}
			public Optional<T> getFirst() {
				return embedded.objects.stream().map(o -> o.embedded.indexableObject).findFirst();
			}
		}
	};
	
	public SearchObjectsEndpoint<DSAuthor> newAuthorQuery(String query) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("f.entityType", DSItem.Type.AUTHOR.getName() + ",equals");
		parameters.add("query", query.replaceAll(":", "")); // In some cases, a URL is provided instead on an email.
															// In such a case, remove the colon since the API fails to process it
		return newQuery(new ParameterizedTypeReference<SearchObjectsEndpoint<DSAuthor>>(){}, query, parameters);
	}

	protected <U> U newQuery(ParameterizedTypeReference<U> returnTypeReference, String query, MultiValueMap<String, String> parameters) {
		MultiValueMap<String, String> parameters2 = new LinkedMultiValueMap<>(parameters);
		parameters2.add("dsoType", "item");
		parameters2.add("sort", "score,DESC");
		URI uri = getSelfUri();
		// @formatter:off
		return DSpaceConnectionManager.buildClient().get()
				.uri(uriBuilder -> uriBuilder
						.scheme(uri.getScheme())
						.host(uri.getHost())
						.port(uri.getPort())
						.path(uri.getPath()).queryParams(parameters2).build())
				.retrieve().bodyToMono(returnTypeReference).block();
		// @formatter:on
	}
	
	public SearchObjectsEndpoint.SearchObjectsResponseEmbedded.SearchResult<T> getQueryResults() {
		return embedded.searchResult;
	}
}
