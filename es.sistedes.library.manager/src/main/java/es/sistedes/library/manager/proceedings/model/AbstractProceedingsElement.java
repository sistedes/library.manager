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

public abstract class AbstractProceedingsElement {

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
}
