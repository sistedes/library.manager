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

package es.sistedes.library.manager;

import java.util.Optional;

import es.sistedes.library.manager.proceedings.model.AbstractProceedingsElement;
import es.sistedes.library.manager.proceedings.model.Edition;
import es.sistedes.library.manager.proceedings.model.Preliminaries;
import es.sistedes.library.manager.proceedings.model.Submission;
import es.sistedes.library.manager.proceedings.model.Track;

public final class HandleGenerator {

	private HandleGenerator() {};
	
	/**
	 * Generates a handle for the given {@link AbstractProceedingsElement} using the
	 * give conference <code>acronym</code>, <code>year</code>, and the global
	 * Sistedes handle prefix
	 * 
	 * @param elt
	 * @param acronym
	 * @param year
	 * @return
	 */
	public static Optional<String> generateHandle(AbstractProceedingsElement elt, String prefix, String acronym, int year) {
		if (elt instanceof Edition) {
			return Optional.of(String.format("%s/%s/%d", prefix, acronym, year));
		} else if (elt instanceof Track) {
			return Optional.of(String.format("%s/%s/%d/%s", prefix, acronym, year, ((Track) elt).getAcronym()));
		} else if (elt instanceof Preliminaries) {
			return Optional.of(String.format("%s/%s/%d/PRELIMINARES/%s", prefix, acronym, year, elt.getId()));
		} else if (elt instanceof Submission) {
			return Optional.of(String.format("%s/%s/%d/%s", prefix, acronym, year, elt.getId()));
		} else {
			return Optional.empty();
		}
	}
}
