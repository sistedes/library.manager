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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import es.sistedes.library.manager.dspace.model.DSItem;

public class Submission extends AbstractProceedingsDocument {
	
	protected Map<String, String> formFields;

	public enum Type {
		PRELIMINARS(es.sistedes.library.manager.dspace.model.DSItem.Type.PRELIMINARS),
		ABSTRACT(es.sistedes.library.manager.dspace.model.DSItem.Type.ABSTRACT),
		PAPER(es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER);

		private DSItem.Type publicationType;
		
		Type(DSItem.Type publicationType) {
			this.publicationType = publicationType;
		}
		
		public DSItem.Type getPublicationType() {
			return publicationType;
		}
	}
	
	/**
	 * @return the formFields
	 */
	public Map<String, String> getFormFields() {
		return formFields;
	}

	/**
	 * @param formFields the formFields to set
	 */
	public void setFormFields(Map<String, String> formFields) {
		this.formFields = formFields;
	}

	/**
	 * Extracts a {@link List} of keywords from a new-line-separated list of
	 * keywords in a single {@link String}. Additionally, the keywords are
	 * capitalized in title case
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> extractKeywordsList(String str) {
		return Arrays.asList(str.split("\\s*\\n\\s*")).stream().map(s -> WordUtils.capitalize(s)).collect(Collectors.toList());
	}

	/**
	 * Extract a {@link Map} with the custom form field defined in the EasyChair
	 * submission form, where the key if the form field name, and the value the
	 * value of the custom form field
	 * 
	 * @param str
	 * @return
	 */
	public static Map<String, String> extractFormFields(String str) {
		Map<String, String> result = new HashMap<>();
		Arrays.asList(str.split("\\s*\\n\\s*")).forEach(s -> {
			Matcher matcher = Pattern.compile("\\((?<fieldname>.*)\\)\\s*(?<fieldvalue>.*)").matcher(s);
			if (matcher.matches()) {
				result.put(matcher.group("fieldname"), matcher.group("fieldvalue"));
			}
		});
		return result;
	}
}
