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

import java.util.ArrayList;
import java.util.List;

import es.sistedes.library.manager.proceedings.model.Submission.Type;

public abstract class AbstractProceedingsDocument extends AbstractProceedingsElement {

	protected String title;

	protected List<Signature> signatures = new ArrayList<>();

	protected List<String> keywords = new ArrayList<>();

	protected String filename;
	
	protected String license = "CC BY-NC-ND 4.0";

	protected String rightsUri = "https://creativecommons.org/licenses/by-nc-nd/4.0/";

	protected Type type;

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the signatures of the authors
	 */
	public List<Signature> getSignatures() {
		return signatures;
	}

	/**
	 * @return the keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * @return the rightsUri
	 */
	public String getRightsUri() {
		return rightsUri;
	}

	/**
	 * @param rightsUri the rightsUri to set
	 */
	public void setRightsUri(String rightsUri) {
		this.rightsUri = rightsUri;
	}

}
