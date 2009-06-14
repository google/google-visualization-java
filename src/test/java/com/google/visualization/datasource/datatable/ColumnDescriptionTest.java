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

import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;

/**
 * ColumnDescription Tester.
 *
 * @author Hillel M.
 */
public class ColumnDescriptionTest extends TestCase {

  private ColumnDescription columnDescription;

  public void testConstructor() {
    columnDescription = new ColumnDescription("col123", ValueType.TIMEOFDAY, "123");
    assertNotNull(columnDescription);
  }

  public void testGetId() {
    columnDescription = new ColumnDescription("col123", ValueType.TIMEOFDAY, "123");
    assertEquals("col123", columnDescription.getId());
  }

  public void testGetType() {
    columnDescription = new ColumnDescription("col123", ValueType.TIMEOFDAY, "123");
    assertEquals(ValueType.TIMEOFDAY, columnDescription.getType());
  }


  public void testGetLabel() {
    columnDescription = new ColumnDescription("col123", ValueType.TIMEOFDAY, "123");
    assertEquals("123", columnDescription.getLabel());
  }

  public void testColumnProperties() {
    columnDescription = new ColumnDescription("col123", ValueType.BOOLEAN, "123");
    assertNull(columnDescription.getCustomProperty("brandy"));

    columnDescription.setCustomProperty("brandy", "cognac");
    assertEquals("cognac", columnDescription.getCustomProperty("brandy"));
  }
}
