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
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.ScalarFunctionColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.scalarfunction.TimeComponentExtractor;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests the ScalarFunctionColumnTitleTest class.
 *
 * @author Liron L.
 */
public class ScalarFunctionColumnTitleTest extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Tests the createColumnDescription method.
   */
  public void testCreateColumnDescription() {
    ColumnDescription simpleColumnDescription =
        new ColumnDescription("simpleColumn", ValueType.DATE, "simpleLabel");
    ColumnDescription aggreationColumnDescription =
        new ColumnDescription("min-simpleColumn", ValueType.DATE,
            "aggLabel");
    DataTable table = new DataTable();
    table.addColumn(simpleColumnDescription);
    table.addColumn(aggreationColumnDescription);

    List<AbstractColumn> simpleColumns =
        Lists.newArrayList((AbstractColumn) new SimpleColumn("simpleColumn"));
    List<AbstractColumn> aggregationColumns =
        Lists.newArrayList((AbstractColumn) new AggregationColumn(
            new SimpleColumn("simpleColumn"), AggregationType.MIN));
    ScalarFunctionColumnTitle titleYear =
        new ScalarFunctionColumnTitle(Lists.<Value>newArrayList(),
            new ScalarFunctionColumn(simpleColumns,
                TimeComponentExtractor.getInstance(
                    TimeComponentExtractor.TimeComponent.YEAR)));

    ScalarFunctionColumnTitle titleSecondWithAggregation =
        new ScalarFunctionColumnTitle(Lists.<Value>newArrayList(),
            new ScalarFunctionColumn(aggregationColumns,
                TimeComponentExtractor.getInstance(
                    TimeComponentExtractor.TimeComponent.SECOND)));

    ScalarFunctionColumnTitle titleMonthWithPivot =
        new ScalarFunctionColumnTitle(Lists.newArrayList(new NumberValue(3.14),
            BooleanValue.TRUE), new ScalarFunctionColumn(simpleColumns,
            TimeComponentExtractor.getInstance(
                TimeComponentExtractor.TimeComponent.MONTH)));

    ScalarFunctionColumnTitle titleDayWithPivotAndAgg =
        new ScalarFunctionColumnTitle(Lists.newArrayList(new NumberValue(3.14),
            BooleanValue.TRUE), new ScalarFunctionColumn(aggregationColumns,
            TimeComponentExtractor.getInstance(
                TimeComponentExtractor.TimeComponent.DAY)));

    ColumnDescription resultColumnDescriptionYear = titleYear.createColumnDescription(table);

    ColumnDescription resultColumnDescriptionSecondWithAgg =
        titleSecondWithAggregation.createColumnDescription(table);

    ColumnDescription resultColumnDescriptionMonthWithPivot =
        titleMonthWithPivot.createColumnDescription(table);

    ColumnDescription resultColumnDescriptionDayWithPivotAndAgg =
        titleDayWithPivotAndAgg.createColumnDescription(table);

    // Check the creation of result column description with year.
    assertEquals("year_simpleColumn", resultColumnDescriptionYear.getId());
    assertEquals(" year(simpleLabel)", resultColumnDescriptionYear.getLabel());
    assertEquals(ValueType.NUMBER, resultColumnDescriptionYear.getType());

    // Check the creation of result column description with aggregation.
    assertEquals("second_min-simpleColumn",
        resultColumnDescriptionSecondWithAgg.getId());
    assertEquals(" second(aggLabel)",
        resultColumnDescriptionSecondWithAgg.getLabel());
    assertEquals(ValueType.NUMBER,
        resultColumnDescriptionSecondWithAgg.getType());

    // Check the creation of result column description with pivot.
    assertEquals("3.14,true month_simpleColumn",
        resultColumnDescriptionMonthWithPivot.getId());
    assertEquals("3.14,true month(simpleLabel)",
        resultColumnDescriptionMonthWithPivot.getLabel());
    assertEquals(ValueType.NUMBER,
        resultColumnDescriptionMonthWithPivot.getType());

    // Check the creation of result column description with aggregation
    // and pivot.
    assertEquals("3.14,true day_min-simpleColumn",
        resultColumnDescriptionDayWithPivotAndAgg.getId());
    assertEquals("3.14,true day(aggLabel)",
        resultColumnDescriptionDayWithPivotAndAgg.getLabel());
    assertEquals(ValueType.NUMBER,
        resultColumnDescriptionDayWithPivotAndAgg.getType());
  }
}
