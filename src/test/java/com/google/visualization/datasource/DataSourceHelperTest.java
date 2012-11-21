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

import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


/**
 * Unit test for Helper.
 *
 * @author Yaniv S.
 */
public class DataSourceHelperTest extends TestCase {

  private DataTable createData() throws TypeMismatchException {
    DataTable data = new DataTable();
    ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
    cd.add(new ColumnDescription("name", ValueType.TEXT, "Animal name"));
    cd.add(new ColumnDescription("link", ValueType.TEXT, "Link to wikipedia"));
    cd.add(new ColumnDescription("population", ValueType.NUMBER, "Population size"));
    cd.add(new ColumnDescription("vegeterian", ValueType.BOOLEAN, "Vegeterian?"));

    data.addColumns(cd);

    // Fill the data-table. addRow() needs to be implemented as taking unknown
    // number of arguments for this to work.
    data.addRowFromValues("Aye-aye", "http://en.wikipedia.org/wiki/Aye-aye", 100, true);
    data.addRowFromValues("Sloth", "http://en.wikipedia.org/wiki/Sloth", 300, true);
    data.addRowFromValues("Leopard", "http://en.wikipedia.org/wiki/Leopard", 50, false);
    data.addRowFromValues("Tiger", "http://en.wikipedia.org/wiki/Tiger", 80, false);

    return data;
  }

  /**
   * Test applyQuery method.
   */
  public void testApplyQuery() throws InvalidQueryException, DataSourceException,
          TypeMismatchException {
    DataTable data = createData();

    // Test select.
    DataTable result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery("select population"),
        data, ULocale.US);
    assertEquals(1, result.getNumberOfColumns());
    assertEquals(4, result.getNumberOfRows());
    assertEquals(new NumberValue(300), result.getRow(1).getCell(0).getValue());

    data = createData();

