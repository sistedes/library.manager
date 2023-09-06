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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import es.sistedes.library.manager.excel.SheetReader.RowReader;

public class SheetReader implements Iterable<RowReader> {

	private Sheet sheet;
	private Map<String, Integer> columnsMap = new HashMap<>();

	public SheetReader(Sheet sheet) {
		this.sheet = sheet;
		this.columnsMap = getColumnsMap(sheet);
	}

	private static Map<String, Integer> getColumnsMap(Sheet sheet) {
		Map<String, Integer> columns = new HashMap<>();
		int first = sheet.getFirstRowNum();
		if (first >= 0) {
			sheet.getRow(first).cellIterator().forEachRemaining(cell -> columns.put(cell.getStringCellValue(), cell.getColumnIndex()));
		}
		return columns;
	}

	public Collection<String> getHeaders() {
		return columnsMap.keySet();
	}

	@Override
	public Iterator<RowReader> iterator() {
		return new RowReaderIterator(sheet.iterator());
	}

	public class RowReader {
		private Row row;

		private RowReader(Row row) {
			this.row = row;
		}

		@SuppressWarnings("unchecked")
		public <T> Optional<T> get(String column, Class<T> type) {
			if (columnsMap.containsKey(column)) {
				Cell cell = row.getCell(columnsMap.get(column));
				if (cell != null) {
					switch (cell.getCellType()) {
					case NUMERIC:
						return (Optional<T>) Optional.ofNullable(cell.getNumericCellValue());
					case STRING:
						return (Optional<T>) Optional.ofNullable(cell.getStringCellValue());
					case BOOLEAN:
						return (Optional<T>) Optional.ofNullable(cell.getBooleanCellValue());
					case BLANK:
						return Optional.empty();
					default:
						throw new RuntimeException(MessageFormat.format("Unexpected cell type ''{0}'' at ''{1}@{2}''",
								cell.getCellType(), cell.getAddress(), cell.getSheet().getSheetName()));
					}
				}
			}
			return Optional.empty();
		}
	}

	private class RowReaderIterator implements Iterator<RowReader> {

		private final Iterator<Row> delegate;

		private RowReaderIterator(Iterator<Row> delegate) {
			this.delegate = delegate;
			// Explicitly skip the first row, since it contains the headers
			delegate.next();
		}

		@Override
		public RowReader next() {
			return new RowReader(delegate.next());
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public void remove() {
			delegate.remove();
		}
	}
}
