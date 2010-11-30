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

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryFormat;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QueryLabels;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.SimpleColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * A utility class for splitting the user query into a data source query and a completion query.
 * The data source query is executed  by the data source, the completion query is then
 * executed by the query engine.
 * The splitting is performed based on the capabilities that the data source declares it can
 * handle.
 *
 * @author Yonatan B.Y.
 *
 */
public final class QuerySplitter {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(QuerySplitter.class.getName());

  /**
   * Private constructor.
   */
  private QuerySplitter() {}

  /**
   * Split the query into a data source query and completion query. The data source query runs
   * first directly on the underlying data. The completion query is run by
   * {@link com.google.visualization.datasource.query.engine.QueryEngine} engine on the result of
   * the data source query.
   *
   * @param query The <code>Query</code> to split.
   * @param capabilities The capabilities supported by the data source.
   *
   * @return A split query.
   *
   * @throws DataSourceException Thrown if the capabilities are not supported.
   */
  public static QueryPair splitQuery(Query query, Capabilities capabilities)
      throws DataSourceException {
    switch (capabilities) {
      case ALL:
        return splitAll(query);
      case NONE:
        return splitNone(query);
      case SQL:
        return splitSQL(query);
      case SORT_AND_PAGINATION:
        return splitSortAndPagination(query);
      case SELECT:
        return splitSelect(query);
    }
    log.error("Capabilities not supported.");
    throw new DataSourceException(ReasonType.NOT_SUPPORTED, "Capabilities not supported.");
  }

