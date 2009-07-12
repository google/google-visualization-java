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

package com.google.visualization.datasource.render;

import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.ValueFormatter;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.ULocale;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Takes a data table and returns a csv string.
 *
 * @author Nimrod T.
 */
public class CsvRenderer {

  /**
   * Private constructor.
   */
  private CsvRenderer() {}

  /**
   * Generates a csv string representation of a data table.
   *
   * @param dataTable The data table.
   * @param locale The locale. If null, uses the default from
   *     {@code LocaleUtil#getDefaultLocale}.
   * @param separator The separator string used to delimit row values. 
   *     If the separator is {@code null}, comma is used as a separator.
   *
   * @return The char sequence with the csv string.
   */
  public static CharSequence renderDataTable(DataTable dataTable, ULocale locale,
      String separator) {
    if (separator == null) {
      separator = ",";
    }

    // Deal with empty data table.
    if (dataTable.getColumnDescriptions().isEmpty()) {
      return "";
    }

    // Deal with non-empty data table.
    StringBuilder sb = new StringBuilder();
    List<ColumnDescription> columns = dataTable.getColumnDescriptions();
    // Append column labels
    for (ColumnDescription column : columns) {
      sb.append(escapeString(column.getLabel())).append(separator);
    }

    Map<ValueType, ValueFormatter> formatters = ValueFormatter.createDefaultFormatters(locale);

    // Remove last comma.
    int length = sb.length();
    sb.replace(length - 1, length, "\n");

    // Append the data cells.
    List<TableRow> rows = dataTable.getRows();
    for (TableRow row : rows) {
      List<TableCell> cells = row.getCells();
      for (TableCell cell : cells) {
        String formattedValue = cell.getFormattedValue();
        if (formattedValue == null) {
          formattedValue = formatters.get(cell.getType()).format(cell.getValue());
        }
        if (cell.isNull()) {
          sb.append("null");
        } else {
          ValueType type = cell.getType();
          // Escape the string with quotes if its a text value or if it contains a comma.
          if (formattedValue.indexOf(',') > -1 || type.equals(ValueType.TEXT)) {
            sb.append(escapeString(formattedValue));
          } else {
            sb.append(formattedValue);
          }
        }
        sb.append(separator);
      }

      // Remove last comma.
      length = sb.length();
      sb.replace(length - 1, length, "\n");
    }
    return sb.toString();
  }

  /**
   * Escapes a string that is written to a csv file. The escaping is as follows:
   * 1) surround with ".
   * 2) double each internal ".
   *
   * @param input The input string.
   *
   * @return An escaped string.
   */
  private static String escapeString(String input) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    sb.append(StringUtils.replace(input, "\"", "\"\""));
    sb.append("\"");
    return sb.toString();
  }

  /**
   * Renders an error message.
   *
   * @param responseStatus The response status.
   * 
   * @return An error message.
   */
  public static String renderCsvError(ResponseStatus responseStatus) {
    StringBuilder sb = new StringBuilder();
    sb.append("Error: ").append(responseStatus.getReasonType().getMessageForReasonType(null));
    sb.append(". ").append(responseStatus.getDescription());
    return escapeString(sb.toString());
  }
}
