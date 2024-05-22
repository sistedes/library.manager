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

package es.sistedes.library.manager.dspace.model;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.endpoints.AuthnEndpoint;
import es.sistedes.library.manager.dspace.endpoints.AuthzEndpoint;
import es.sistedes.library.manager.dspace.endpoints.BundlesEndpoint;
import es.sistedes.library.manager.dspace.endpoints.CollectionsEndpoint;
import es.sistedes.library.manager.dspace.endpoints.CommunitiesEndpoint;
import es.sistedes.library.manager.dspace.endpoints.DiscoverEndpoint;
import es.sistedes.library.manager.dspace.endpoints.ItemsEndpoint;
import es.sistedes.library.manager.dspace.endpoints.RelationshipTypesEndpoint;
import es.sistedes.library.manager.dspace.endpoints.RelationshipsEndpoint;

public class DSRoot extends AbstractHateoas {

	public static DSRoot create(URI rootUri) {
		return DSpaceConnectionManager.buildClient().get().uri(rootUri).retrieve().bodyToMono(DSRoot.class).block();
	}

	public AuthnEndpoint getAuthnEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("authn").get()).retrieve().bodyToMono(AuthnEndpoint.class).block();
	}
	
	public AuthzEndpoint getAuthzEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("authz").get()).retrieve().bodyToMono(AuthzEndpoint.class).block();
	}
	
	public DiscoverEndpoint getDiscoverEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("discover").get()).retrieve().bodyToMono(DiscoverEndpoint.class).block();
	}
	
	public ItemsEndpoint getItemsEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("items").get()).retrieve().bodyToMono(ItemsEndpoint.class).block();
	}

	public CommunitiesEndpoint getCommunitiesEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("communities").get()).retrieve().bodyToMono(CommunitiesEndpoint.class).block();
	}
	
	public CollectionsEndpoint getCollectionsEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("collections").get()).retrieve().bodyToMono(CollectionsEndpoint.class).block();
	}

	public RelationshipsEndpoint getRelationshipsEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("relationships").get()).retrieve().bodyToMono(RelationshipsEndpoint.class).block();
	}
	
	public RelationshipTypesEndpoint getRelationshipTypesEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("relationshiptypes").get()).retrieve().bodyToMono(RelationshipTypesEndpoint.class).block();
	}
	
	public BundlesEndpoint getBundlesEndpoint() {
		// The bundles endpoint cannot be GETted, thus, we create a dummy instance with the "self" link for convenience purposes
		return new BundlesEndpoint(getLinkUri("bundles").get());
	}
	
	public Optional<DSAuthor> searchAuthor(String query) {
		return getDiscoverEndpoint().getSearchEndpoint().getSearchObjectsEndpoint().newAuthorQuery(query).getQueryResults().getFirst();
	}

	public Collection<DSAuthor> searchAuthors(String query) {
		return getDiscoverEndpoint().getSearchEndpoint().getSearchObjectsEndpoint().newAuthorQuery(query).getQueryResults().getAll();
	}
	
}
