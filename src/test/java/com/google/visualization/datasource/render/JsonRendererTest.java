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
import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.base.Warning;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests for JsonRenderer.
 *
 * @author Nimrod T.
 */
public class JsonRendererTest extends TestCase {

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

  public void testEmptyDataTableToJson() {
    DataTable dataTable = new DataTable();
    assertEquals("", JsonRenderer.renderDataTable(dataTable, false, false, true));
    assertEquals("", JsonRenderer.renderDataTable(dataTable, false, true, true));
    assertEquals("", JsonRenderer.renderDataTable(dataTable, true, false, true));
    assertEquals("", JsonRenderer.renderDataTable(dataTable, true, true, true));
  }

  public void testAppendCellJson() {
    TableCell dateCell = new TableCell(new DateValue(2009, 1, 12));
    TableCell timeofdayCell = new TableCell(new TimeOfDayValue(12, 13, 14, 15));
    TableCell datetimeCell = new TableCell(new DateTimeValue(2009, 1, 12, 12, 13, 14, 15));
    TableCell booleanCell = new TableCell(true);
    TableCell numberCell = new TableCell(12.3);
    TableCell textCell = new TableCell("aba");


    assertEquals("{\"v\":new Date(2009,1,12)}",
        JsonRenderer.appendCellJson(dateCell, new StringBuilder(),
            true, false, true).toString());
    assertEquals("{\"v\":\"Date(2009,1,12)\"}",
        JsonRenderer.appendCellJson(dateCell, new StringBuilder(),
            true, false, false).toString());
    assertEquals("{\"v\":[12,13,14,15]}",
        JsonRenderer.appendCellJson(timeofdayCell, new StringBuilder(),
            true, false, true).toString());
    assertEquals("{\"v\":new Date(2009,1,12,12,13,14)}", //no milliseconds passed
        JsonRenderer.appendCellJson(datetimeCell, new StringBuilder(),
            true, false, true).toString());
    assertEquals("{\"v\":\"Date(2009,1,12,12,13,14)\"}", //no milliseconds passed
        JsonRenderer.appendCellJson(datetimeCell, new StringBuilder(),
            true, false, false).toString());
    assertEquals("{\"v\":true}",
        JsonRenderer.appendCellJson(booleanCell, new StringBuilder(),
            true, false, true).toString());
    assertEquals("{\"v\":12.3}",
        JsonRenderer.appendCellJson(numberCell, new StringBuilder(),
            true, false, true).toString());
    assertEquals("{\"v\":\"aba\"}",
        JsonRenderer.appendCellJson(textCell, new StringBuilder(),
            true, false, true).toString());

    // No formatting still stays the same when there is no formatted value
    assertEquals("{\"v\":12.3}",
        JsonRenderer.appendCellJson(numberCell, new StringBuilder(),
            false, false, true).toString());


   dateCell = new TableCell(new DateValue(2009, 1, 12), "2009-2-12");

    // With formatting
    assertEquals("{\"v\":new Date(2009,1,12),\"f\":\"2009-2-12\"}",
        JsonRenderer.appendCellJson(dateCell, new StringBuilder(),
            true, false, true).toString());

    // Without formatting
    assertEquals("{\"v\":new Date(2009,1,12)}",
        JsonRenderer.appendCellJson(dateCell, new StringBuilder(),
            false, false, true).toString());

    TableCell nullCell = new TableCell(Value.getNullValueFromValueType(ValueType.NUMBER));

    // Null value
    assertEquals("",
        JsonRenderer.appendCellJson(nullCell, new StringBuilder(),
            true, false, true).toString());
    // isLast = true
    assertEquals("{\"v\":null}",
        JsonRenderer.appendCellJson(nullCell, new StringBuilder(),
            true, true, true).toString());
  }

  public void testAppendColumnDescriptionJson() {
    ColumnDescription columnDescription = new ColumnDescription("ID", ValueType.BOOLEAN, "LABEL");
    assertEquals("{\"id\":\"ID\",\"label\":\"LABEL\",\"type\":\"boolean\",\"pattern\":\"\"}",
        JsonRenderer.appendColumnDescriptionJson(
            columnDescription, new StringBuilder()).toString());

    columnDescription.setPattern("%%%.@@");

    assertEquals("{\"id\":\"ID\",\"label\":\"LABEL\",\"type\":\"boolean\",\"pattern\":\"%%%.@@\"}",
        JsonRenderer.appendColumnDescriptionJson(
            columnDescription, new StringBuilder()).toString());
  }


  public void testSimpleDataTableToJson() throws DataSourceException {
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
    row.addCell(new TableCell(new TextValue("bbb"), "bbb"));
    row.addCell(new TableCell(333));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("d'dd"));
    row.addCell(new TableCell(222));
    row.addCell(new TableCell(false));
    rows.add(row);

