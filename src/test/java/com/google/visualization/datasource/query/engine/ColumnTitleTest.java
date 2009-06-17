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

package com.google.visualization.datasource.query.engine;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.AggregationColumn;

import junit.framework.TestCase;

/**
 * Tests the functionality of ColumnTitle class.
 *
 * @author Yoav G.
 */
public class ColumnTitleTest extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Tests the createAggregationColumnDescription.
   */
  public void testCreateAggregationColumnDescription() {
    ColumnDescription columnDescription = new ColumnDescription("id",
        ValueType.TEXT, "label");


    ColumnTitle columnTitleCount = new ColumnTitle(Lists.<Value>newArrayList(),
        new AggregationColumn(new SimpleColumn("id"),
            AggregationType.COUNT), false);

    ColumnTitle columnTitleMin = new ColumnTitle(Lists.<Value>newArrayList(),
        new AggregationColumn(new SimpleColumn("id"),
            AggregationType.MIN), true);

    ColumnTitle columnTitleCountWithPivot =
        new ColumnTitle(Lists.newArrayList(new NumberValue(3.14),
            BooleanValue.TRUE), new AggregationColumn(new SimpleColumn("id"),
            AggregationType.COUNT), false);

    ColumnTitle columnTitleMinWithPivot =
        new ColumnTitle(Lists.newArrayList(new NumberValue(3.14),
            BooleanValue.TRUE), new AggregationColumn(new SimpleColumn("id"),
            AggregationType.MIN), true);


    ColumnDescription resultColumnDescriptionCount =
        columnTitleCount.createAggregationColumnDescription(columnDescription);

    ColumnDescription resultColumnDescriptionMin =
        columnTitleMin.createAggregationColumnDescription(columnDescription);

    ColumnDescription resultColumnDescriptionCountWithPivot =
        columnTitleCountWithPivot.createAggregationColumnDescription(columnDescription);

    ColumnDescription resultColumnDescriptionMinWithPivot =
        columnTitleMinWithPivot.createAggregationColumnDescription(columnDescription);

    // Check the creation of result column description with aggregation
    // type count.
    assertEquals("count-id", resultColumnDescriptionCount.getId());
    assertEquals("count label", resultColumnDescriptionCount.getLabel());
    assertEquals(ValueType.NUMBER, resultColumnDescriptionCount.getType());

    // Check the creation of result column description with aggregation
    // type min.
    assertEquals("min-id", resultColumnDescriptionMin.getId());
    assertEquals("min label", resultColumnDescriptionMin.getLabel());
    assertEquals(ValueType.TEXT, resultColumnDescriptionMin.getType());

    // Check the creation of result column description with aggregation
    // type count with pivot and single aggregation.
    assertEquals("3.14,true count-id", resultColumnDescriptionCountWithPivot.getId());
    assertEquals("3.14,true", resultColumnDescriptionCountWithPivot.getLabel());
    assertEquals(ValueType.NUMBER, resultColumnDescriptionCountWithPivot.getType());

    // Check the creation of result column description with aggregation
    // type min with pivot, multi-aggregation.
    assertEquals("3.14,true min-id",
        resultColumnDescriptionMinWithPivot.getId());
    assertEquals("3.14,true min label",
        resultColumnDescriptionMinWithPivot.getLabel());
    assertEquals(ValueType.TEXT, resultColumnDescriptionMinWithPivot.getType());
  }
}
