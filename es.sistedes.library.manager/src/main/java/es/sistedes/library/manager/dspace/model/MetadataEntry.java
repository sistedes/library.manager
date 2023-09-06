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

public class MetadataEntry {

	@JsonProperty
	protected String value;
	
	@JsonProperty
	protected String language;
    
	@JsonProperty
	protected String authority;
    
	@JsonProperty
	protected Integer confidence;

	@JsonProperty
    protected Integer place;
    
	public MetadataEntry() {
		this.confidence = -1;
	}
	
    public MetadataEntry(String value) {
    	this();
    	this.value = value;
    }
    
    public MetadataEntry(String value, Integer place) {
    	this(value);
    	this.place = place;
    }
    
    public String getValue() {
		return value;
	}

    public void setValue(String value) {
    	this.value = value;
    }
    
    public Integer getPlace() {
		return place;
	}
    
    public void setPlace(Integer place) {
		this.place = place;
	}
}
