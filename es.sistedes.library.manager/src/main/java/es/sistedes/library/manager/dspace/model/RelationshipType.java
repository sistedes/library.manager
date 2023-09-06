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

import com.fasterxml.jackson.annotation.JsonProperty;

public class RelationshipType extends AbstractHateoas {
	
	@JsonProperty
	protected Integer id;
	
	@JsonProperty
	protected String leftwardType;

	@JsonProperty
	protected String rightwardType;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @return the leftwardType
	 */
	public String getLeftwardType() {
		return leftwardType;
	}

	/**
	 * @return the rightwardType
	 */
	public String getRightwardType() {
		return rightwardType;
	}
	

}