    // Test where.
    result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery(
        "select name,vegeterian where population > 100"), data, ULocale.US);
    assertEquals(2, result.getNumberOfColumns());
    assertEquals(1, result.getNumberOfRows());
    assertEquals(new TextValue("Sloth"), result.getRow(0).getCell(0).getValue());
    assertEquals(BooleanValue.TRUE, result.getRow(0).getCell(1).getValue());

    data = createData();

    // Test group by.
    result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery(
        "select vegeterian,sum(population) group by vegeterian"), data, ULocale.US);
    assertEquals(2, result.getNumberOfColumns());
    assertEquals(2, result.getNumberOfRows());
    assertEquals(BooleanValue.FALSE, result.getRow(0).getCell(0).getValue());
    assertEquals(new NumberValue(130), result.getRow(0).getCell(1).getValue());

    data = createData();

    // Test pivot.
    result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery(
        "select sum(population) pivot vegeterian"), data, ULocale.US);
    assertEquals(2, result.getNumberOfColumns());
    assertEquals(1, result.getNumberOfRows());
    assertEquals("false", result.getColumnDescription(0).getLabel());
    assertEquals(new NumberValue(130), result.getRow(0).getCell(0).getValue());

    data = createData();

    // Test order by.
    result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery(
        "select name order by population"), data, ULocale.US);
    assertEquals(1, result.getNumberOfColumns());
    assertEquals(4, result.getNumberOfRows());
    assertEquals(new TextValue("Leopard"), result.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("Tiger"), result.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("Aye-aye"), result.getRow(2).getCell(0).getValue());
    assertEquals(new TextValue("Sloth"), result.getRow(3).getCell(0).getValue());

    data = createData();

    // Test limit and offset.
    result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery("limit 1 offset 1"), data,
        ULocale.US);
    assertEquals(4, result.getNumberOfColumns());
    assertEquals(1, result.getNumberOfRows());
    assertEquals(new TextValue("Sloth"), result.getRow(0).getCell(0).getValue());

    data = createData();

    // Test label and format.
    result = DataSourceHelper.applyQuery(DataSourceHelper.parseQuery(
        "label population 'Population size (thousands)' format population \"'$'#'k'\""), data,
        ULocale.US);
    assertEquals(4, result.getNumberOfColumns());
    assertEquals(4, result.getNumberOfRows(), 4);
    assertEquals("Population size (thousands)",
        result.getColumnDescription("population").getLabel());
    int populationIndex = result.getColumnIndex("population");
    assertEquals("$100k", result.getRow(0).getCell(populationIndex).getFormattedValue());

    // Test that validation can fail
    try {
      DataSourceHelper.parseQuery("select min(min(a))");
      fail();
    } catch (InvalidQueryException e) {
      // Do nothing.
    }

    // Test that validation against the table description can fail.
    Query q = DataSourceHelper.parseQuery("select avg(name) group by link");
    try {
      DataSourceHelper.applyQuery(q, data, ULocale.US);
      fail();
    } catch (InvalidQueryException e) {
      // Do nothing.
    }
  }

  /**
   * Test genereateResponse method.
   *
   * @throws DataSourceException
   */
  public void testGenerateResponse() throws DataSourceException {
    // Check with simple data table and simple data source parameters.
    DataTable dataTable = new DataTable();
    dataTable.addColumn(new ColumnDescription("col1", ValueType.NUMBER, "column1"));
    dataTable.addColumn(new ColumnDescription("col2", ValueType.BOOLEAN, "column2"));
    dataTable.addColumn(new ColumnDescription("col3", ValueType.TEXT, "column3"));
    TableRow tableRow = new TableRow();
    tableRow.addCell(7);
    tableRow.addCell(false);
    tableRow.addCell("Why?");
    dataTable.addRow(tableRow);

    DataSourceRequest dataSourceRequest = new DataSourceRequest(
        new Query(),
        new DataSourceParameters(null),
        ULocale.UK);
    assertEquals(
        "{\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1548939605\","
        + "\"table\":{\"cols\":[{\"id\":\"col1\",\"label\":\"column1\","
        + "\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"col2\",\"label\":\"column2\",\"type\":\"boolean\",\"pattern\":\"\"},"
        + "{\"id\":\"col3\",\"label\":\"column3\",\"type\":\"string\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":7.0},{\"v\":false},{\"v\":\"Why?\"}]}]}}",
        DataSourceHelper.generateResponse(dataTable, dataSourceRequest));

    // With reqId:666;
    dataSourceRequest = new DataSourceRequest(
        new Query(),
        new DataSourceParameters("reqId:666"),
        ULocale.UK);
    assertEquals(
        "{\"version\":\"0.6\",\"reqId\":\"666\",\"status\":\"ok\",\"sig\":\"1548939605\","
        + "\"table\":{\"cols\":[{\"id\":\"col1\",\"label\":\"column1\","
        + "\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"col2\",\"label\":\"column2\",\"type\":\"boolean\",\"pattern\":\"\"},"
        + "{\"id\":\"col3\",\"label\":\"column3\",\"type\":\"string\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":7.0},{\"v\":false},{\"v\":\"Why?\"}]}]}}",
        DataSourceHelper.generateResponse(dataTable, dataSourceRequest));

    // With out:json;
    dataSourceRequest = new DataSourceRequest(
        new Query(),
        new DataSourceParameters("out:json"),
        ULocale.UK);
    assertEquals(
        "{\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1548939605\","
        + "\"table\":{\"cols\":[{\"id\":\"col1\",\"label\":\"column1\","
        + "\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"col2\",\"label\":\"column2\",\"type\":\"boolean\",\"pattern\":\"\"},"
        + "{\"id\":\"col3\",\"label\":\"column3\",\"type\":\"string\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":7.0},{\"v\":false},{\"v\":\"Why?\"}]}]}}",
        DataSourceHelper.generateResponse(dataTable, dataSourceRequest));

    // With out:jsonp;
    dataSourceRequest = new DataSourceRequest(
        new Query(),
        new DataSourceParameters("out:jsonp"),
        ULocale.UK);
    assertEquals(
        "// Data table response\ngoogle.visualization.Query.setResponse("
        + "{\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1548939605\","
        + "\"table\":{\"cols\":[{\"id\":\"col1\",\"label\":\"column1\","
        + "\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"col2\",\"label\":\"column2\",\"type\":\"boolean\",\"pattern\":\"\"},"
        + "{\"id\":\"col3\",\"label\":\"column3\",\"type\":\"string\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":7.0},{\"v\":false},{\"v\":\"Why?\"}]}]}});",
        DataSourceHelper.generateResponse(dataTable, dataSourceRequest));

    // Now with out:csv;
    dataSourceRequest = new DataSourceRequest(
        new Query(),
        new DataSourceParameters("out:tsv-excel"),
        ULocale.UK);
    assertEquals(
        "\"column1\"\t\"column2\"\t\"column3\"\n7\tfalse\t\"Why?\"\n",
        DataSourceHelper.generateResponse(dataTable, dataSourceRequest));

    // Now with out:tsv-excel;
    dataSourceRequest = new DataSourceRequest(
        new Query(),
        new DataSourceParameters("out:csv;reqId:7"),
        ULocale.UK);
    assertEquals(
        "\"column1\",\"column2\",\"column3\"\n7,false,\"Why?\"\n",
        DataSourceHelper.generateResponse(dataTable, dataSourceRequest));
  }


  public void testGetLocaleFromReuqest() {
    HttpServletRequest req = createMock(HttpServletRequest.class);
    expect(req.getParameter(DataSourceHelper.LOCALE_REQUEST_PARAMETER)).andReturn("fr");
    expect(req.getParameter(DataSourceHelper.LOCALE_REQUEST_PARAMETER)).andReturn(null);
    expect(req.getLocale()).andReturn(Locale.CANADA_FRENCH);
    replay(req);

    assertEquals(Locale.FRENCH, DataSourceHelper.getLocaleFromRequest(req).toLocale());
    assertEquals(ULocale.CANADA_FRENCH, DataSourceHelper.getLocaleFromRequest(req));

    verify(req);
  }
  
  public void testVerifyAccessApprovedTest() throws DataSourceException {
    HttpServletRequest req = createNiceMock(HttpServletRequest.class);

    // Verify that json from same domain is approved.
    setupHttpRequestMock(req, true, "out:json");
    DataSourceHelper.verifyAccessApproved(new DataSourceRequest(req));
    verify(req);
    
    // Verify that json from cross domain is denied.
    setupHttpRequestMock(req, false, "out:json");
    try {
      DataSourceHelper.verifyAccessApproved(new DataSourceRequest(req));
      fail();
    } catch (DataSourceException e) {
      // This is the expected behavior.
    }
    verify(req);
    
    // Verify that csv from cross domain is approved.
    setupHttpRequestMock(req, false, "out:csv");
    DataSourceHelper.verifyAccessApproved(new DataSourceRequest(req));
    verify(req);
    
    // Verify that html from cross domain is approved.
    setupHttpRequestMock(req, false, "out:html");
    DataSourceHelper.verifyAccessApproved(new DataSourceRequest(req));
    verify(req);

    // Verify that tsv-excel from cross domain is approved.
    setupHttpRequestMock(req, false, "out:tsv-excel");
    DataSourceHelper.verifyAccessApproved(new DataSourceRequest(req));
    verify(req);
  }
  
  public void testParseQueryErrors() {
    DataTable dataTable = new DataTable();
    dataTable.addColumn(new ColumnDescription("A", ValueType.TEXT, "column1"));
    dataTable.addColumn(new ColumnDescription("B", ValueType.BOOLEAN, "column2"));
    dataTable.addColumn(new ColumnDescription("C", ValueType.NUMBER, "column3"));
    // Wrong column id
    checkQueryError("select D", dataTable, "Column [D] does not exist in table.");
    checkQueryError("where F > 1", dataTable, "Column [F] does not exist in table.");
    // aggregation sum and average only on numeric columns
    checkQueryError("select avg(A) group by B", dataTable,
        "'Average' and 'sum' aggreagation functions can be applied only on numeric values.");
    checkQueryError("select sum(A) group by B", dataTable,
        "'Average' and 'sum' aggreagation functions can be applied only on numeric values.");
    checkQueryError("select sum(B) group by A", dataTable,
        "'Average' and 'sum' aggreagation functions can be applied only on numeric values.");
    // parse errors
    checkQueryError("select (", dataTable, "Query parse error: ", true);
    checkQueryError("group by avg(A)", dataTable,
        "Column [AVG(`A`)] cannot be in GROUP BY because it has an aggregation.");
    checkQueryError("pivot avg(A)", dataTable,
        "Column [AVG(`A`)] cannot be in PIVOT because it has an aggregation.");
    checkQueryError("where avg(A) > 1", dataTable,
        "Column [AVG(`A`)] cannot appear in WHERE because it has an aggregation.");
    checkQueryError("select A, sum(A)", dataTable,
        "Column [A] cannot be selected both with and without aggregation in SELECT.");
    checkQueryError("select sum(C) group by C", dataTable,
        "Column [C] which is aggregated in SELECT, cannot appear in GROUP BY.");
    checkQueryError("select A group by C", dataTable,
        "Cannot use GROUP BY when no aggregations are defined in SELECT.");
    checkQueryError("select A pivot C", dataTable,
        "Cannot use PIVOT when no aggregations are defined in SELECT.");
    checkQueryError("select min(B) pivot B", dataTable,
        "Column [B] which is aggregated in SELECT, cannot appear in PIVOT.");
    checkQueryError("select A format B 'yes:no'", dataTable,
        "Column [`B`] which is referenced in FORMAT, is not part of SELECT clause.");
    checkQueryError("select A label B 'COL'", dataTable,
        "Column [`B`] which is referenced in LABEL, is not part of SELECT clause.");
    checkQueryError("select A,count(B)", dataTable,
        "Column [A] should be added to GROUP BY, removed from SELECT, or aggregated in SELECT.");
    checkQueryError("select B order by min(A)", dataTable,
        "Aggregation [MIN(`A`)] found in ORDER BY but was not found in SELECT");
    checkQueryError("select min(A) pivot B order by min(A)", dataTable,
        "Column [A] cannot be aggregated in ORDER BY when PIVOT is used.");
    checkQueryError("select min(A) order by B", dataTable,
        "Column [`B`] which appears in ORDER BY, must be in SELECT as well, " +
        "because SELECT contains aggregated columns.");
    checkQueryError("select min(A) group by B pivot B", dataTable,
        "Column [B] cannot appear both in GROUP BY and in PIVOT.");
    checkQueryError("offset -1", dataTable,
        "Invalid value for row offset: -1");
    checkQueryError("select avg(C),avg(C)", dataTable,
        "Column [avg(C)] cannot appear more than once in SELECT.");
  }
  
  private void checkQueryError(String query, DataTable dataTable, String expectedMessage) {
    checkQueryError(query, dataTable, expectedMessage, false);
  }
  
  private void checkQueryError(String query, DataTable dataTable, String expectedMessage,
      boolean startsWith) {
    try {
      DataSourceHelper.applyQuery(
          DataSourceHelper.parseQuery(query), dataTable, null);
      fail("Exception should be thrown for query " + query);
    } catch (DataSourceException dse) {
      if (startsWith) {
        assertNotNull(dse.getMessageToUser());
        assertTrue(dse.getMessageToUser().startsWith(expectedMessage));
      } else {
        assertEquals(expectedMessage, dse.getMessageToUser());
      }
    }
  }
  
  private void setupHttpRequestMock(HttpServletRequest req, boolean hasHeader, String tqx) {
    reset(req);
    expect(req.getHeader(DataSourceRequest.SAME_ORIGIN_HEADER)).andReturn(hasHeader ? "a" : null);
    expect(req.getParameter(DataSourceRequest.DATASOURCE_REQUEST_PARAMETER)).andReturn(tqx);
    replay(req);
  }
}

