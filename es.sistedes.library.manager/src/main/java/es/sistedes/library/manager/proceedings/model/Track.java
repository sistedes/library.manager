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

import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.sistedes.library.manager.HandleGenerator;

public class Track extends AbstractProceedingsElement {

	protected String acronym;

	protected String name;
	
	protected SortedSet<Integer> submissions = new TreeSet<>();
	
	@JsonIgnore
	private TracksIndex index;

	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}

	/**
	 * @param acronym the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
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
	 * @return the submissions
	 */
	public SortedSet<Integer> getSubmissions() {
		return submissions;
	}

	public void setIndex(TracksIndex index) {
		this.index = index;
	}
	
	/**
	 * Generates a new {@link Track} with the given <code>acronym</code> and for the
	 * given <code>year</code> that serves as a template to be later manually
	 * customized
	 * 
	 * @param acronym
	 * @param year
	 * @return
	 */
	public static Track createTemplate(String prefix, String acronym, int year) {
		Track tracks = new Track();
		tracks.setId(1);
		tracks.setAcronym("ST");
		tracks.setName("Categoría de Ejemplo");
		tracks.getSubmissions().add(0);
		HandleGenerator.generateHandle(tracks, prefix, acronym, year).ifPresent(tracks::setSistedesHandle);
		return tracks;
	}
	
	@Override
	public void save() {
		index.save();
	}
}
