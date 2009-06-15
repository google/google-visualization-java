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
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests for HtmlRenderer.
 *
 * @author Nimrod T.
 */
public class HtmlRendererTest extends TestCase {

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

  public void testEmptyDataTableToHtml() {
    DataTable dataTable = new DataTable();
    String expected =
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
        + "<title>Google Visualization</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">\n"
        + "<tr style=\"font-weight: bold; background-color: #aaa;\"></tr>\n"
        + "</table>\n"
        + "</body>\n"
        + "</html>\n";

    String actual = HtmlRenderer.renderDataTable(dataTable, ULocale.US).toString();
    assertEquals(expected, actual);
  }

  public void testSimpleDataTableToHtml() throws DataSourceException {
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
    row.addCell(new TableCell(111));
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

    String expected =
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
        + "<title>Google Visualization</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">\n"
        + "<tr style=\"font-weight: bold; background-color: #aaa;\">\n"
        + "<td>col0</td>"
        + "<td>col1</td>"
        + "<td>col2</td>\n"
        + "</tr>\n"
        + "<tr style=\"background-color: #f0f0f0\">\n"
        + "<td>aaa</td>"
        + "<td align=\"right\">222</td>"
        + "<td align=\"center\">\u2717</td>\n"
        + "</tr>\n"
        + "<tr style=\"background-color: #ffffff\">\n"
        + "<td>&nbsp;</td>"
        + "<td align=\"right\">111</td>"
        + "<td align=\"center\">\u2714</td>\n"
        + "</tr>\n"
        + "<tr style=\"background-color: #f0f0f0\">\n"
        + "<td>bb@@b</td>"
        + "<td align=\"right\">333</td>"
        + "<td align=\"center\">\u2714</td>\n"
        + "</tr>\n"
        + "<tr style=\"background-color: #ffffff\">\n"
        + "<td>ddd</td>"
        + "<td align=\"right\">222</td>"
        + "<td align=\"center\">\u2717</td>\n"
        + "</tr>\n"
        + "</table>\n"
        + "</body>\n"
        + "</html>\n";

    String actual = HtmlRenderer.renderDataTable(testData, ULocale.US).toString();

    assertEquals(expected, actual);
  }

  public void testWarnings() throws DataSourceException {
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
    row.addCell(new TableCell(333));
    rows.add(row);

    testData.addRows(rows);

    testData.addWarning(new Warning(ReasonType.DATA_TRUNCATED, "Sorry, data truncated"));
    testData.addWarning(new Warning(ReasonType.NOT_SUPPORTED, "foobar"));

    String expected =
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
      + "<html>\n"
      + "<head>\n"
      + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
      + "<title>Google Visualization</title>\n"
      + "</head>\n"
      + "<body>\n"
      + "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">\n"
      + "<tr style=\"font-weight: bold; background-color: #aaa;\">\n"
      + "<td>col0</td>"
      + "<td>col1</td>\n"
      + "</tr>\n"
      + "<tr style=\"background-color: #f0f0f0\">\n"
      + "<td>aaa</td>"
      + "<td align=\"right\">$222</td>\n"
      + "</tr>\n"
      + "<tr style=\"background-color: #ffffff\">\n"
      + "<td>bbb</td>"
      + "<td align=\"right\">333</td>\n"
      + "</tr>\n"
      + "</table>\n"
      + "<br>\n<br>\n<div>Retrieved data was truncated. Sorry, data truncated</div>\n"
      + "<br>\n<br>\n<div>Operation not supported. foobar</div>\n"
      + "</body>\n"
      + "</html>\n";

    String actual = HtmlRenderer.renderDataTable(testData, ULocale.US).toString();

    assertEquals(expected, actual);
  }

  public void testEscaping() {
    ResponseStatus responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.INVALID_REQUEST, "but why? why? why?");
    assertEquals(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
        + "<title>Google Visualization</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "<h3>Oops, an error occured.</h3>\n"
        + "<div>Status: error</div>\n"
        + "<div>Reason: Invalid request</div>\n"
        + "<div>Description: but why? why? why?</div>\n"
        + "</body>\n"
        + "</html>\n",
        HtmlRenderer.renderHtmlError(responseStatus).toString());

    responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.NOT_SUPPORTED, "Cannot do dat!");
    assertEquals(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
        + "<title>Google Visualization</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "<h3>Oops, an error occured.</h3>\n"
        + "<div>Status: error</div>\n"
        + "<div>Reason: Operation not supported</div>\n"
        + "<div>Description: Cannot do dat!</div>\n"
        + "</body>\n"
        + "</html>\n",
        HtmlRenderer.renderHtmlError(responseStatus).toString());
  }

  public void testRenderError() {
    ResponseStatus responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.INVALID_REQUEST, "but why? why?");
    assertEquals(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
        + "<title>Google Visualization</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "<h3>Oops, an error occured.</h3>\n"
        + "<div>Status: error</div>\n"
        + "<div>Reason: Invalid request</div>\n"
        + "<div>Description: but why? why?</div>\n"
        + "</body>\n"
        + "</html>\n",
        HtmlRenderer.renderHtmlError(responseStatus).toString());

    responseStatus = new ResponseStatus(
        StatusType.ERROR, ReasonType.NOT_SUPPORTED, "Cannot do dat!");
    assertEquals(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
        + "<title>Google Visualization</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "<h3>Oops, an error occured.</h3>\n"
        + "<div>Status: error</div>\n"
        + "<div>Reason: Operation not supported</div>\n"
        + "<div>Description: Cannot do dat!</div>\n"
        + "</body>\n"
        + "</html>\n",
        HtmlRenderer.renderHtmlError(responseStatus).toString());
  }
}
