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

import com.ibm.icu.util.ULocale;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.NumberValue;

import junit.framework.TestCase;

import java.util.Comparator;

/**
 * TableCell Tester.
 *
 * @author Hillel M.
 */
public class TableCellTest extends TestCase {

  private String storedFormattedValue;
  private DateValue dateValue;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    storedFormattedValue = "21-Feb-2007";
    dateValue = new DateValue(2007, 1, 21);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testGetFormattedValue() {
    TableCell cell = new TableCell(true);
    String formattedValue;

    // Test use of the default column formatter.
    formattedValue = cell.getFormattedValue();
    assertNull(formattedValue);

    cell.setFormattedValue("YES");
    formattedValue = cell.getFormattedValue();
    assertEquals("YES", formattedValue);
  }

  public void testConstructorString() {
    TableCell cell = new TableCell("string");
    assertNotNull(cell);
    assertEquals(new TextValue("string"), cell.getValue());
  }

  public void testConstructorBoolean() {
    TableCell cell = new TableCell(true);
    assertNotNull(cell);
    assertEquals(BooleanValue.TRUE, cell.getValue());
  }

  public void testConstructorDouble() {
    TableCell cell = new TableCell(34.43);
    assertNotNull(cell);
    assertEquals(new NumberValue(34.43), cell.getValue());
  }


  public void testConstructor() {
    TableCell cell = new TableCell(dateValue);
    assertNotNull(cell);
    assertEquals(dateValue, cell.getValue());

    cell = new TableCell(dateValue, storedFormattedValue);
    assertNotNull(cell);
    assertEquals(dateValue, cell.getValue());
  }

  public void testGetType() {
    TableCell cell = new TableCell(dateValue, storedFormattedValue);
    assertEquals(ValueType.DATE, cell.getType());
  }

  public void testGetNull() {
    TableCell cell = new TableCell(dateValue, storedFormattedValue);
    assertFalse(cell.isNull());
    cell = new TableCell(DateValue.getNullValue());
    assertTrue(cell.isNull());
  }

  public void testToString() {
    TableCell cell = new TableCell(BooleanValue.TRUE);
    assertEquals("true", cell.toString());
  }

  /**
   * Tests the localized comparator.
   */
  public void testLocalizedComparator() {
    // Test that strings are compared according to the given locale.
    Comparator<TableCell> rootComparator = TableCell.getLocalizedComparator(
        ULocale.ROOT);
    Comparator<TableCell> frComparator = TableCell.getLocalizedComparator(
        ULocale.FRENCH);
    TableCell cell1 = new TableCell(new TextValue("cot\u00E9"));
    TableCell cell2 = new TableCell(new TextValue("c\u00F4te"));
    assertEquals(-1, rootComparator.compare(cell1, cell2));
    assertEquals(1, frComparator.compare(cell1, cell2));

    // Test that number values are sorted correctly.
    TableCell numberCell2 = new TableCell((new NumberValue(2)));
    TableCell numberCell1 = new TableCell((new NumberValue(1)));
    assertEquals(-1, rootComparator.compare(numberCell1, numberCell2));

    // Test that value type must be the same.
    try {
      assertEquals(-1, rootComparator.compare(cell1, numberCell2));
      fail();
    } catch (RuntimeException e) {
    }
  }
  
  public void testClone() throws Exception {
    TableCell cell = new TableCell(new NumberValue(-2.3), "foobar23");
    TableCell cloned = cell.clone();
    
    assertEquals(-2.3, ((NumberValue) cloned.getValue()).getValue());
    assertEquals("foobar23", cloned.getFormattedValue());
    assertTrue(cloned.getCustomProperties().isEmpty());
    
    cloned.setCustomProperty("foo", "bar");
    cell.setCustomProperty("foo2", "bar2");
    cell.setFormattedValue("chuku23");
    
    assertEquals(1, cell.getCustomProperties().size());
    assertEquals(1, cloned.getCustomProperties().size());
    assertEquals("bar", cloned.getCustomProperty("foo"));
    assertEquals("bar2", cell.getCustomProperty("foo2"));
    assertEquals("foobar23", cloned.getFormattedValue());
  }
}
