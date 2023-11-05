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

import java.text.MessageFormat;
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

	public enum Type {
		JISBD_FULL("completo", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		JISBD_RELEVANT("relevante", es.sistedes.library.manager.dspace.model.DSItem.Type.ABSTRACT),
		JISBD_SHORT("corto", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		JISBD_TOOL("herramienta", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		JCIS_PUBLISHED("Published", es.sistedes.library.manager.dspace.model.DSItem.Type.ABSTRACT),
		JCIS_LONG("Long papers", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		JCIS_SHORT("Short papers", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		PROLE_ORIGINAL("1", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		PROLE_TUTORIAL("2", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		PROLE_TOOL("3", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		PROLE_PROGRESS("4", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER),
		PROLE_RELEVANT("5", es.sistedes.library.manager.dspace.model.DSItem.Type.ABSTRACT),
		PRELIMINARES("preliminares", es.sistedes.library.manager.dspace.model.DSItem.Type.PRELIMINARS), 
		GENERIC_ABSTRACT("resumen", es.sistedes.library.manager.dspace.model.DSItem.Type.ABSTRACT),
		GENERIC_PAPER("artículo", es.sistedes.library.manager.dspace.model.DSItem.Type.PAPER);
		
		private String submissionTypeName;
		private DSItem.Type publicationType;
		
		Type(String submissionType, DSItem.Type publicationType) {
			this.submissionTypeName = submissionType;
			this.publicationType = publicationType;
		}
		
		public static Type from(String submissionTypeName) {
			for (Type type : values()) {
				if (type.submissionTypeName.equals(submissionTypeName)) {
					return type;
				}
			}
			throw new IllegalArgumentException(MessageFormat.format("No enum constant value ''{0}'' in {1}", submissionTypeName, Type.class.getCanonicalName()));
		}
		
		public DSItem.Type getPublicationType() {
			return publicationType;
		}
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
