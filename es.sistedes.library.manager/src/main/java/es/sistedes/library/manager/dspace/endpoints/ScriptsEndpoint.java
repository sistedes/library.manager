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

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;
import es.sistedes.library.manager.dspace.model.DSProcess;
import es.sistedes.library.manager.dspace.model.DSProcess.DSParameter;

public class ScriptsEndpoint extends AbstractHateoas {

	public DSProcess executeScript(String script, List<DSParameter> parameters) {
		
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("properties", parameters);

		// @formatter:off
		return DSpaceConnectionManager
			.buildClient(getSelfUri())
			.post()
			.uri((uriBuilder) -> uriBuilder.pathSegment(script, "processes").build())
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(builder.build()))
			.retrieve()
			.bodyToMono(DSProcess.class)
			.block();
		// @formatter:on
	}
}
