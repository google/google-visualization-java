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

package com.google.visualization.datasource.query;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.scalarfunction.ScalarFunction;
import com.google.visualization.datasource.query.scalarfunction.TimeComponentExtractor;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests for ScalarFunctionColumn.
 *
 * @author Liron L.
 */
public class ScalarFunctionColumnTest extends TestCase {

  private ScalarFunctionColumn scalarFunctionColumn;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    List<AbstractColumn> columns1 = Lists.newArrayList();
    ScalarFunction scalarFunction1 = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.MONTH);

    columns1.add(new SimpleColumn("col1"));
    columns1.add(new SimpleColumn("col2"));
    columns1.add(new AggregationColumn(new SimpleColumn("col3"),
        AggregationType.getByCode("sum")));

    ScalarFunctionColumn innerScalarFunctionColumn =
        new ScalarFunctionColumn(columns1, scalarFunction1);

    List<AbstractColumn> columns = Lists.newArrayList();
    ScalarFunction scalarFunction = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.YEAR);

    columns.add(new SimpleColumn("col4"));
    columns.add(new SimpleColumn("col5"));
    columns.add(innerScalarFunctionColumn);
    columns.add(new AggregationColumn(new SimpleColumn("col6"),
        AggregationType.getByCode("sum")));
    columns.add(new SimpleColumn("col7"));

    scalarFunctionColumn = new ScalarFunctionColumn(columns, scalarFunction);
  }

  public void testGetAllSimpleColumnIds() {
    List<String> expectedColumnIds = Lists.newArrayList("col4", "col5", "col1",
        "col2", "col3", "col6", "col7");
    List<String> columnIds = scalarFunctionColumn.getAllSimpleColumnIds();
    assertEquals(expectedColumnIds, columnIds);
  }

  public void testGetValue() {
    ScalarFunction scalarFunction = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.YEAR);

    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("dateCol", ValueType.DATE, "dateCol"));
    table.addColumn(new ColumnDescription("numberCol", ValueType.NUMBER, "numberCol"));
    table.addColumn(new ColumnDescription("timeOfDayCol", ValueType.TIMEOFDAY, "timeOfDayCol"));
    table.addColumn(new ColumnDescription("dateTimeCol", ValueType.DATETIME, "dateTimeCol"));

    TableRow row = new TableRow();
    row.addCell(new TableCell(new DateValue(2008, 5, 3)));
    row.addCell(new TableCell(new NumberValue(23)));
    row.addCell(new TableCell(new TimeOfDayValue(13, 12, 11)));
    row.addCell(new TableCell(new DateTimeValue(2007, 3, 4, 2, 6, 23, 120)));

    // Check date value.
    List<AbstractColumn> columns =
        Lists.newArrayList((AbstractColumn) new SimpleColumn("dateCol"));
     ScalarFunctionColumn sfc =
        new ScalarFunctionColumn(columns, scalarFunction);
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    Value value = sfc.getValue(lookup, row);
    Value expectedValueFromDate = new NumberValue(2008);
    assertEquals(expectedValueFromDate, value);

    // Check datetime value.
    List<AbstractColumn> columns1 =
        Lists.newArrayList((AbstractColumn) new SimpleColumn("dateTimeCol"));
    sfc = new ScalarFunctionColumn(columns1, scalarFunction);
    lookup =  new DataTableColumnLookup(table);
    value = sfc.getValue(lookup, row);
    Value expectedValueFromDateTime = new NumberValue(2007);
    assertEquals(value, expectedValueFromDateTime);

    // Check bad input (timeofday value).
    List<AbstractColumn> columns2 =
        Lists.newArrayList((AbstractColumn) new SimpleColumn("timeOfDayCol"));
    sfc = new ScalarFunctionColumn(columns2, scalarFunction);
    try {
      sfc.validateColumn(table);
    } catch (DataSourceException e) {
      // Expected behavior.
    }
  }

  public void testGetAllSimpleColumns() {
    List<SimpleColumn> simpleColumns =
        scalarFunctionColumn.getAllSimpleColumns();
    List<SimpleColumn> expectedSimpleColumns = Lists.newArrayList();
    expectedSimpleColumns.add(new SimpleColumn("col4"));
    expectedSimpleColumns.add(new SimpleColumn("col5"));
    expectedSimpleColumns.add(new SimpleColumn("col1"));
    expectedSimpleColumns.add(new SimpleColumn("col2"));
    expectedSimpleColumns.add(new SimpleColumn("col7"));
    assertEquals(expectedSimpleColumns, simpleColumns);
  }

  public void testGetAllAggregationColumns() {
    List<AggregationColumn> aggregationColumns =
        scalarFunctionColumn.getAllAggregationColumns();
    List<AggregationColumn> expectedAggregationColumns = Lists.newArrayList();
    expectedAggregationColumns.add(new AggregationColumn(
        new SimpleColumn("col3"), AggregationType.getByCode("sum")));
    expectedAggregationColumns.add(
        new AggregationColumn(new SimpleColumn("col6"),
            AggregationType.getByCode("sum")));
    assertEquals(expectedAggregationColumns, aggregationColumns);
  }

  public void testGetAllScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns =
        scalarFunctionColumn.getAllScalarFunctionColumns();
    List<ScalarFunctionColumn> expectedScalarFunctionColumns =
        Lists.newArrayList();

    List<AbstractColumn> columns1 = Lists.newArrayList();
    ScalarFunction scalarFunction1 = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.MONTH);

    columns1.add(new SimpleColumn("col1"));
    columns1.add(new SimpleColumn("col2"));
    columns1.add(new AggregationColumn(new SimpleColumn("col3"),
        AggregationType.getByCode("sum")));

    ScalarFunctionColumn innerScalarFunctionColumn =
        new ScalarFunctionColumn(columns1, scalarFunction1);

    expectedScalarFunctionColumns.add(scalarFunctionColumn);
    expectedScalarFunctionColumns.add(innerScalarFunctionColumn);
    assertEquals(expectedScalarFunctionColumns, scalarFunctionColumns);
  }

  public void testGetValueType() {
    ScalarFunction scalarFunction = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.YEAR);
    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("dateCol", ValueType.DATE, "dateCol"));
     List<AbstractColumn> columns =
        Lists.newArrayList((AbstractColumn) new SimpleColumn("dateCol"));
     ScalarFunctionColumn sfc = new ScalarFunctionColumn(columns, scalarFunction);
    ValueType valueType = sfc.getValueType(table);
    assertEquals(ValueType.NUMBER, valueType);
  }
  
  public void testGetId() {
    assertEquals("year_col4,col5,month_col1,col2,sum-col3,sum-col6,col7",
                 scalarFunctionColumn.getId());
  }
}
