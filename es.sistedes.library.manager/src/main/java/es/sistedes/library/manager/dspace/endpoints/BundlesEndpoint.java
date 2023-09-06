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
import java.util.HashMap;

import es.sistedes.library.manager.dspace.model.AbstractHateoas;
import es.sistedes.library.manager.dspace.model.Link;

public class BundlesEndpoint extends AbstractHateoas {

	public BundlesEndpoint(URI selfUri) {
		try {
			links = new HashMap<>();
			Link selfLink = new Link(selfUri.toURL());
			links.put("self", Arrays.asList(selfLink));
		} catch (MalformedURLException e) {
			throw new RuntimeException("Unable to create the a 'bundles' endpoint instance from " + selfUri);
		}
	}

}