    testData.addRows(rows);
    assertEquals(
        "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
            + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\"},"
            + "{\"id\":\"C\",\"label\":\"col2\",\"type\":\"boolean\",\"pattern\":\"\"}],"
            + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"222\"},{\"v\":false}]},"
            + "{\"c\":[{\"v\":\"\"},,{\"v\":true}]},"
            + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0},{\"v\":true}]},"
            + "{\"c\":[{\"v\":\"d\\u0027dd\"},{\"v\":222.0},{\"v\":false}]}]}",
        JsonRenderer.renderDataTable(testData, true, true, true).toString());

    // With non default pattern (the ' will be escaped)
    testData.getColumnDescription(1).setPattern("00'\"<script>##");
    assertEquals(
        "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
            + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":"
            + "\"00\\u0027\\u0022\\u003cscript\\u003e##\"},"
            + "{\"id\":\"C\",\"label\":\"col2\",\"type\":\"boolean\",\"pattern\":\"\"}],"
            + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"222\"},{\"v\":false}]},"
            + "{\"c\":[{\"v\":\"\"},,{\"v\":true}]},"
            + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0},{\"v\":true}]},"
            + "{\"c\":[{\"v\":\"d\\u0027dd\"},{\"v\":222.0},{\"v\":false}]}]}",
        JsonRenderer.renderDataTable(testData, true, true, true).toString());
  }
  
  public void testSimpleDataTableWithDatesInJson() throws DataSourceException {
    testData = new DataTable();
    ColumnDescription c0 = new ColumnDescription("DateA", ValueType.DATE, "col0");
    ColumnDescription c1 = new ColumnDescription("DateTimeA", ValueType.DATETIME, "col1");
    ColumnDescription c2 = new ColumnDescription("ValueA", ValueType.NUMBER, "col2");

    testData.addColumn(c0);
    testData.addColumn(c1);
    testData.addColumn(c2);

    rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell(new DateValue(2011, 1, 1), "1/1/2011"));
    row.addCell(new TableCell(new DateTimeValue(2011, 1, 1, 0, 0, 0, 0), "1/1/2011 00:00:00"));
    row.addCell(new TableCell(new NumberValue(222), "222"));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(new DateValue(2011, 1, 2), "1/2/2011"));
    row.addCell(new TableCell(new DateTimeValue(2011, 1, 2, 3, 15, 0, 0)));
    row.addCell(new TableCell(NumberValue.getNullValue()));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(new DateValue(2011, 1, 3), "1/3/2011"));
    row.addCell(new TableCell(new DateTimeValue(2011, 1, 3, 3, 15, 0, 0), "1/1/2011 03:15:00"));
    row.addCell(new TableCell(333));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(new DateValue(2011, 1, 4)));
    row.addCell(new TableCell(new DateTimeValue(2011, 1, 4, 0, 0, 0, 0)));
    row.addCell(new TableCell(222));
    rows.add(row);
    
    testData.addRows(rows);
    assertEquals(
        "{\"cols\":[{\"id\":\"DateA\",\"label\":\"col0\",\"type\":\"date\",\"pattern\":\"\"},"
            + "{\"id\":\"DateTimeA\",\"label\":\"col1\",\"type\":\"datetime\",\"pattern\":\"\"},"
            + "{\"id\":\"ValueA\",\"label\":\"col2\",\"type\":\"number\",\"pattern\":\"\"}],"
            + "\"rows\":[{\"c\":[{\"v\":new Date(2011,1,1),\"f\":\"1/1/2011\"},"
            + "{\"v\":new Date(2011,1,1,0,0,0),\"f\":\"1/1/2011 00:00:00\"},"
            + "{\"v\":222.0,\"f\":\"222\"}]},"
            + "{\"c\":[{\"v\":new Date(2011,1,2),\"f\":\"1/2/2011\"},"
            + "{\"v\":new Date(2011,1,2,3,15,0)},{\"v\":null}]},"
            + "{\"c\":[{\"v\":new Date(2011,1,3),\"f\":\"1/3/2011\"},"
            + "{\"v\":new Date(2011,1,3,3,15,0),\"f\":\"1/1/2011 03:15:00\"},{\"v\":333.0}]},"
            + "{\"c\":[{\"v\":new Date(2011,1,4)},"
            + "{\"v\":new Date(2011,1,4,0,0,0)},{\"v\":222.0}]}]}",
        JsonRenderer.renderDataTable(testData,
            true, true, true /* renderDateConstructor */).toString());
    
    assertEquals(
        "{\"cols\":[{\"id\":\"DateA\",\"label\":\"col0\",\"type\":\"date\",\"pattern\":\"\"},"
            + "{\"id\":\"DateTimeA\",\"label\":\"col1\",\"type\":\"datetime\",\"pattern\":\"\"},"
            + "{\"id\":\"ValueA\",\"label\":\"col2\",\"type\":\"number\",\"pattern\":\"\"}],"
            + "\"rows\":[{\"c\":[{\"v\":\"Date(2011,1,1)\",\"f\":\"1/1/2011\"},"
            + "{\"v\":\"Date(2011,1,1,0,0,0)\",\"f\":\"1/1/2011 00:00:00\"},"
            + "{\"v\":222.0,\"f\":\"222\"}]},"
            + "{\"c\":[{\"v\":\"Date(2011,1,2)\",\"f\":\"1/2/2011\"},"
            + "{\"v\":\"Date(2011,1,2,3,15,0)\"},{\"v\":null}]},"
            + "{\"c\":[{\"v\":\"Date(2011,1,3)\",\"f\":\"1/3/2011\"},"
            + "{\"v\":\"Date(2011,1,3,3,15,0)\",\"f\":\"1/1/2011 03:15:00\"},{\"v\":333.0}]},"
            + "{\"c\":[{\"v\":\"Date(2011,1,4)\"},"
            + "{\"v\":\"Date(2011,1,4,0,0,0)\"},{\"v\":222.0}]}]}",
        JsonRenderer.renderDataTable(testData, true, true, false).toString());
  }

  public void testEntireResponseWithWarnings() throws DataSourceException {
    colIds = Lists.newArrayList();

    testData = new DataTable();
    ColumnDescription c0 = new ColumnDescription("A", ValueType.TEXT, "col0");
    ColumnDescription c1 = new ColumnDescription("B", ValueType.NUMBER, "col1");

    testData.addColumn(c0);
    testData.addColumn(c1);

    rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell("aaa"));
    row.addCell(new TableCell(new NumberValue(222), "$222"));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("bbb"));
    row.addCell(new TableCell(new NumberValue(333)));
    rows.add(row);

    testData.addRows(rows);
    testData.addWarning(new Warning(ReasonType.DATA_TRUNCATED, "Sorry, data truncated"));
    testData.addWarning(new Warning(ReasonType.NOT_SUPPORTED, "foobar"));

    assertEquals(
        "google.visualization.Query.setResponse({\"version\":\"0.6\","
        + "\"reqId\":\"7\",\"status\":\"warning\","
        + "\"warnings\":[{\"reason\":\"data_truncated\",\"message\":"
        + "\"Retrieved data was truncated\",\"detailed_message\":"
        + "\"Sorry, data truncated\"},{\"reason\":\"not_supported\",\"message\":"
        + "\"Operation not supported\",\"detailed_message\":\"foobar\"}],"
        + "\"sig\":\"121655538\",\"table\":"
        + "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
        + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"$222\"}]},"
        + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0}]}]}});",
        JsonRenderer.renderJsonResponse(new DataSourceParameters("reqId:7;out:jsonp"),
            new ResponseStatus(StatusType.WARNING), testData).toString());
  }


  public void testCustomPropertiesToJson() throws DataSourceException {
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
    row.addCell(new TableCell(new NumberValue(222), "222"));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(""));
    row.addCell(new TableCell(NumberValue.getNullValue()));
    rows.add(row);
    row.setCustomProperty("sensi", "puff");

    testData.addRows(rows);

    testData.getRow(0).getCell(0).setCustomProperty("a", "b");

    assertEquals(
        "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
            + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\""
            + ",\"p\":{\"arak\":\"elit\"}}],"
            + "\"rows\":[{\"c\":[{\"v\":\"aaa\",\"p\":{\"a\":\"b\"}},"
            + "{\"v\":222.0,\"f\":\"222\"}]},"
            + "{\"c\":[{\"v\":\"\"},{\"v\":null}],\"p\":{\"sensi\":\"puff\"}}]}",
        JsonRenderer.renderDataTable(testData, true, true, true).toString());

    testData.setCustomProperty("brandy", "cognac");
    assertEquals(
        "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
            + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\""
            + ",\"p\":{\"arak\":\"elit\"}}],"
            + "\"rows\":[{\"c\":[{\"v\":\"aaa\",\"p\":{\"a\":\"b\"}},"
            + "{\"v\":222.0,\"f\":\"222\"}]},"
            + "{\"c\":[{\"v\":\"\"},{\"v\":null}],\"p\":{\"sensi\":\"puff\"}}]"
            + ",\"p\":{\"brandy\":\"cognac\"}}",
    JsonRenderer.renderDataTable(testData, true, true, true).toString());
  }
}
