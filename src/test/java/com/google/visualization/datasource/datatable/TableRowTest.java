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

import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;

import junit.framework.TestCase;

/**
 * Tests for TableRow
 *
 * @author Yonatan B.Y.
 */
public class TableRowTest extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testClone() throws Exception {
    TableRow row = new TableRow();
    row.addCell(true);
    row.addCell(BooleanValue.getNullValue());
    row.addCell(-2.3);
    row.addCell("foo");
    row.addCell(new DateValue(2008, 2, 3));
    row.getCell(2).setFormattedValue("bar-2.3");
    row.getCell(4).setCustomProperty("foo", "bar");
    
    TableRow clonedRow = row.clone();
    
    assertEquals(true, ((BooleanValue) clonedRow.getCell(0).getValue()).getValue());
    assertTrue(clonedRow.getCell(1).isNull());
    assertEquals(-2.3, ((NumberValue) clonedRow.getCell(2).getValue()).getValue());
    assertEquals("foo", ((TextValue) clonedRow.getCell(3).getValue()).getValue());
    assertEquals(3, ((DateValue) clonedRow.getCell(4).getValue()).getDayOfMonth());
    assertEquals("bar-2.3", clonedRow.getCell(2).getFormattedValue());
    assertEquals("bar", clonedRow.getCell(4).getCustomProperty("foo"));
    assertEquals(1, clonedRow.getCell(4).getCustomProperties().size());
    
    assertTrue(clonedRow != row);
    assertTrue(clonedRow.getCell(0) != row.getCell(0));
    assertTrue(clonedRow.getCell(1) != row.getCell(1));
    assertTrue(clonedRow.getCell(4).getCustomProperties() != row.getCell(4).getCustomProperties());
    
    row.getCell(0).setCustomProperty("foo2", "bar2");
    assertTrue(clonedRow.getCell(0).getCustomProperties().isEmpty());
    
    clonedRow.getCell(3).setCustomProperty("foo3", "bar3");
    assertTrue(row.getCell(3).getCustomProperties().isEmpty());
  }
}
