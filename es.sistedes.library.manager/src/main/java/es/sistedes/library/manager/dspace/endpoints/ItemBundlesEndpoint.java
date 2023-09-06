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

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.dspace.model.AbstractPageableResponse;
import es.sistedes.library.manager.dspace.model.DSBundle;

public class ItemBundlesEndpoint extends AbstractPageableResponse {

	@JsonProperty("_embedded")
	protected ItemBundlesEmbedded embedded;
	
	protected static class ItemBundlesEmbedded {
		@JsonProperty
		protected List<DSBundle> bundles;
	}

	public List<DSBundle> getAll() {
		if (page.getTotalPages() > 1) {
			throw new UnsupportedOperationException("Results with more than one page are not yet supported");
		}
		return Collections.unmodifiableList(embedded.bundles);
	}

	public Optional<DSBundle> getBundle(String name) {
		return getAll().stream().filter(b -> b.getName().equals(name)).findAny();
	}

}
