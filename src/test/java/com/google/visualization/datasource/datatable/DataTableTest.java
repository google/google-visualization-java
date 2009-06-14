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

package com.google.visualization.datasource.datatable;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.base.Warning;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tests for DataTable.
 *
 * @author Yoah B.D.
 */
public class DataTableTest extends TestCase {

  DataTable testData;

  List<TableRow> rows;

  List<String> colIds;

  private static final Comparator<TableCell> CELL_COMPARATOR =
      TableCell.getLocalizedComparator(ULocale.ENGLISH);

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
    row.addCell(new TableCell(new DateTimeValue(2000, 10 ,1, 1, 10, 23, 432)));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(new TextValue("ccc"), "$ccc"));
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
    row.addCell(new TableCell(new DateTimeValue(2000, 1 ,1, 1, 10, 31, 4)));
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

  /**
   * Tests getting cells for a single column.
   */
  public void testCellList() {
    List<TableCell> cells = testData.getColumnCells(2);
    assertEquals(4, cells.size());
    assertEquals(ValueType.BOOLEAN, cells.get(0).getType());

    cells = testData.getColumnCells("col0");
    assertEquals(4, cells.size());
    assertEquals(ValueType.TEXT, cells.get(0).getType());
  }

  /**
   * Tests adding a row.
   */
  public void testAddRow() throws Exception {
    assertEquals(4, testData.getNumberOfRows());
    TableRow row = new TableRow();
    TableCell cell = new TableCell("1234");
    row.addCell(cell);
    testData.addRow(row);
    assertEquals(5, testData.getNumberOfRows());

    TableRow row1 = testData.getRow(4);
    assertEquals(cell, row1.getCell(0));
  }

  /**
   * Tests addRowFromValues.
   */
  public void testAddRowFromValues() {
    assertEquals(4, testData.getNumberOfRows());
    GregorianCalendar c1 = new GregorianCalendar(2009, 2, 15);
    c1.setTimeZone(TimeZone.getTimeZone("GMT"));
    GregorianCalendar c2 = new GregorianCalendar(2009, 2, 15, 12, 30, 45);
    c2.setTimeZone(TimeZone.getTimeZone("GMT"));
    try {
      testData.addRowFromValues("blah", 5, true, c1, c2, c2);
    } catch (TypeMismatchException e) {
      fail();
    }
    assertEquals(5, testData.getNumberOfRows());
    assertEquals(testData.getRow(4).getCell(0).getValue().compareTo(new TextValue("blah")), 0);
    assertEquals(testData.getRow(4).getCell(1).getValue().compareTo(new NumberValue(5)), 0);
    assertEquals(testData.getRow(4).getCell(2).getValue().compareTo(BooleanValue.TRUE), 0);
    assertEquals(testData.getRow(4).getCell(3).getValue().compareTo(new DateValue(c1)), 0);
    assertEquals(testData.getRow(4).getCell(4).getValue().compareTo(new TimeOfDayValue(c2)), 0);
    assertEquals(testData.getRow(4).getCell(5).getValue().compareTo(new DateTimeValue(c2)), 0);
  }

  /**
   * Tests get and set rows.
   */
  public void testGetSetRows() throws Exception {
    testData.setRows(rows);
    List<TableRow> retrievedRows = testData.getRows();
    assertNotNull(retrievedRows);
    assertEquals(rows.size(), retrievedRows.size());
    assertEquals(rows.get(0).getCell(0).getValue(), retrievedRows.get(0).getCell(0).getValue());
  }

  /**
   * Tests distinct column values.
   */
  public void testColumnDistinctValues() {
    List<Value> values = testData.getColumnDistinctValues(0);
    assertEquals(4, values.size());
    values = testData.getColumnDistinctValues("col1");
    assertEquals(3, values.size());
    assertEquals("3.0", values.get(0).toString());
    assertEquals("111.0", values.get(1).toString());
    assertEquals("222.0", values.get(2).toString());
    values = testData.getColumnDistinctValues("col2");
    assertEquals(2, values.size());
  }

  /**
   * Tests distinct column values.
   */
  public void testColumnDistinctCellsSorted() {
    // Test col1.
    List<TableCell> cells = testData.
        getColumnDistinctCellsSorted(1, CELL_COMPARATOR);
    assertEquals(3, cells.size());
    assertEquals("3.0", cells.get(0).toString());
    assertEquals("111.0", cells.get(1).toString());
    assertEquals("222.0", cells.get(2).toString());

    // Test col2.
    cells = testData.getColumnDistinctCellsSorted(2, CELL_COMPARATOR);
    assertEquals(2, cells.size());
    assertEquals("false", cells.get(0).toString());
    assertEquals("true", cells.get(1).toString());

    // Test col6, verify that when formatted value is present
    // the sorting is based on it.
    cells = testData.getColumnDistinctCellsSorted(0, CELL_COMPARATOR);
    assertEquals(4, cells.size());
    assertEquals("aaa", cells.get(0).toString());
    assertEquals("bbb", cells.get(1).toString());
    assertEquals("ccc", cells.get(2).toString());
    assertEquals("ddd", cells.get(3).toString());
  }


  /**
   * Tests the check that a list of column ids are all in the data.
   */
  public void testContainsAll() {
    assertTrue(testData.containsAllColumnIds(colIds));
    // Check that it works for a partial set of ids as well.
    if (!colIds.isEmpty()) {
      colIds.remove(0);
      assertTrue(testData.containsAllColumnIds(colIds));
    }
    colIds.add("@@@@@#@#@#@#");
    assertFalse(testData.containsAllColumnIds(colIds));
  }

  public void testClone() throws Exception {
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
    row.addCell(new TableCell(new TextValue("bbb"), "bbb"));
    row.addCell(new TableCell(333));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("ddd"));
    row.addCell(new TableCell(222));
    row.addCell(new TableCell(false));
    rows.add(row);

    testData.addRows(rows);

    DataTable cloned = testData.clone();

    // Test that the data is the same:
    assertEquals("col0", cloned.getColumnDescription("A").getLabel());
    assertEquals("aaa", cloned.getRow(0).getCell(0).getValue().toString());
    assertEquals("222", cloned.getRow(0).getCell(1).getFormattedValue());

    // Test that some pointers are not the same:
    assertTrue(cloned != testData);
    assertTrue(cloned.getRows() != testData.getRows());
    assertTrue(cloned.getRow(1) != testData.getRow(1));
    assertTrue(cloned.getRow(2).getCell(0) != testData.getRow(2).getCell(0));

    // Change cloned's metadata and see that the original data remains the same.
    cloned.addWarning(new Warning(ReasonType.OTHER, "baz"));
    cloned.addColumn(new ColumnDescription("foo", ValueType.BOOLEAN, "a"));
    cloned.getColumnDescription("A").setLabel("chacha");

    assertTrue(testData.getWarnings().isEmpty());
    assertEquals(4, testData.getRows().size());
    assertFalse(testData.containsColumn("foo"));
    assertEquals("col0", testData.getColumnDescription("A").getLabel());
  }

  public void testTableProperties() {
    DataTable dataEmpty = new DataTable();
    assertNull(dataEmpty.getCustomProperty("brandy"));

    dataEmpty.setCustomProperty("brandy", "cognac");
    assertEquals("cognac", dataEmpty.getCustomProperty("brandy"));
  }

  public void testToString() {
    DataTable dataTable = testData;
    assertEquals(
        "aaa,222.0,false,2001-11-14,12:11:13. 14,2000-11-01 01:10:23.432\nccc,111.0,true,"
        + "2001-02-14,12:30:13. 14,1000-12-01 01:10:23.432\nbbb,3.0,true,2012-03-14,12:11:03. "
        + "14,2000-02-01 01:10:31.004\nddd,222.0,false,1997-06-05,12:15:15. 14,3100-02-02 15:15:01",
        testData.toString());
  }

  public void testGetCellAndGetValue() {
    assertEquals(new TextValue("ccc"), testData.getValue(1, 0));
    TableCell cell = testData.getCell(1, 0);
    assertEquals("$ccc", cell.getFormattedValue());
  }
}
