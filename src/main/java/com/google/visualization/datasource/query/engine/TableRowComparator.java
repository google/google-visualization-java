// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.visualization.datasource.query.engine;

import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.ColumnLookup;
import com.google.visualization.datasource.query.ColumnSort;
import com.google.visualization.datasource.query.QuerySort;
import com.google.visualization.datasource.query.SortOrder;
import com.ibm.icu.util.ULocale;

import java.util.Comparator;
import java.util.List;

/**
 * A comparator comparing two {@link TableRow}s according to the query's ORDER BY, i.e.,
 * {@link QuerySort}.
 *
 * @author Yoah B.D.
 */
/*package*/ class TableRowComparator implements Comparator<TableRow> {

  /**
   * The columns to order by, in sequence of importance.
   */
  private AbstractColumn[] sortColumns;

  /**
   * The sort order of the columns to order by, in sequence of importance. Will
   * be of same size as sortColumnIndex.
   */
  private SortOrder[] sortColumnOrder;

  /**
   * A value comparator.
   */
  private Comparator<Value> valueComparator;

  /**
   * The column lookup.
   */
  private ColumnLookup columnLookup;

  /**
   * Construct a new TableRowComparator.
   *
   * @param sort The ordering criteria.
   * @param locale The locale defining the order relation of text values.
   * @param lookup The column lookup.
   */
  public TableRowComparator(QuerySort sort, ULocale locale, ColumnLookup lookup) {
    valueComparator = Value.getLocalizedComparator(locale);
    columnLookup = lookup;
    List<ColumnSort> columns = sort.getSortColumns();
    sortColumns = new AbstractColumn[columns.size()];
    sortColumnOrder = new SortOrder[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      ColumnSort columnSort = columns.get(i);
      sortColumns[i] = columnSort.getColumn();
      sortColumnOrder[i] = columnSort.getOrder();
    }
  }

  /**
   * Compares two arguments for order. Returns a negative integer, zero, or
   * a positive integer if the first argument is less than, equal to, or greater
   * than the second.
   *
   * @param r1 the first row to be compared.
   * @param r2 the second row to be compared.
   *
   * @return a negative integer, zero, or a positive integer as the first
   *     argument is less than, equal to, or greater than the second.
   */
  public int compare(TableRow r1, TableRow r2) {
    for (int i = 0; i < sortColumns.length; i++) {
      AbstractColumn col = sortColumns[i];
      int cc = valueComparator.compare(col.getValue(columnLookup, r1),
          col.getValue(columnLookup, r2));
      if (cc != 0) {
        return (sortColumnOrder[i] == SortOrder.ASCENDING) ? cc  : -cc;
      }
    }
    return 0;
  }
}
