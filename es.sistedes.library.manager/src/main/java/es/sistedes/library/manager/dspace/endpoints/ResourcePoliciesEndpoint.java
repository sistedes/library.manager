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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;
import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.DSResourcePolicy;

public class ResourcePoliciesEndpoint extends AbstractHateoas {

	@JsonProperty("_embedded")
	protected SearchResourcePoliciciesResponseEmbedded embedded;

	protected static class SearchResourcePoliciciesResponseEmbedded extends AbstractPageableResponse {
		
		@JsonProperty
		protected List<DSResourcePolicy> resourcepolicies;
		
		public List<DSResourcePolicy> getAll() {
			if (page.getTotalPages() > 1) {
				throw new UnsupportedOperationException("Results with more than one page are not yet supported");
			}
			return Collections.unmodifiableList(resourcepolicies);
		}
		public Optional<DSResourcePolicy> getFirst() {
			return resourcepolicies.stream().findFirst();
		}
	};

	public List<DSResourcePolicy> getResourcePoliciesFor(String uuid) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("uuid", uuid);
		// @formatter:off
		return DSpaceConnectionManager
					.buildClient(getSelfUri())
					.post()
					.uri((uriBuilder) -> uriBuilder.pathSegment("search", "resource").queryParam("uuid", uuid).build())
					.retrieve()
					.bodyToMono(SearchResourcePoliciciesResponseEmbedded.class)
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
				.bodyToMono(SearchResourcePoliciciesResponseEmbedded.class)
				.block();
		// @formatter:on
	}
}
