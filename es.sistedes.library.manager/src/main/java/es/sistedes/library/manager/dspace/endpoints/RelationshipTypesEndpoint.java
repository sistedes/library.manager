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

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.RelationshipType;

public class RelationshipTypesEndpoint extends AbstractPageableResponse {

	@JsonProperty("_embedded")
	protected RelationshipTypesEmbedded embedded;
	
	protected static class RelationshipTypesEmbedded {
		@JsonProperty
		protected List<RelationshipType> relationshiptypes;
	}

	public List<RelationshipType> getAll() {
		if (page.getTotalPages() > 1) {
			throw new UnsupportedOperationException("Results with more than one page are not yet supported");
		}
		return Collections.unmodifiableList(embedded.relationshiptypes);
	}

	public RelationshipType getIsAuthorOfPaperRelationship() {
		return getAll().stream().filter(r -> r.getLeftwardType().equals("isAuthorOfPaper")).findAny().orElseThrow();
	}

	public RelationshipType getIsAuthorOfAbstractRelationship() {
		return getAll().stream().filter(r -> r.getLeftwardType().equals("isAuthorOfAbstract")).findAny().orElseThrow();
	}
}
