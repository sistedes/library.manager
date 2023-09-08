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

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.sistedes.library.manager.dspace.endpoints.CommunitiesEndpoint;
import es.sistedes.library.manager.proceedings.model.Edition;

public class DSCommunity extends AbstractHateoas {

	@JsonProperty
	protected String id;

	@JsonProperty
	protected String uuid;

	@JsonProperty
	protected String name;

	@JsonProperty
	protected String handle;

	@JsonProperty
	protected String type;

	@JsonProperty
	protected Metadata metadata = new Metadata();

	@JsonProperty
	protected Integer archivedItemsCount;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	/**
	 * @return the handle
	 */
	public String getHandle() {
		return handle;
	}

	/**
	 * @param handle the handle to set
	 */
	public void setHandle(String handle) {
		this.handle = handle;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the archivedItemsCount
	 */
	public Integer getArchivedItemsCount() {
		return archivedItemsCount;
	}

	/**
	 * @param archivedItemsCount the archivedItemsCount to set
	 */
	public void setArchivedItemsCount(Integer archivedItemsCount) {
		this.archivedItemsCount = archivedItemsCount;
	}

	/**
	 * @return the internal URL handle
	 */
	public String getHandleUrl() {
		return "https://hdl.handle.net/" + handle;
	}

	/**
	 * @return the Sistedes identifier (i.e., Sistedes Handle)
	 */
	public String getSistedesIdentifier() {
		return this.metadata.getSistedesIdentifier();
	}
	
	/**
	 * @return the Sistedes Handle URL
	 */
	public String getSistedesUri() {
		return "https://hdl.handle.net/" + this.metadata.getSistedesIdentifier();
	}
	
	/**
	 * @param id the Sistedes id
	 */
	public void setSistedesIdentifier(String id) {
		this.metadata.setSistedesIdentifier(id);
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.metadata.getTitle();
	}
	
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.metadata.setTitle(title);
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.metadata.getDescription();
	}
	
	/**
	 * @param html the description to set
	 */
	public void setDescription(String html) {
		this.metadata.setDescription(html);
	}
	
	/**
	 * @return the abstract
	 */
	public String getAbstract() {
		return this.metadata.getAbstract();
	}
	
	/**
	 * @param abstract the abstract to set
	 */
	public void setAbstract(String _abstract) {
		this.metadata.setAbstract(_abstract);
	}
	
	/**
	 * @return the creation date
	 */
	public Date getDate() {
		return this.metadata.getDate();
	}
	
	/**
	 * @param date the creation date
	 */
	public void setDate(Date date) {
		this.metadata.setDate(date);
	}
	
	public List<DSCollection> getCollections() {
		return CommunitiesEndpoint.getCollectionsEndpoint(this).getAll();
	}

	public static DSCommunity createSubCommunity(DSRoot dsRoot, DSCommunity topCommunity, Edition edition) {
		DSCommunity result = new DSCommunity();
		result.setTitle(MessageFormat.format("{0} ({1})", edition.getShortName(), edition.getLocation()));
		result.setSistedesIdentifier(edition.getSistedesHandle());
		result.setAbstract(edition.getAbstract());
		result.setDescription("<p>" + edition.getAbstract().replaceAll("\\R", "</p><p>") + "</p>");
		result.setDate(Calendar.getInstance().getTime());
		return dsRoot.getCommunitiesEndpoint().createSubCommunity(result, topCommunity);
	}
}
