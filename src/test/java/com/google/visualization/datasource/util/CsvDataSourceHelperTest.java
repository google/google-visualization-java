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
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Tests for the CsvDataSourceHelper class.
 *
 * @author Nimrod T.
 */
public class CsvDataSourceHelperTest extends TestCase {

  public void testReaderFromUrl() {
    String url = "xxx";
    boolean catched = false;
    try {
      CsvDataSourceHelper.getCsvUrlReader(url);
    } catch (DataSourceException e) {
      assertEquals(ReasonType.INVALID_REQUEST, e.getReasonType());
      assertEquals(e.getMessageToUser(), "url is malformed: " + url);
      catched = true;
    }
    assertTrue(catched);
  }

  public void testRead() throws IOException, CsvDataSourceException {
    // Null reader.
    Reader reader = null;
    DataTable dataTable = CsvDataSourceHelper.read(reader, null, false);
    assertEquals(0, dataTable.getNumberOfRows());

    // Empty string reader.
    reader = new StringReader("");
    dataTable = CsvDataSourceHelper.read(reader, null, false);
    assertEquals(0, dataTable.getNumberOfRows());

    // Null TableDescription.
    reader = new StringReader("1,2,3\n4,5,6");
    dataTable = CsvDataSourceHelper.read(reader, null, false);
    assertEquals(2, dataTable.getNumberOfRows());
    assertEquals(3, dataTable.getNumberOfColumns());
    assertEquals(new TextValue("1"), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("2"), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new TextValue("3"), dataTable.getRow(0).getCell(2).getValue());
    assertEquals(new TextValue("4"), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("5"), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(new TextValue("6"), dataTable.getRow(1).getCell(2).getValue());

    // Different column numbers.
    boolean catched = false;
    try {
      reader = new StringReader("1,2\na, b, c");
      dataTable = CsvDataSourceHelper.read(reader, null, false);
    } catch (CsvDataSourceException e) {
      catched = true;
      assertEquals(ReasonType.INTERNAL_ERROR, e.getReasonType());
      assertEquals(
          "Wrong number of columns in the data.",
          e.getMessageToUser());
    }
    assertTrue(catched);

    // Working example with a table description filled only with types.
    reader = new StringReader("1,a,true\n4,x13a,false\n1400,4,true");
    List<ColumnDescription> columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("i1", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i2", ValueType.TEXT, null));
    columnDescriptions.add(new ColumnDescription("i3", ValueType.DATE, null));
    dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, false);
    assertEquals(3, dataTable.getNumberOfRows());
    assertEquals(3, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(1), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("a"), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(DateValue.getNullValue(), dataTable.getRow(0).getCell(2).getValue());
    assertEquals(new NumberValue(4), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("x13a"), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(DateValue.getNullValue(), dataTable.getRow(1).getCell(2).getValue());
    assertEquals(new NumberValue(1400), dataTable.getRow(2).getCell(0).getValue());
    assertEquals(new TextValue("4"), dataTable.getRow(2).getCell(1).getValue());
    assertEquals(DateValue.getNullValue(), dataTable.getRow(2).getCell(2).getValue());

    // Working example with a table description filled only with types.
    reader = new StringReader("1,a,2004-03-01\n4,x13a,2005-04-02\n1400,4,2006-05-03");
    columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("i1", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i2", ValueType.TEXT, null));
    columnDescriptions.add(new ColumnDescription("i3", ValueType.DATE, null));
    dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, false);
    assertEquals(3, dataTable.getNumberOfRows());
    assertEquals(3, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(1), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("a"), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new DateValue(2004, 2, 1), dataTable.getRow(0).getCell(2).getValue());
    assertEquals(new NumberValue(4), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("x13a"), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(new DateValue(2005, 3, 2), dataTable.getRow(1).getCell(2).getValue());
    assertEquals(new NumberValue(1400), dataTable.getRow(2).getCell(0).getValue());
    assertEquals(new TextValue("4"), dataTable.getRow(2).getCell(1).getValue());
    assertEquals(new DateValue(2006, 4, 3), dataTable.getRow(2).getCell(2).getValue());
    assertEquals("i1", dataTable.getColumnDescription(0).getId());
    assertEquals("Column0", dataTable.getColumnDescription(0).getLabel());
    assertEquals("i2", dataTable.getColumnDescription(1).getId());
    assertEquals("Column1", dataTable.getColumnDescription(1).getLabel());
    assertEquals("i3", dataTable.getColumnDescription(2).getId());
    assertEquals("Column2", dataTable.getColumnDescription(2).getLabel());

    // Working example with header rows.
    reader = new StringReader("1,a,2004-03-01\n4,x13a,2005-04-02\n1400,4,2006-05-03");
    columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("i1", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i2", ValueType.TEXT, null));
    columnDescriptions.add(new ColumnDescription("i3", ValueType.DATE, null));
    dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, true);
    assertEquals(2, dataTable.getNumberOfRows());
    assertEquals(3, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(4), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("x13a"), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new DateValue(2005, 3, 2), dataTable.getRow(0).getCell(2).getValue());
    assertEquals(new NumberValue(1400), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("4"), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(new DateValue(2006, 4, 3), dataTable.getRow(1).getCell(2).getValue());
    assertEquals("i1", dataTable.getColumnDescription(0).getId());
    assertEquals("1",  dataTable.getColumnDescription(0).getLabel());
    assertEquals("i2", dataTable.getColumnDescription(1).getId());
    assertEquals("a", dataTable.getColumnDescription(1).getLabel());
    assertEquals("i3", dataTable.getColumnDescription(2).getId());
    assertEquals("2004-03-01", dataTable.getColumnDescription(2).getLabel());

    // Bad table description for that data.
    catched = false;
    try {
      reader = new StringReader("true\nfalse\nfalse");
      columnDescriptions = Lists.newArrayList();
      dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, false);
    } catch (CsvDataSourceException e) {
      catched = true;
      assertEquals(ReasonType.INTERNAL_ERROR, e.getReasonType());
      assertEquals(
          "Wrong number of columns in the data.",
          e.getMessageToUser());
    }
    assertTrue(catched);

    // Working example with a null table description.
    reader = new StringReader("true,false\ntrue,false\ntrue,false\nfalse,false");
    columnDescriptions = null;
    dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, false);
    assertEquals(4, dataTable.getNumberOfRows());
    assertEquals(2, dataTable.getNumberOfColumns());
    assertEquals(new TextValue("true"), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("false"), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new TextValue("true"), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("false"), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(new TextValue("true"), dataTable.getRow(2).getCell(0).getValue());
    assertEquals(new TextValue("false"), dataTable.getRow(2).getCell(1).getValue());
    assertEquals(new TextValue("false"), dataTable.getRow(3).getCell(0).getValue());
    assertEquals(new TextValue("false"), dataTable.getRow(3).getCell(1).getValue());

    // Working example with a table description filled with types.
    reader = new StringReader("true,false\ntrue,false\ntrue,false\nfalse,false");
    columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("1", ValueType.BOOLEAN, "123"));
    columnDescriptions.add(new ColumnDescription("2", ValueType.BOOLEAN, "123"));
    dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, false);
    assertEquals(4, dataTable.getNumberOfRows());
    assertEquals(2, dataTable.getNumberOfColumns());
    assertEquals(BooleanValue.TRUE, dataTable.getRow(0).getCell(0).getValue());
    assertEquals(BooleanValue.FALSE, dataTable.getRow(0).getCell(1).getValue());
    assertEquals(BooleanValue.TRUE, dataTable.getRow(1).getCell(0).getValue());
    assertEquals(BooleanValue.FALSE, dataTable.getRow(1).getCell(1).getValue());
    assertEquals(BooleanValue.TRUE, dataTable.getRow(2).getCell(0).getValue());
    assertEquals(BooleanValue.FALSE, dataTable.getRow(2).getCell(1).getValue());
    assertEquals(BooleanValue.FALSE, dataTable.getRow(3).getCell(0).getValue());
    assertEquals(BooleanValue.FALSE, dataTable.getRow(3).getCell(1).getValue());
  }
  
  public void testPatterns() throws CsvDataSourceException, IOException {
    // Working example with header rows.
    Reader reader = new StringReader("1,a,20040301\n4,x13a,20050402\n1400,4,20060503");
    List<ColumnDescription> columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("i1", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i2", ValueType.TEXT, null));
    
    ColumnDescription columnDescription = new ColumnDescription("i3", ValueType.DATE, null);
    columnDescription.setPattern("yyyyMMdd");
    columnDescriptions.add(columnDescription);
    
    DataTable dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, true);
    assertEquals(2, dataTable.getNumberOfRows());
    assertEquals(3, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(4), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new TextValue("x13a"), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new DateValue(2005, 3, 2), dataTable.getRow(0).getCell(2).getValue());
    assertEquals(new NumberValue(1400), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(new TextValue("4"), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(new DateValue(2006, 4, 3), dataTable.getRow(1).getCell(2).getValue());
    assertEquals("i1", dataTable.getColumnDescription(0).getId());
    assertEquals("1",  dataTable.getColumnDescription(0).getLabel());
    assertEquals("i2", dataTable.getColumnDescription(1).getId());
    assertEquals("a", dataTable.getColumnDescription(1).getLabel());
    assertEquals("i3", dataTable.getColumnDescription(2).getId());
    assertEquals("20040301", dataTable.getColumnDescription(2).getLabel());
  }

  public void testWhitespaces() throws CsvDataSourceException, IOException {
    // Working example with header rows.
    Reader reader = new StringReader("   1   ,2,   3\n4   , 5 , 6");
    List<ColumnDescription> columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("i1", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i2", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i3", ValueType.NUMBER, null));
       
    DataTable dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, true);
    assertEquals(1, dataTable.getNumberOfRows());
    assertEquals(3, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(4), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new NumberValue(5), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new NumberValue(6), dataTable.getRow(0).getCell(2).getValue());
    assertEquals("i1", dataTable.getColumnDescription(0).getId());
    assertEquals("1",  dataTable.getColumnDescription(0).getLabel());
    assertEquals("i2", dataTable.getColumnDescription(1).getId());
    assertEquals("2", dataTable.getColumnDescription(1).getLabel());
    assertEquals("i3", dataTable.getColumnDescription(2).getId());
    assertEquals("3", dataTable.getColumnDescription(2).getLabel());
  }
  
  public void testEmptyStrings() throws CsvDataSourceException, IOException {
    // Working example with header rows.
    Reader reader = new StringReader("   1   ,,2,   3\n4   , 5 , 6,\n 7 ,,,10");
    List<ColumnDescription> columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("i1", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i2", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i3", ValueType.NUMBER, null));
    columnDescriptions.add(new ColumnDescription("i4", ValueType.NUMBER, null));
       
    DataTable dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, true);
    assertEquals(2, dataTable.getNumberOfRows());
    assertEquals(4, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(4), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(new NumberValue(5), dataTable.getRow(0).getCell(1).getValue());
    assertEquals(new NumberValue(6), dataTable.getRow(0).getCell(2).getValue());
    assertEquals(NumberValue.getNullValue(), dataTable.getRow(0).getCell(3).getValue());
    assertEquals(new NumberValue(7), dataTable.getRow(1).getCell(0).getValue());
    assertEquals(NumberValue.getNullValue(), dataTable.getRow(1).getCell(1).getValue());
    assertEquals(NumberValue.getNullValue(), dataTable.getRow(1).getCell(2).getValue());
    assertEquals(new NumberValue(10), dataTable.getRow(1).getCell(3).getValue());
    assertEquals("i1", dataTable.getColumnDescription(0).getId());
    assertEquals("1",  dataTable.getColumnDescription(0).getLabel());
    assertEquals("i2", dataTable.getColumnDescription(1).getId());
    assertEquals("", dataTable.getColumnDescription(1).getLabel());
    assertEquals("i3", dataTable.getColumnDescription(2).getId());
    assertEquals("2", dataTable.getColumnDescription(2).getLabel());
    assertEquals("i4", dataTable.getColumnDescription(3).getId());
    assertEquals("3", dataTable.getColumnDescription(3).getLabel());
  }  
  
  public void testReadWithLocale() throws IOException, CsvDataSourceException {
    List <ColumnDescription> columnDescriptions = Lists.newArrayList();
    columnDescriptions.add(new ColumnDescription("A", ValueType.NUMBER, "A"));
    columnDescriptions.add(new ColumnDescription("B", ValueType.TIMEOFDAY, "B"));
    TimeOfDayValue hindiTimeOfDayValue = new TimeOfDayValue(1, 12, 1);
    String hindiTimeOfDayString =  "\u0966\u0967\u003a\u0967\u0968\u003a\u0966\u0967";
    Reader reader = new StringReader("1," + hindiTimeOfDayString);
    DataTable dataTable = CsvDataSourceHelper.read(reader, columnDescriptions, false,
        new ULocale("hi_IN"));
    assertEquals(1, dataTable.getNumberOfRows());
    assertEquals(2, dataTable.getNumberOfColumns());
    assertEquals(new NumberValue(1), dataTable.getRow(0).getCell(0).getValue());
    assertEquals(hindiTimeOfDayValue, dataTable.getRow(0).getCell(1).getValue());
  }
}
