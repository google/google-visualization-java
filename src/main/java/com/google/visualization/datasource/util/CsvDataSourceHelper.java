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

package com.google.visualization.datasource.util;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.ValueFormatter;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import au.com.bytecode.opencsv.CSVReader;
import com.ibm.icu.util.ULocale;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class with static utility methods that are specific for building a
 * data source based on CSV (Comma Separated Values) files.
 * The main functionality is taking a CSV and generating a data table.
 *
 * @author Nimrod T.
 */
public class CsvDataSourceHelper {

  /**
   * Private constructor. All methods are static.
   */
  private CsvDataSourceHelper() {}

  /**
   * @see #read(java.io.Reader, java.util.List, Boolean, ULocale)
   */
  public static DataTable read(Reader reader, List<ColumnDescription> columnDescriptions,
      Boolean headerRow)
      throws IOException, CsvDataSourceException {
    return read(reader, columnDescriptions, headerRow, null);
  }

  /**
   * Translates a CSV formatted input into a data table representation.
   * The reader parameter should point to a CSV file.
   *
   * @param reader The CSV input Reader from which to read.
   * @param columnDescriptions The column descriptions.
   *     If columnDescriptions is null, then it is assumed that all 
   *     values are strings, and the number of the columns is equal
   *     to the number of the columns in the first line of the reader.
   *     If headerRow is set to true, and the columnDescriptions does not
   *     contain the labels of the columns, then the headerRow values are
   *     used as the column labels.
   * @param headerRow True if there is an header row.
   *     In that case, the first line of the csv is taken as the header row.
   * @param locale An optional locale in which to parse the input csv file.
   *     If null, uses the default from {@code LocaleUtil#getDefaultLocale}.
   *
   * @return A data table with the values populated from the CSV file.
   *
   * @throws IOException In case of error reading from the reader.
   * @throws CsvDataSourceException In case of specific csv error.
   */
  public static DataTable read(Reader reader, List<ColumnDescription> columnDescriptions,
      Boolean headerRow, ULocale locale)
      throws IOException, CsvDataSourceException {
    DataTable dataTable = new DataTable();

    if (reader == null) {
      return dataTable;
    }

    CSVReader csvReader = new CSVReader(reader);
    Map<ValueType, ValueFormatter> defaultFormatters =
        ValueFormatter.createDefaultFormatters(locale);

    // Parse the CSV.
    String[] line;
    boolean firstLine = true;
    while ((line = csvReader.readNext()) != null) {
      // Being lenient about newlines.
      // The reader reads them as lines with
      // one element ("").
      if ((line.length == 1) && (line[0].equals(""))) {
        // This is a new line.
        continue;
      }

      if ((columnDescriptions != null) && (line.length != columnDescriptions.size())) {
        throw new CsvDataSourceException(
            ReasonType.INTERNAL_ERROR,
            "Wrong number of columns in the data.");
      }
      if (firstLine) {
        // If columnDescriptions is null, create it.
        if (columnDescriptions == null) {
          columnDescriptions = Lists.newArrayList();
        }

        List<ColumnDescription> tempColumnDescriptions = new ArrayList<ColumnDescription>();

        // Fill the defaults in the tableDescription.
        // This allows easy-to-write table descriptions
        // (or even null table descriptions).
        for (int i = 0; i < line.length; i++) {
          ColumnDescription tempColumnDescription =
              (columnDescriptions.isEmpty() || columnDescriptions.get(i) == null)
              ? null
              : columnDescriptions.get(i);

          String id =
              ((tempColumnDescription == null) || (tempColumnDescription.getId() == null))
              ? "Col" + (i) : tempColumnDescription.getId();
          ValueType type =
              ((tempColumnDescription == null) || (tempColumnDescription.getType() == null))
              ? ValueType.TEXT : tempColumnDescription.getType();
          String label =
              ((tempColumnDescription == null) || (tempColumnDescription.getLabel() == null))
              ? "Column" + i : tempColumnDescription.getLabel();
          String pattern =
              ((tempColumnDescription == null) || (tempColumnDescription.getPattern() == null))
              ? "" : tempColumnDescription.getPattern();

          tempColumnDescription = new ColumnDescription(id, type, label);
          tempColumnDescription.setPattern(pattern);
          tempColumnDescriptions.add(tempColumnDescription);
        }

        // Deal with header rows.
        if (headerRow) {
          for (int i = 0; i < line.length; i++) {
            String string = line[i];
            if (string == null) {
              tempColumnDescriptions.get(i).setLabel("");
            } else {
              tempColumnDescriptions.get(i).setLabel(line[i].trim());
            }
          }
        }

        columnDescriptions = tempColumnDescriptions;
        dataTable = new DataTable();
        dataTable.addColumns(columnDescriptions);
      }
      if (!(firstLine && headerRow)) {
        // Need to parse the first line as a regular row.
        TableRow tableRow = new TableRow();
        for (int i = 0; i < line.length; i++) {
          ColumnDescription columnDescription = columnDescriptions.get(i);
          ValueType valueType = columnDescription.getType();
          String string = line[i];
          if (string != null) {
            string = string.trim();
          }
          String pattern = columnDescription.getPattern();
          ValueFormatter valueFormatter;
          if (pattern == null || pattern.equals("")) {
            valueFormatter = defaultFormatters.get(valueType);
          } else {
            valueFormatter = ValueFormatter.createFromPattern(valueType, pattern, locale);
          }
          Value value = valueFormatter.parse(string);
          
          tableRow.addCell(value);
        }
        try {
          dataTable.addRow(tableRow);
        } catch (TypeMismatchException e) {
          // Should not happen as we always use the correct types (null if we cannot parse),
          // and we check the size of the lines.
        }
      }

      firstLine = false;
    }

    return dataTable;
  }

  /**
   * Returns a Reader for the url.
   * Given a specific url, returns a Reader for that url,
   * so that the CSV data source will be able to read the CSV file from that url.
   *
   * @param url The url to get a Reader for.
   *
   * @return A Reader for the given url.
   *
   * @throws DataSourceException In case of a problem reading from the url.
   */
  public static Reader getCsvUrlReader(String url) throws DataSourceException {
    // Get a Reader.
    Reader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
    } catch (MalformedURLException e) {
      throw new DataSourceException(
          ReasonType.INVALID_REQUEST,
          "url is malformed: " + url);
    } catch (IOException e) {
      throw new DataSourceException(
          ReasonType.INVALID_REQUEST,
          "Couldn't read csv file from url: " + url);
    }
    return reader;
  }

  /**
   * Returns a Reader for the file.
   * Given a specific file, returns a Reader to that file,
   * so that the CSV data source will be able to read the CSV file from that file.
   *
   * @param file The file to get a Reader for.
   *
   * @return A Reader for the given file.
   *
   * @throws DataSourceException In case of a problem reading from the file.
   */
  public static Reader getCsvFileReader(String file) throws DataSourceException {
    // Get a Reader.
    Reader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      throw new DataSourceException(
          ReasonType.INVALID_REQUEST,
          "Couldn't read csv file from: " + file);
    }
    return reader;
  }
}
