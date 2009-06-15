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

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests for CsvRenderer.
 *
 * @author Nimrod T.
 */
public class CsvRendererTest extends TestCase {

  DataTable testData;

  List<TableRow> rows;

  List<String> colIds;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    colIds = Lists.newArrayList();

    testData = new DataTable();
    ColumnDescription c0 = new ColumnDescription("col0", ValueType.TEXT, "label0");
    ColumnDescription c1 = new ColumnDescription("col1", ValueType.NUMBER, "label1");
    ColumnDescription c2 = new ColumnDescription("col2", ValueType.BOOLEAN, "label2");
    ColumnDescription c3 = new ColumnDescription("col3", ValueType.DATE, "label3");
    ColumnDescription c4 = new ColumnDescription("col4", ValueType.TIMEOFDAY, "label4");
    ColumnDescription c5 = new ColumnDescription("col5", ValueType.DATETIME, "label5");

    testData.addColumn(c0);
    testData.addColumn(c1);
    testData.addColumn(c2);
    testData.addColumn(c3);
    testData.addColumn(c4);
    testData.addColumn(c5);

    colIds.add("col0");
    colIds.add("col1");
    colIds.add("col2");
    colIds.add("col3");
    colIds.add("col4");
    colIds.add("col5");

    rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell("aaa"));
    row.addCell(new TableCell(222));
    row.addCell(new TableCell(false));
    row.addCell(new TableCell(new DateValue(2001, 10, 14)));
    row.addCell(new TableCell(new TimeOfDayValue(12, 11, 13, 14)));
    row.addCell(new TableCell(new DateTimeValue(2000, 10, 1, 1, 10, 23, 432)));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("ccc"));
    row.addCell(new TableCell(111));
    row.addCell(new TableCell(true));
    row.addCell(new TableCell(new DateValue(2001, 1, 14)));
    row.addCell(new TableCell(new TimeOfDayValue(12, 30, 13, 14)));
    row.addCell(new TableCell(new DateTimeValue(1000, 11, 1, 1, 10, 23, 432)));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("bbb"));
    row.addCell(new TableCell(3));
    row.addCell(new TableCell(true));
    row.addCell(new TableCell(new DateValue(2012, 2, 14)));
    row.addCell(new TableCell(new TimeOfDayValue(12, 11, 3, 14)));
    row.addCell(new TableCell(new DateTimeValue(2000, 1, 1, 1, 10, 31, 4)));
    rows.add(row);

    row = new TableRow();
    row.addCell("ddd");
    row.addCell(222);
    row.addCell(false);
    row.addCell(new DateValue(1997, 5, 5));
    row.addCell(new TimeOfDayValue(12, 15, 15, 14));
    row.addCell(new DateTimeValue(3100, 1, 2, 15, 15, 1, 0));
    rows.add(row);

    testData.addRows(rows);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    testData = null;
    rows = null;
  }

  public void testEmptyDataTableToCsv() {
    DataTable dataTable = new DataTable();
    assertEquals("", CsvRenderer.renderDataTable(dataTable, null, null));
    assertEquals("", CsvRenderer.renderDataTable(dataTable, null, ","));
    assertEquals("", CsvRenderer.renderDataTable(dataTable, null, "\t"));
  }

  public void testSimpleDataTableToCsv() throws DataSourceException {
    colIds = Lists.newArrayList();

    testData = new DataTable();
    ColumnDescription c0 = new ColumnDescription("A", ValueType.TEXT, "col0");
    ColumnDescription c1 = new ColumnDescription("B", ValueType.NUMBER, "col1");
    ColumnDescription c2 = new ColumnDescription("C", ValueType.BOOLEAN, "col2");

    testData.addColumn(c0);
    testData.addColumn(c1);
    testData.addColumn(c2);

    rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell("aaa"));
    row.addCell(new TableCell(new NumberValue(222), "222"));
    row.addCell(new TableCell(false));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(""));
    row.addCell(new TableCell(NumberValue.getNullValue()));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(new TextValue("bbb"), "bb@@b"));
    row.addCell(new TableCell(333));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("ddd"));
    row.addCell(new TableCell(222));
    row.addCell(new TableCell(false));
    rows.add(row);

    testData.addRows(rows);

    assertEquals(
        "\"col0\",\"col1\",\"col2\"\n" +
        "\"aaa\",222,false\n" +
        "\"\",null,true\n" +
        "\"bb@@b\",333,true\n" +
        "\"ddd\",222,false\n",
        CsvRenderer.renderDataTable(testData, null, null).toString());
    assertEquals(
        "\"col0\",\"col1\",\"col2\"\n" +
        "\"aaa\",222,false\n" +
        "\"\",null,true\n" +
        "\"bb@@b\",333,true\n" +
        "\"ddd\",222,false\n",
        CsvRenderer.renderDataTable(testData, null, ",").toString());
    assertEquals(
        "\"col0\"\t\"col1\"\t\"col2\"\n" +
        "\"aaa\"\t222\tfalse\n" +
        "\"\"\tnull\ttrue\n" +
        "\"bb@@b\"\t333\ttrue\n" +
        "\"ddd\"\t222\tfalse\n",
        CsvRenderer.renderDataTable(testData, null, "\t").toString());
  }

  public void testCustomPropertiesToCsv() throws DataSourceException {
    colIds = Lists.newArrayList();

    testData = new DataTable();
    ColumnDescription c0 = new ColumnDescription("A", ValueType.TEXT, "col0");
    ColumnDescription c1 = new ColumnDescription("B", ValueType.NUMBER, "col1");
    c1.setCustomProperty("arak", "elit");

    testData.addColumn(c0);
    testData.addColumn(c1);

    rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell("aaa"));
    row.addCell(new TableCell(new NumberValue(222), "2a2b2"));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(""));
    row.addCell(new TableCell(NumberValue.getNullValue()));
    rows.add(row);
    row.setCustomProperty("sensi", "puff");

    testData.addRows(rows);

    testData.getRow(0).getCell(0).setCustomProperty("a", "b");

    assertEquals(
        "\"col0\",\"col1\"\n\"aaa\",2a2b2\n\"\",null\n",
        CsvRenderer.renderDataTable(testData, null, null).toString());
    assertEquals(
        "\"col0\",\"col1\"\n\"aaa\",2a2b2\n\"\",null\n",
        CsvRenderer.renderDataTable(testData, null, ",").toString());
    assertEquals(
        "\"col0\"\t\"col1\"\n\"aaa\"\t2a2b2\n\"\"\tnull\n",
        CsvRenderer.renderDataTable(testData, null, "\t").toString());

    testData.setCustomProperty("brandy", "cognac");
    assertEquals(
        "\"col0\",\"col1\"\n\"aaa\",2a2b2\n\"\",null\n",
        CsvRenderer.renderDataTable(testData, null, null).toString());
    assertEquals(
        "\"col0\",\"col1\"\n\"aaa\",2a2b2\n\"\",null\n",
        CsvRenderer.renderDataTable(testData, null, ",").toString());
    assertEquals(
        "\"col0\"\t\"col1\"\n\"aaa\"\t2a2b2\n\"\"\tnull\n",
        CsvRenderer.renderDataTable(testData, null, "\t").toString());
  }

  public void testRenderError() {
    ResponseStatus responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.INVALID_REQUEST, "but why? why?");
    assertEquals(
        "\"Error: Invalid request. but why? why?\"",
        CsvRenderer.renderCsvError(responseStatus));

    responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.NOT_SUPPORTED, "Cannot do dat!");
    assertEquals(
        "\"Error: Operation not supported. Cannot do dat!\"",
        CsvRenderer.renderCsvError(responseStatus));

    responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.NOT_SUPPORTED, "Cannot \"do\" that, too late!");
    assertEquals(
        "\"Error: Operation not supported. Cannot \"\"do\"\" that, too late!\"",
        CsvRenderer.renderCsvError(responseStatus));
  }
}
