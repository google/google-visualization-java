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

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.ScalarFunctionColumn;

import org.apache.commons.lang.text.StrBuilder;

import java.util.List;

/**
 * A "title" (identification) of a scalar function column in a pivoted and/or grouped table.
 * What identifies a column is the list of values, one value from each of the
 * pivot columns, and a ScalarFunctionColumn denoting the original column (of the original table
 * columns).
 *
 * @author Liron L.
 */
/* package */ class ScalarFunctionColumnTitle {

  /**
   * The list of values from the pivot-by columns.
   */
  private List<Value> values;

  /**
   * The scalar function column denoting this column.
   */
  public ScalarFunctionColumn scalarFunctionColumn;

  /**
   * When creating the id of the column, this is used as a separator between the values
   * in the pivot-by columns.
   */
  public static final String PIVOT_COLUMNS_SEPARATOR = ",";

  /**
   * When creating the id of the column, this is used as a separator between the list of
   * values and the ScalarFunctionColumn.
   */
  public static final String PIVOT_SCALAR_FUNCTION_SEPARATOR = " ";

  /**
   * Creates a new instance of this class, with the given values and
   * ScalarFunctionColumn.
   *
   * @param values The list of values.
   * @param column The ScalarFunctionColumn.
   */
  public ScalarFunctionColumnTitle(List<Value> values,
      ScalarFunctionColumn column) {
    this.values = values;
    this.scalarFunctionColumn = column;
  }

  /**
   * Returns the values in this ColumnTitle.
   *
   * @return The values in this ColumnTitle.
   */
  public List<Value> getValues() {
    return values;
  }

  /**
   * Creates a ColumnDescription for this column.
   *
   * @param originalTable The original table, from which the original column description
   *     of the column is taken.
   *
   * @return The ColumnDescription for this column.
   */
  public ColumnDescription createColumnDescription(DataTable originalTable) {
    String columnId = createIdPivotPrefix() + scalarFunctionColumn.getId();
    ValueType type = scalarFunctionColumn.getValueType(originalTable);
    String label = createLabelPivotPart() + " " +
        getColumnDescriptionLabel(originalTable, scalarFunctionColumn);
    ColumnDescription result = new ColumnDescription(columnId, type, label);

    return result;
  }

  /**
   * Creates a prefix for a pivoted column id, containing all the values of the
   * pivoted columns. Returns the empty string if no pivoting was used.
   *
   * @return A prefix for the pivoted column id.
   */
  private String createIdPivotPrefix() {
    if (!isPivot()) {
      return "";
    }
    return new StrBuilder().appendWithSeparators(values, PIVOT_COLUMNS_SEPARATOR)
        .append(PIVOT_SCALAR_FUNCTION_SEPARATOR).toString();
  }

  /**
   * Creates a prefix for a pivoted column label, containing all the values of
   * the pivoted columns. Returns an empty string if no pivoting was used.
   *
   * @return A prefix for the pivoted column label.
   */
  private String createLabelPivotPart() {
    if (!isPivot()) {
      return "";
    }
    return new StrBuilder().appendWithSeparators(values, PIVOT_COLUMNS_SEPARATOR).toString();
  }

  /**
   * Returns true if pivoting was used.
   *
   * @return True if pivoting was used.
   */
  private boolean isPivot() {
    return (!values.isEmpty());
  }

  /**
   * Returns the label of the column description.
   *
   * @param originalTable The original table, from which the original column description of the
   *     column is taken.
   *
   * @return The label of the column description.
   */
  public static String getColumnDescriptionLabel(DataTable originalTable, AbstractColumn column) {
    StringBuilder label = new StringBuilder();
    if (originalTable.containsColumn(column.getId())) {
      label.append(originalTable.getColumnDescription(column.getId()).getLabel());
    } else {
      if (column instanceof AggregationColumn) {
        AggregationColumn aggColumn = (AggregationColumn) column;
        label.append(aggColumn.getAggregationType().getCode()).append(" ").
            append(originalTable.getColumnDescription(
                aggColumn.getAggregatedColumn().getId()).getLabel());
      } else {
        ScalarFunctionColumn scalarFunctionColumn = (ScalarFunctionColumn) column;
        List<AbstractColumn> columns = scalarFunctionColumn.getColumns();
        label.append(scalarFunctionColumn.getFunction().getFunctionName()).append("(");
        for (AbstractColumn abstractColumn : columns) {
          label.append(getColumnDescriptionLabel(originalTable, abstractColumn));
        }
        label.append(")");
      }
    }
    return label.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ScalarFunctionColumnTitle) {
      ScalarFunctionColumnTitle other = (ScalarFunctionColumnTitle) o;
      return (values.equals(other.values)
           && scalarFunctionColumn.equals(other.scalarFunctionColumn));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 31;
    if (scalarFunctionColumn != null) {
      result += scalarFunctionColumn.hashCode();
    }
    result *= 31;
    if (values != null) {
      result += values.hashCode();
    }
    return result;
  }
}
