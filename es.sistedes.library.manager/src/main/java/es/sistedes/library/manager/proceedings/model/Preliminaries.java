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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import es.sistedes.library.manager.HandleGenerator;
import es.sistedes.library.manager.proceedings.model.Submission.Type;

public class Preliminaries extends AbstractProceedingsDocument {
	
	public Preliminaries() {
	}
	
	public Preliminaries(File file) {
		this.file = file;
		setType(Type.PRELIMINARS);
	}

	/**
	 * Generates a new {@link Preliminaries} with the given <code>acronym</code> and for
	 * the given <code>year</code> that serves as a template to be later manually
	 * customized
	 * 
	 * @param acronym
	 * @param year
	 * @return
	 */
	public static Preliminaries createTemplate(File file, String prefix, String acronym, int year) {
		Preliminaries preliminaries = new Preliminaries(file);
		preliminaries.setId(1);
		preliminaries.setTitle("Prefacio");
		preliminaries.setAbstract("Prefacio de...");
		preliminaries.getKeywords().add("Keyword 1");
		preliminaries.getKeywords().add("Keyword 2");
		preliminaries.getSignatures().add(new Signature() {
			{
				familyName = "Doe";
				givenName = "John";
				affiliation = "University";
				country = "Spain";
				email = "nobody@example.com";
			}
		});
		HandleGenerator.generateHandle(preliminaries, prefix, acronym, year).ifPresent(preliminaries::setSistedesHandle);
		preliminaries.setFilename("prefacio.md");
		return preliminaries;
	}
	
	public static Preliminaries load(File preliminariesFile) throws StreamReadException, DatabindException, IOException {
		JsonMapper mapper = JsonMapper.builder().build();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
		Preliminaries preliminaries = mapper.readValue(preliminariesFile, Preliminaries.class);
		preliminaries.setFile(preliminariesFile);
		return preliminaries;
	}
}
