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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import es.sistedes.library.manager.HandleGenerator;

public class Track extends AbstractProceedingsElement {

	protected String acronym;

	protected String name;
	
	protected SortedSet<Integer> submissions = new TreeSet<>();

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

	/**
	 * Generates a new {@link Track} with the given <code>acronym</code> and for the
	 * given <code>year</code> that serves as a template to be later manually
	 * customized
	 * 
	 * @param acronym
	 * @param year
	 * @return
	 */
	public static Track createTemplate(String acronym, int year) {
		Track tracks = new Track();
		tracks.setId(1);
		tracks.setAcronym("ST");
		tracks.setName("Categoría de Ejemplo");
		tracks.getSubmissions().add(0);
		HandleGenerator.generateHandle(tracks, acronym, year).ifPresent(tracks::setSistedesHandle);
		return tracks;
	}

	/**
	 * Utility class to save all {@link Track}s information in a single file as an
	 * {@link AbstractProceedingsElement}
	 * 
	 * @author agomez
	 *
	 */
	public static class TracksIndex extends AbstractProceedingsElement {

		private Map<Integer, Track> tracks = new HashMap<>();

		private TracksIndex() {
		}
		
		private TracksIndex(Track track) {
			this.tracks.put(track.getId(), track);
		}
		
		private TracksIndex(Map<Integer, Track> tracks) {
			this.tracks.putAll(tracks);
		}

		/**
		 * Static method factory
		 * 
		 * @return
		 */
		public static TracksIndex from(Track track) {
			return new TracksIndex(track);
		}

		/**
		 * Static method factory
		 * 
		 * @param tracks
		 * @return
		 */
		public static TracksIndex from(Map<Integer, Track> tracks) {
			return new TracksIndex(tracks);
		}

		/**
		 * @return the tracks
		 */
		public Map<Integer, Track> getTracks() {
			return tracks;
		}

	}
}
