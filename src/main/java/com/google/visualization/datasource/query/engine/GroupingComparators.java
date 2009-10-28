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

import com.google.common.collect.Ordering;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.query.AggregationColumn;

import java.util.Comparator;
import java.util.List;

/**
 * Contains several comparators that are used by the grouping and pivoting
 * mechanism.
 *
 * @author Yonatan B.Y.
 */
/*package*/ class GroupingComparators {

  /**
   * Compares a list of values "lexicographically", i.e., if l1 = (x1...xn) and
   * l2 = (y1...ym) then compare(l1, l2) will be compare(xi,yi) when i =
   * argmin_k(compare(xk, yk) != 0). The result is 0 if no such k exists and
   * both lists are of the same size. If the lists are not of the same size and no
   * such k exists, then the longer list is considered to be greater.
   */
  public static final Comparator<List<Value>> VALUE_LIST_COMPARATOR =
      new Comparator<List<Value>>() {
        public int compare(List<Value> l1, List<Value> l2) {
          int i;
          int localCompare;
          for (i = 0; i < Math.min(l1.size(), l2.size()); i++) {
            localCompare = l1.get(i).compareTo(l2.get(i));
            if (localCompare != 0) {
              return localCompare;
            }
          }

          // l1 is not over.
          if (i < l1.size()) {
            localCompare = 1;
          } else if (i < l2.size()) { // l2 is not over.
            localCompare = -1;
          } else {// both lists of the same size.
            localCompare = 0;
          }
          return localCompare;
        }
      };

  /**
   * Compares RowTitles by comparing their list of values "lexicographically",
   * i.e. by using VALUE_LIST_COMPARATOR on them.
   */
  public static final Comparator<RowTitle> ROW_TITLE_COMPARATOR =
      new Comparator<RowTitle>() {
        public int compare(RowTitle col1, RowTitle col2) {
          return VALUE_LIST_COMPARATOR.compare(col1.values, col2.values);
        }
      };

  /**
   * Return a comparator that compares ColumnTitles by first comparing their
   * lists of (pivot) values "lexicographically" (by using
   * VALUE_LIST_COMPARATOR) and if these are equal, compares the
   * ColumnAggregation within the title, by simply finding their index in the
   * given list, and comparing the indices, i.e., two ColumnTitles that have
   * the same pivot values, will compare according to which ColumnTitle's
   * ColumnAggregation comes first in the given list. If a ColumnTitle's
   * ColumnAggregation isn't in the given list, then it shouldn't be
   * compared using this comparator and a runtime exception will be thrown.
   *
   * @param columnAggregations The list that orders the ColumnAggregations.
   *
   * @return The comparator.
   */
  public static Comparator<ColumnTitle> getColumnTitleDynamicComparator(
      List<AggregationColumn> columnAggregations) {
    return new ColumnTitleDynamicComparator(columnAggregations);
  }

  /**
   * Private constructor for utility class.
   */
  private GroupingComparators() {}

  /**
   * A comparator that compares {@link ColumnTitle}s and is parameterized by a
   * list of {@link com.google.visualization.datasource.query.AggregationColumn}s.
   * <p>
   * It compares ColumnTitles by first comparing their
   * lists of (pivot) values "lexicographically" (by using
   * {@code VALUE_LIST_COMPARATOR}) and if these are equal, compares the
   * ColumnAggregation within the title, by simply finding their index
   * in the given list, and comparing the indices, i.e., two ColumnTitles that
   * have the same pivot values, will compare according to which ColumnTitle's
   * ColumnAggregation comes first in the given list. If a ColumnTitle's
   * ColumnAggregation isn't in the given list, then it shouldn't be
   * compared using this comparator and a runtime exception will be thrown.
   */
  private static class ColumnTitleDynamicComparator
      implements Comparator<ColumnTitle> {

    /**
     * A comparator for the ColumnAggregations.
     */
    private Comparator<AggregationColumn> aggregationsComparator;

    /**
     * Creates a new instance of this class with the ordering of the
     * ColumnAggregations is in the given list.
     *
     * @param aggregations The list that gives order to the ColumnAggregations.
     */
    public ColumnTitleDynamicComparator(List<AggregationColumn> aggregations) {
      aggregationsComparator = Ordering.explicit(aggregations);
    }

    /**
     * Compares the ColumnTitles according to the logic described in the
     * description of this class.
     *
     * @param col1 The first ColumnTitle
     * @param col2 The second ColumnTitle
     *
     * @return The compare result.
     */
    public int compare(ColumnTitle col1, ColumnTitle col2) {
      int listCompare = VALUE_LIST_COMPARATOR.compare(col1.getValues(),
          col2.getValues());
      if (listCompare != 0) {
        return listCompare;
      }
      return aggregationsComparator.compare(col1.aggregation, col2.aggregation);
    }
  }
}
