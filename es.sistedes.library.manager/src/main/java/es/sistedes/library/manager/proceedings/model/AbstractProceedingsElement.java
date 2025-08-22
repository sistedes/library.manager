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

package es.sistedes.library.manager.proceedings.model;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public abstract class AbstractProceedingsElement {

	private static final Logger logger = LoggerFactory.getLogger(AbstractProceedingsElement.class);
	
	@JsonIgnore
	protected File file;
	
	protected String sistedesUuid;
	
	protected Integer id;
	
	protected String sistedesHandle;

	protected String internalHandle;
	
	protected String abstract_;

	/**
	 * @return the sistedesUuid
	 */
	public String getSistedesUuid() {
		return sistedesUuid;
	}

	/**
	 * @param sistedesUuid the sistedesUuid to set
	 */
	public void setSistedesUuid(String sistedesUuid) {
		this.sistedesUuid = sistedesUuid;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the Sistedes handle
	 */
	public String getSistedesHandle() {
		return sistedesHandle;
	}

	/**
	 * @param handle the Sistedes handle to set
	 */
	public void setSistedesHandle(String sistedesHandle) {
		this.sistedesHandle = sistedesHandle;
	}

	/**
	 * @return the internalHandle
	 */
	public String getInternalHandle() {
		return internalHandle;
	}

	/**
	 * @param internalHandle the internalHandle to set
	 */
	public void setInternalHandle(String internalHandle) {
		this.internalHandle = internalHandle;
	}

	/**
	 * @return the abstract
	 */
	public String getAbstract() {
		return abstract_;
	}

	/**
	 * @param abstract_ the abstract to set
	 */
	public void setAbstract(String abstract_) {
		this.abstract_ = abstract_;
	}

	@JsonIgnore
	public File getFile() {
		return file;
	}
	
	@JsonIgnore
	protected void setFile(File file) {
		this.file = file;
	}

	public void save() {
		if (file == null) {
			throw new RuntimeException(MessageFormat.format("Proceedings element ''{0}'' does not have a file name", this.toString()));
		}
		JsonMapper mapper = JsonMapper.builder().build();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
		try {
			mapper.writeValue(file, this);
		} catch (IOException e) {
			logger.error(MessageFormat.format("Unable to write ''{0}''! ({1})", file, e.getLocalizedMessage()));
		}
	}
}
