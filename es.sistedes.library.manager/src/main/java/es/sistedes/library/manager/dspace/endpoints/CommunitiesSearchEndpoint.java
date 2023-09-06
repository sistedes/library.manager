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

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.model.AbstractHateoas;

public class CommunitiesSearchEndpoint extends AbstractHateoas {

	public CommunitiesSearchTopEndpoint getSearchTopEndpoint() {
		return DSpaceConnectionManager.buildClient().get().uri(getLinkUri("top").get()).retrieve().bodyToMono(CommunitiesSearchTopEndpoint.class).block();
	}
}
