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

public class Page {
	
	@JsonProperty
	protected Integer size;
	
	@JsonProperty
	protected Integer totalElements;
	
	@JsonProperty
	protected Integer totalPages;

	@JsonProperty
	protected Integer number;

	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * @return the totalElements
	 */
	public Integer getTotalElements() {
		return totalElements;
	}

	/**
	 * @return the totalPages
	 */
	public Integer getTotalPages() {
		return totalPages;
	}

	/**
	 * @return the number
	 */
	public Integer getNumber() {
		return number;
	}
	
	
}
