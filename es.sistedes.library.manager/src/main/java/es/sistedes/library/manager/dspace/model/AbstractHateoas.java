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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractHateoas {
	
	@JsonProperty(value = "_links", access = JsonProperty.Access.WRITE_ONLY)
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	protected HashMap<String, List<Link>> links;
	
	@JsonIgnore
	public Optional<URI> getLinkUri(String name) {
		try {
			return Optional.of(links.get(name).get(0).getHref().toURI());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@JsonIgnore
	public URI getSelfUri() {
		return getLinkUri("self").get();
	}
}
