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

package com.google.visualization.datasource;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;

import junit.framework.TestCase;

import java.util.List;


/**
 * Unit test for ResponseWriter.
 *
 * @author Nimrod T.
 */
public class ResponseWriterTest extends TestCase {

  private DataTable getTestDataTable() throws DataSourceException {
    DataTable dataTable = new DataTable();
    ColumnDescription c0 = new ColumnDescription("A", ValueType.TEXT, "col0");
    ColumnDescription c1 = new ColumnDescription("B", ValueType.NUMBER, "col1");
    ColumnDescription c2 = new ColumnDescription("C", ValueType.BOOLEAN, "col2");

    dataTable.addColumn(c0);
    dataTable.addColumn(c1);
    dataTable.addColumn(c2);

    List<TableRow> rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell("aaa"));
    row.addCell(new TableCell(new NumberValue(222), "222"));
    row.addCell(new TableCell(false));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(""));
    row.addCell(new TableCell(111));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell((new TextValue("bbb")), "bbb"));
    row.addCell(new TableCell(333));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("ddd"));
    row.addCell(new TableCell(222));
    row.addCell(new TableCell(false));
    rows.add(row);
    dataTable.addRows(rows);

    return dataTable;
  }

  public void testSetServletResponseJson() throws DataSourceException {
    // Basic test 1.
    DataTable data = getTestDataTable();
    DataSourceParameters dsParams = new DataSourceParameters("responseHandler:babylon;out:json");
    ResponseStatus responseStatus = new ResponseStatus(StatusType.OK, null, null);

    String expected = "{\"version\":\"0.6\",\"status\":\"ok\","
        + "\"sig\":\"2087475733\",\"table\":"
        + "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
        + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"C\",\"label\":\"col2\",\"type\":\"boolean\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"222\"},{\"v\":false}]},"
        + "{\"c\":[{\"v\":\"\"},{\"v\":111.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"ddd\"},{\"v\":222.0},{\"v\":false}]}]}}";

    assertEquals(expected, JsonRenderer.renderJsonResponse(
        dsParams, responseStatus, data, false).toString());
    assertEquals("babylon(" + expected + ");", JsonRenderer.renderJsonResponse(
        dsParams, responseStatus, data, true).toString());

    // Basic test 2.
    data = getTestDataTable();
    dsParams = new DataSourceParameters("reqId:90210;responseHandler:babylon;");
    responseStatus = new ResponseStatus(StatusType.OK, null, null);

    expected = "{\"version\":\"0.6\",\"reqId\":\"90210\",\"status\":\"ok\","
        + "\"sig\":\"2087475733\",\"table\":"
        + "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
        + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"C\",\"label\":\"col2\",\"type\":\"boolean\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"222\"},{\"v\":false}]},"
        + "{\"c\":[{\"v\":\"\"},{\"v\":111.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"ddd\"},{\"v\":222.0},{\"v\":false}]}]}}";

    assertEquals(expected, JsonRenderer.renderJsonResponse(
        dsParams, responseStatus, data, false).toString());
    assertEquals("babylon(" + expected + ");", JsonRenderer.renderJsonResponse(
        dsParams, responseStatus, data, true).toString());
  }

  public void testGenerateJsonResponseError() throws DataSourceException {
    DataTable data = getTestDataTable();
    DataSourceParameters dsParams =
        new DataSourceParameters("reqId:90210;responseHandler:babylon;");
    ResponseStatus responseStatus = new ResponseStatus(
        StatusType.ERROR,
        ReasonType.INTERNAL_ERROR,
        "this is me not you why it is that not knowing me cave man");

    String expected = "{\"version\":\"0.6\",\"reqId\":\"90210\",\"status\":\"error\",\"errors\":"
        + "[{\"reason\":\"internal_error\",\"message\":\"Internal error\","
        + "\"detailed_message\":\"this is me not you why it is that not knowing me cave man\"}]}";
    assertEquals(
        expected,
        JsonRenderer.renderJsonResponse(dsParams, responseStatus, data, false).toString());
    assertEquals(
        "babylon(" + expected + ");",
        JsonRenderer.renderJsonResponse(dsParams, responseStatus, data, true).toString());
  }
}
