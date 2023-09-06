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

package es.sistedes.library.manager.excel;

import java.text.MessageFormat;
import java.util.NoSuchElementException;

public class NoSuchColumnException extends NoSuchElementException {

	private static final long serialVersionUID = 1L;

	public NoSuchColumnException() {
	}
	
	public NoSuchColumnException(String column) {
		super(MessageFormat.format("Unable to find a column named ''{0}''", column));
	}
}
