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

import org.springframework.core.io.AbstractResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.DSpaceConnectionManager;

public class DSBundle extends AbstractHateoas {

	@JsonProperty
	protected String uuid;
	
	@JsonProperty
	protected String name;
	
	public DSBundle() {
	}
		
	public DSBundle(String name) {
		this.name = name;
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public DSBitstream createBitstreamFrom(AbstractResource resource, String bitstreamName) {
		DSBitstream bitstream = new DSBitstream();
		bitstream.setName(bitstreamName);
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("file", resource);
		builder.part("properties", bitstream);
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getLinkUri("bitstreams").get())
				.post()
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(BodyInserters.fromMultipartData(builder.build()))
				.retrieve()
				.bodyToMono(DSBitstream.class)
				.block();
		// @formatter:on
	}
}
