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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;
import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.DSResourcePolicy;
import es.sistedes.library.manager.dspace.model.Link;

public class ResourcePoliciesEndpoint extends AbstractHateoas {

	protected static class SearchResourcePoliciciesResponse extends AbstractPageableResponse {
		@JsonProperty("_embedded")
		protected SearchResourcePoliciciesResponseEmbedded embedded;
	
		protected static class SearchResourcePoliciciesResponseEmbedded  {
			
			@JsonProperty
			protected List<DSResourcePolicy> resourcepolicies;
			
			public List<DSResourcePolicy> getAll() {
				return Collections.unmodifiableList(resourcepolicies);
			}
		};
		public List<DSResourcePolicy> getAll() {
			return Collections.unmodifiableList(embedded.getAll());
		}
	};

	public ResourcePoliciesEndpoint(URI selfUri) {
		try {
			links = new HashMap<>();
			Link selfLink = new Link(selfUri.toURL());
			links.put("self", Arrays.asList(selfLink));
		} catch (MalformedURLException e) {
			throw new RuntimeException("Unable to create the a 'resourcepolicies' endpoint instance from " + selfUri);
		}
	}
	
	public List<DSResourcePolicy> getResourcePoliciesFor(String uuid) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("uuid", uuid);
		// @formatter:off
		return DSpaceConnectionManager
					.buildClient(getSelfUri())
					.get()
					.uri((uriBuilder) -> uriBuilder.pathSegment("search", "resource").queryParam("uuid", uuid).build())
					.retrieve()
					.bodyToMono(SearchResourcePoliciciesResponse.class)
					.block()
					.getAll();
		// @formatter:on
	}
	
	public void deleteResourcePolicy(Integer policyId) {
		// @formatter:off
		DSpaceConnectionManager
				.buildClient(getSelfUri())
				.delete()
				.uri((uriBuilder) -> uriBuilder.pathSegment("{policyId}").build(policyId))
				.retrieve()
				.toBodilessEntity()
				.block();
		// @formatter:on
	}
}