  /**
   * Splits the query for a data source with capabilities ALL. In this case, the original query is
   * copied to the data source query and the original query is empty.
   *
   * @param query The query to split.
   *
   * @return The split query.
   */
  private static QueryPair splitAll(Query query) {
    Query dataSourceQuery = new Query();
    dataSourceQuery.copyFrom(query);
    Query completionQuery = new Query();
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  /**
   * Splits the query for a data source with capabilities NONE. In this case, the original query is
   * copied to the completionQuery and the data source query is empty. Furthermore, the data source
   * query is assigned null and shouldn't be used or referenced by the data source.
   *
   * @param query The query to split.
   *
   * @return The split query.
   */
  private static QueryPair splitNone(Query query) {
    Query completionQuery = new Query();
    completionQuery.copyFrom(query);
    return new QueryPair(null, completionQuery);
  }

  /**
   * Splits the query for a data source with capabilities SQL.
   * If the query contains scalar functions, then the query is split as if data source capabilities
   * are NONE. If the query does not contain scalar functions, then the data source query contains
   * most of the operations.
   * Because SQL cannot handle pivoting, special care needs to be taken if the query includes a
   * pivot operation. The aggregation operation required for pivoting is passed to the data source
   * query. We make use of this, with some implementation tricks. See implementation comments.
   *
   * @param query The original query.
   *
   * @return The split query.
   */
  private static QueryPair splitSQL(Query query) {
    // Situations we currently do not support good splitting of:
    // - Queries with scalar functions.
    // - Queries with pivot that also contain labels or formatting on aggregation columns.
    if (!query.getAllScalarFunctionsColumns().isEmpty() 
        || (query.hasPivot()
            && ((query.hasUserFormatOptions() &&
                !query.getUserFormatOptions().getAggregationColumns().isEmpty())
             || (query.hasLabels() && !query.getLabels().getAggregationColumns().isEmpty())))) {
      Query completionQuery = new Query();
      completionQuery.copyFrom(query);
      return new QueryPair(new Query(), completionQuery);
    }

    Query dataSourceQuery = new Query();
    Query completionQuery = new Query();

    // sql supports select, where, sort, group, limit, offset.
    // The library further supports pivot.
    if (query.hasPivot()) {
      // Make the pivot columns additional grouping columns, and handle the
      // transformation later.

      List<AbstractColumn> pivotColumns = query.getPivot().getColumns();

      dataSourceQuery.copyFrom(query);
      dataSourceQuery.setPivot(null);
      dataSourceQuery.setSort(null);
      dataSourceQuery.setOptions(null);
      dataSourceQuery.setLabels(null);
      dataSourceQuery.setUserFormatOptions(null);

      try {
        dataSourceQuery.setRowSkipping(0);
        dataSourceQuery.setRowLimit(-1);
        dataSourceQuery.setRowOffset(0);
      } catch (InvalidQueryException e) {
        // Should not happen.
      }

      // Let the data source group by all grouping/pivoting columns, and let it
      // select all selection/pivoting columns, e.g., SELECT A, max(B) GROUP BY A PIVOT C turns
      // into SELECT A, max(B), C GROUP BY A, C

      List<AbstractColumn> newGroupColumns = Lists.newArrayList();
      List<AbstractColumn> newSelectionColumns = Lists.newArrayList();
      if (dataSourceQuery.hasGroup()) {
        // Same logic applies here, no calculated columns and no aggregations.
        newGroupColumns.addAll(dataSourceQuery.getGroup().getColumns());
      }
      newGroupColumns.addAll(pivotColumns);
      if (dataSourceQuery.hasSelection()) {
        newSelectionColumns.addAll(dataSourceQuery.getSelection().getColumns());
      }
      newSelectionColumns.addAll(pivotColumns);
      QueryGroup group = new QueryGroup();
      for (AbstractColumn col : newGroupColumns) {
        group.addColumn(col);
      }
      dataSourceQuery.setGroup(group);
      QuerySelection selection = new QuerySelection();
      for (AbstractColumn col : newSelectionColumns) {
        selection.addColumn(col);
      }
      dataSourceQuery.setSelection(selection);

      // Build the completion query to group by the grouping columns. Because an aggregation is
      // required, make a dummy aggregation on the original column by which the aggregation is
      // required.
      // This original column must be unique for a given set of values for the grouping/pivoting
      // columns so any aggregation operation out of MIN, MAX, AVG will return the value
      // itself and will not aggregate anything. The example from before,
      // SELECT A, max(B) GROUP BY A PIVOT C turns into SELECT A, min(max-B) GROUP BY A PIVOT C

      completionQuery.copyFrom(query);
      completionQuery.setFilter(null);

      QuerySelection completionSelection = new QuerySelection();
      List<AbstractColumn> originalSelectedColumns =
          query.getSelection().getColumns();
      for (int i = 0; i < originalSelectedColumns.size(); i++) {
        AbstractColumn column = originalSelectedColumns.get(i);
        if (query.getGroup().getColumns().contains(column)) {
          completionSelection.addColumn(column);
        } else { // Must be an aggregation column if doesn't appear in the grouping.
          // The id here is the id generated by the data source for the column containing
          // the aggregated data, e.g., max-B.
          String id = column.getId();
          // MIN is chosen arbitrarily, because there will be exactly one.
          completionSelection.addColumn(
              new AggregationColumn(new SimpleColumn(id), AggregationType.MIN));
        }
      }

      completionQuery.setSelection(completionSelection);
    } else {
      // When there is no pivoting, sql does everything (except skipping, options, labels, format).
      dataSourceQuery.copyFrom(query);
      dataSourceQuery.setOptions(null);
      completionQuery.setOptions(query.getOptions());
      try {
        // If there is skipping pagination should be done in the completion query
        if (query.hasRowSkipping()) {
          dataSourceQuery.setRowSkipping(0);
          dataSourceQuery.setRowLimit(-1);
          dataSourceQuery.setRowOffset(0);
          
          completionQuery.copyRowSkipping(query);
          completionQuery.copyRowLimit(query);
          completionQuery.copyRowOffset(query); 
        }
        if (query.hasLabels()) {
          dataSourceQuery.setLabels(null);
          QueryLabels labels = query.getLabels();
          QueryLabels newLabels = new QueryLabels();
          for (AbstractColumn column : labels.getColumns()) {
            newLabels.addLabel(new SimpleColumn(column.getId()), labels.getLabel(column));
          }
          completionQuery.setLabels(newLabels);
        }
        if (query.hasUserFormatOptions()) {
          dataSourceQuery.setUserFormatOptions(null);
          QueryFormat formats = query.getUserFormatOptions();
          QueryFormat newFormats = new QueryFormat();
          for (AbstractColumn column : formats.getColumns()) {
            newFormats.addPattern(new SimpleColumn(column.getId()), formats.getPattern(column));
          }
          completionQuery.setUserFormatOptions(newFormats);
        }
      } catch (InvalidQueryException e) {
        // Should not happen.
      }
    }
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  /**
   * Splits the query for a data source with capabilities SORT_AND_PAGINATION.
   * Algorithm: if the query has filter, grouping, pivoting or skipping requirements the query is
   * split as in the NONE case.
   * If the query does not have filter, grouping, pivoting or skipping the data source query
   * receives any sorting or pagination requirements and the completion query receives
   * any selection requirements.
   *
   * @param query The query to split.
   *
   * @return The split query.
   */
  private static QueryPair splitSortAndPagination(Query query) {
    if (!query.getAllScalarFunctionsColumns().isEmpty()) {
      Query completionQuery = new Query();
      completionQuery.copyFrom(query);
      return new QueryPair(new Query(), completionQuery);
    }

    Query dataSourceQuery = new Query();
    Query completionQuery = new Query();
    if (query.hasFilter() || query.hasGroup() || query.hasPivot()) {
      // The query is copied to the completion query.
      completionQuery.copyFrom(query);
    } else {
      // The execution order of the 3 relevant operators is:
      // sort -> skip -> paginate (limit and offset).
      // Skipping is not a possible data source capability, Therefore:
      // 1. Sorting can be performed in the data source query.
      // 2. Pagination should be performed in the data source query IFF skipping
      //    isn't stated in the original query.
      dataSourceQuery.setSort(query.getSort());     
      if (query.hasRowSkipping()) {
        completionQuery.copyRowSkipping(query);
        completionQuery.copyRowLimit(query);
        completionQuery.copyRowOffset(query);
      } else {
        dataSourceQuery.copyRowLimit(query);
        dataSourceQuery.copyRowOffset(query);
      }

      completionQuery.setSelection(query.getSelection());
      completionQuery.setOptions(query.getOptions());
      completionQuery.setLabels(query.getLabels());
      completionQuery.setUserFormatOptions(query.getUserFormatOptions());
    }
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  /**
   * Splits the query for a data source with capabilities SELECT.
   * Algorithm: the data source query receives any select operation from the query, however the
   * completion query also receives selection to properly post-process the result.
   *
   * @param query The query to split.
   *
   * @return The split query.
   */
  private static QueryPair splitSelect(Query query) {
    Query dataSourceQuery = new Query();
    Query completionQuery = new Query();
    if (query.getSelection() != null) {
      QuerySelection selection = new QuerySelection();
      for (String simpleColumnId : query.getAllColumnIds()) {
        selection.addColumn(new SimpleColumn(simpleColumnId));
      }
      // Column selection can be empty. For example, for query "SELECT 1".
      dataSourceQuery.setSelection(selection);
    }

    completionQuery.copyFrom(query);
    return new QueryPair(dataSourceQuery, completionQuery);
  }
}
