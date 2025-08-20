/*******************************************************************************
* Copyright (c) 2025 Sistedes
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

public class DSProcess extends AbstractHateoas {

	public static class DSParameter {

		@JsonProperty
		protected String name;

		@JsonProperty
		protected String value;

		public DSParameter(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	@JsonProperty
	protected Integer processId;

	@JsonProperty
	protected String userId;

	@JsonProperty
	protected String processStatus;

	public Integer getProcessId() {
		return processId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getProcessStatus() {
		return processStatus;
	}

}
