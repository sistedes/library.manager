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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.sistedes.library.manager.DSpaceConnectionManager;
import es.sistedes.library.manager.dspace.endpoints.ItemBundlesEndpoint;
import es.sistedes.library.manager.dspace.endpoints.ItemRelationshipsEndpoint;

public class DSItem extends AbstractHateoas {

	protected static final String OTHER_BUNDLE = "OTHER";
	protected static final String ORIGINAL_BUNDLE = "ORIGINAL";


	public enum Type {
		AUTHOR("Autor"), PAPER("Artículo"), ABSTRACT("Resumen"), BULLETIN("Boletín"), SEMINAR("Seminario"), PRELIMINARS("Preliminares");

		private String name;

		Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	@JsonProperty
	protected String id;

	@JsonProperty
	protected String uuid;

	@JsonProperty
	protected String name;

	@JsonProperty
	protected String handle;

	@JsonProperty
	protected Metadata metadata = new Metadata();

	@JsonProperty
	protected Boolean inArchive = true;

	@JsonProperty
	protected Boolean discoverable = true;

	@JsonProperty
	protected Boolean withdrawn = false;

	@JsonProperty
	protected Date lastModified;

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
	 * @return the inArchive
	 */
	public Boolean getInArchive() {
		return inArchive;
	}

	/**
	 * @param inArchive the inArchive to set
	 */
	public void setInArchive(Boolean inArchive) {
		this.inArchive = inArchive;
	}

	/**
	 * @return the discoverable
	 */
	public Boolean getDiscoverable() {
		return discoverable;
	}

	/**
	 * @param discoverable the discoverable to set
	 */
	public void setDiscoverable(Boolean discoverable) {
		this.discoverable = discoverable;
	}

	/**
	 * @return the withdrawn
	 */
	public Boolean getWithdrawn() {
		return withdrawn;
	}

	/**
	 * @param withdrawn the withdrawn to set
	 */
	public void setWithdrawn(Boolean withdrawn) {
		this.withdrawn = withdrawn;
	}

	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return the links
	 */
	public Map<String, List<Link>> getLinks() {
		return links;
	}

	public void save() {
		ObjectMapper mapper = DSpaceConnectionManager.createDefaultObjectMapper();
		DSAuthor dsAuthor = DSpaceConnectionManager.buildClient().put().uri(getSelfUri()).bodyValue(this).retrieve().bodyToMono(DSAuthor.class).block();
		try {
			mapper.readerForUpdating(this).readValue(mapper.writeValueAsString(dsAuthor));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public DSBundle createOtherBundle() {
		return createBundle(new DSBundle(OTHER_BUNDLE));
	}
	
	public Optional<DSBundle> getOtherBundle() {
		return getBundle(OTHER_BUNDLE);
	}
	
	public DSBundle createOriginalBundle() {
		return createBundle(new DSBundle(ORIGINAL_BUNDLE));
	}

	public Optional<DSBundle> getOriginalBundle() {
		return getBundle(ORIGINAL_BUNDLE);
	}

	public List<DSRelationship> getRelationships() {
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getLinkUri("relationships").get())
				.get()
				.retrieve()
				.bodyToMono(ItemRelationshipsEndpoint.class)
				.block().getAll();
		// @formatter:on
	}

	protected Optional<DSBundle> getBundle(String name) {
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getLinkUri("bundles").get())
				.get()
				.retrieve()
				.bodyToMono(ItemBundlesEndpoint.class)
				.block().getBundle(name);
		// @formatter:on
	}

	protected DSBundle createBundle(DSBundle bundle) {
		// @formatter:off
		return DSpaceConnectionManager
				.buildClient(getLinkUri("bundles").get())
				.post()
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(bundle)
				.retrieve()
				.bodyToMono(DSBundle.class)
				.block();
		// @formatter:on
	}
}
