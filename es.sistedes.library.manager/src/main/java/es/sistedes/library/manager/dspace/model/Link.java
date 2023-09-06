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

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {
	
	@JsonProperty
	protected URL href;
	
	@JsonProperty
	protected Boolean templated;
	
	public Link() {
	}
	
	public Link(URL url) {
		href = url;
	}
	
	public URL getHref() {
		return href;
	}
}
