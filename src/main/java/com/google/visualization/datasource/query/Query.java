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
import com.google.common.collect.Sets;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.MessagesEnum;

import com.ibm.icu.util.ULocale;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * Holds the query data and clauses. This class is the result of parsing the query
 * string that comes from the user.
 * The clauses that can be included in a query are: select, filter, sort, group by, pivot, options,
 * labels, format, limit, offset and skipping.
 * For more details see the
 * <a href="http://code.google.com/apis/visualization/documentation/querylanguage.html">
 * query language reference
 * </a>.
 * A Query can be executed by the query engine
 * (see: {@link com.google.visualization.datasource.DataSourceHelper#applyQuery}).
 * It can be split in 2 Query objects based on the Capabilities value
 * (see: {@link com.google.visualization.datasource.DataSourceHelper#splitQuery}).
 *
 * Note on errors: Since this class handles a user generated query, all errors
 * (of type INVALID_QUERY) should provide a descriptive error message for both
 * the user.
 * 
 * @author Itai R.
 * @author Yonatan B.Y.
 */
public class Query {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(Query.class.getName());

  /**
   * Checks the given list for duplicates. Throws an exception if a duplicate
   * is found, giving as a message a description containing information on which
   * item was found to be duplicate, and in which clause it occurred (the
   * clause name is given).
   *
   * @param selectionColumns The list to check for duplicates.
   * @param clauseName The clause name to report in the exception message.
   * @param userLocale The user locale.
   *
   * @throws InvalidQueryException Thrown if a duplicate was found.
   */
  private static<T> void checkForDuplicates(List<T>
      selectionColumns, String clauseName, ULocale userLocale) throws InvalidQueryException {
    for (int i = 0; i < selectionColumns.size(); i++) {
      T col = selectionColumns.get(i);
      for (int j = i + 1; j < selectionColumns.size(); j++) {
        if (col.equals(selectionColumns.get(j))) {
          String[] args = {col.toString(), clauseName};
          String messageToLogAndUser = MessagesEnum.COLUMN_ONLY_ONCE.getMessageWithArgs(userLocale, 
              args);
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
  }

  /**
   * The required sort order.
   */
  private QuerySort sort = null;

  /**
   * The required selection.
   * If the selection is null, or is empty, the original
   * selection that was defined in the report is used.
   */
  protected QuerySelection selection = null;

  /**
   * The required filter.
   * If the filter is null, then the results are not filtered at all.
   */
  private QueryFilter filter = null;

  /**
   * The required grouping.
   */
  private QueryGroup group = null;

  /**
   * The required pivoting.
   */
  private QueryPivot pivot = null;

  /**
   * The number of rows to skip when selecting only a subset of the rows using
   * skipping clause, e.g., skipping 4.
   * If a skipping clause, "skip k",  is added to the query, the resulting table
   * will be consisted of the first row of every k rows of the table returned by
   * the earlier part of the query, when k corresponds to rowSkipping value.
   * For example, if rowSkipping = 10 the returned table will consist of rows 0, 10...
   * The default value is 0, meaning no skipping should be performed.
   */
  private int rowSkipping = 0;
  
  /**
   * Max number of rows to return to caller.
   * If the caller specified this parameter, and the data table returned from
   * the data source contains more than this number of rows, then the result is
   * truncated, and the query result contains a warning with this reason.
   * If this value is set to -1, which can only be done internally, this is ignored.
   * (0 is a legal value, denoting no rows should be retrieved).
   */
  private int rowLimit = -1;

  /**
   * The number of rows that should be removed from the beginning of the
   * data table.
   * Together with the row limit parameter, this enables pagination.
   * The default value is 0, (skip 0 rows) - which means start from the
   * first row.
   */
  private int rowOffset = 0;

  /**
   * Additional options for the query.
   */
  private QueryOptions options = null;

  /**
   * Column labels as specified in the query.
   */
  private QueryLabels labels = null;

  /**
   * Column formatting patterns as specified in the query.
   */
  private QueryFormat userFormatOptions = null;
  
  /**
   * The user locale, Used to create localized messages.
   */
  private ULocale localeForUserMessages = null;

  /**
   * Constructs a new, empty, query.
   */
  public Query() {
  }

  /**
   * Set the required sort of the query result.
   *
   * @param sort The required sort of the query result.
   */
  public void setSort(QuerySort sort) {
    this.sort = sort;
  }

  /**
   * Returns the required sort of the query result.
   *
   * @return The required sort of the query result.
   */
  public QuerySort getSort() {
    return sort;
  }

  /**
   * Returns true if the query has a non empty sort-by section.
   *
   * @return True if the query has a non empty sort-by section.
   */
  public boolean hasSort() {
    return (sort != null) && (!sort.isEmpty());
  }

  /**
   * Set the required selection of the query result.
   *
   * @param selection The required selection of the query result.
   */
  public void setSelection(QuerySelection selection) {
    this.selection = selection;
  }

  /**
   * Returns the required selection of the query result.
   *
   * @return The required selection of the query result.
   */
  public QuerySelection getSelection() {
    return selection;
  }

  /**
   * Returns true if there is a non-trivial selection, i.e. if not all columns
   * should be selected.
   *
   * @return True if there is a non-trivial selection.
   */
  public boolean hasSelection() {
    return (selection != null) && (!selection.isEmpty());
  }

  /**
   * Set the required filter of this query.
   *
   * @param filter The required filter of this query.
   */
  public void setFilter(QueryFilter filter) {
    this.filter = filter;
  }

  /**
   * Returns the required filter of this query.
   *
   * @return The required filter of this query.
   */
  public QueryFilter getFilter() {
    return filter;
  }

  /**
   * Returns whether or not this query has a filter defined.
   *
   * @return true if this query has a filter defined.
   */
  public boolean hasFilter() {
    return (filter != null);
  }

  /**
   * Sets the required group of the query result.
   *
   * @param group The required group of the query result.
   */
  public void setGroup(QueryGroup group) {
    this.group = group;
  }

  /**
   * Sets the required pivot of the query result.
   *
   * @param pivot The required pivot of the query result.
   */
  public void setPivot(QueryPivot pivot) {
    this.pivot = pivot;
  }

  /**
   * Returns the required group of the query result.
   *
   * @return The required group of the query result.
   */
  public QueryGroup getGroup() {
    return group;
  }

  /**
   * Returns true if the query has a group-by section.
   *
   * @return True if the query has a group-by section.
   */
  public boolean hasGroup() {
    return group != null && !group.getColumnIds().isEmpty();
  }

  /**
   * Returns the required pivot of the query result.
   *
   * @return The required pivot of the query result.
   */
  public QueryPivot getPivot() {
    return pivot;
  }

  /**
   * Returns true if the query has a pivot-by section.
   *
   * @return True if the query has a pivot-by section.
   */
  public boolean hasPivot() {
    return pivot != null && !pivot.getColumnIds().isEmpty();
  }

  /**
   * Returns the number of rows to skip when selecting only a subset of
   * the rows using skipping clause, e.g., skipping 4.
   * The default value is 0, meaning no skipping should be performed.
   *
   * @return The number of rows to skip between each row selection.
   */
  public int getRowSkipping() {
    return rowSkipping;
  }

  /**
   * Sets the number of rows to skip when selecting only a subset of
   * the rows using skipping clause, e.g., skipping 4.
   * The default value is 0, meaning no skipping should be performed.
   *
   * If there is is an attempt to set the skipping value to non positive value,
   * then an InvalidQueryException is thrown.
   *
   * @param rowSkipping The number of rows to skip between each row selection.
   *        0 value means no skipping.
   *
   * @throws InvalidQueryException Thrown if an invalid value is specified.
   */
  public void setRowSkipping(int rowSkipping) throws InvalidQueryException {
    if (rowSkipping < 0) {
      String messageToLogAndUser = MessagesEnum.INVALID_SKIPPING.getMessageWithArgs(
          localeForUserMessages, Integer.toString(rowSkipping));
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    this.rowSkipping = rowSkipping;
  }

  /**
   * Sets the number of rows to skip when selecting only a subset of
   * the row, based on another query.
   * 
   * @param originalQuery The query from which the row skipping should be taken.
   */
  public void copyRowSkipping(Query originalQuery) {
    rowSkipping = originalQuery.getRowSkipping();
  }

  /**
   * Returns true if this query has a row skipping set. A value of 0 means no
   * skipping.
   *
   * @return True if this query has a row skipping set.
   */
  public boolean hasRowSkipping() {
    return rowSkipping > 0;
  }

  /**
   * Returns the maximum number of rows to return to the caller.
   * If the caller specified this parameter, and the data table returned from
   * the data source contains more than this number of rows, then the result is
   * truncated, and the query result contains a warning with this reason.
   * If this value is set to -1 it is just ignored, but this can only be done
   * internally.
   *
   * @return The maximum number of rows to return to the caller.
   */
  public int getRowLimit() {
    return rowLimit;
  }

  /**
   * Sets the max number of rows to return to the caller.
   * If the caller specified this parameter, and the data table returned from
   * the data source contains more than this number of rows, then the result is
   * truncated, and the query result contains a warning with this reason.
   * By default, this is set to -1, which means that no limit is requested.
   *
   * If there is is an attempt to set the limit value to any negative number
   * smaller than -1 (which means no limit), then an InvalidQueryException is
   * thrown.
   *
   * @param rowLimit The max number of rows to return.
   *
   * @throws InvalidQueryException Thrown if an invalid value is specified.
   */
  public void setRowLimit(int rowLimit) throws InvalidQueryException {
    if (rowLimit < -1) {
      String messageToLogAndUser = "Invalid value for row limit: " + rowLimit;
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    this.rowLimit = rowLimit;
  }

  /**
   * Sets the max number of rows to return to the caller, based on another
   * query.
   *
   * @param originalQuery The query from which the row limit should be taken.
   */
  public void copyRowLimit(Query originalQuery) {
    rowLimit = originalQuery.getRowLimit();
  }

  /**
   * Returns true if this query has a row limit set. A value of -1 means no
   * limit so in case of -1 this function returns true.
   *
   * @return True if this query has a row limit set.
   */
  public boolean hasRowLimit() {
    return rowLimit > -1;
  }

  /**
   * Returns the number of rows that should be removed from the beginning of the
   * data table.
   * Together with the row limit parameter, this enables pagination.
   * The default value is 0, (skip 0 rows) - which means to start from the
   * first row.
   *
   * @return The number of rows to skip from the beginning of the table.
   */
  public int getRowOffset() {
    return rowOffset;
  }

  /**
   * Sets the number of rows that should be removed from the beginning of the
   * data table.
   * Together with the row limit parameter, this enables pagination.
   * The default value is 0, (skip 0 rows) - which means to start from the
   * first row.
   *
   * If there is is an attempt to set the offset value to any negative number,
   * then an InvalidQueryException is thrown.
   *
   * @param rowOffset The number of rows to skip from the beginning of the
   *     table.
   *
   * @throws InvalidQueryException Thrown if an invalid value is specified.
   */
  public void setRowOffset(int rowOffset) throws InvalidQueryException {
    if (rowOffset < 0) {
      String messageToLogAndUser = MessagesEnum.INVALID_OFFSET.getMessageWithArgs(
          localeForUserMessages, Integer.toString(rowOffset));
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    this.rowOffset = rowOffset;
  }

  /**
   * Sets the number of rows to skip, based on another query.
   *
   * @param originalQuery The query from which the row offset should be taken.
   */
  public void copyRowOffset(Query originalQuery) {
    rowOffset = originalQuery.getRowOffset();
  }

  /**
   * Returns true if this query has a row offset set. A value of 0 means no
   * offset so in case of 0 this function returns true.
   *
   * @return True if this query has a row offset set.
   */
  public boolean hasRowOffset() {
    return rowOffset > 0;
  }

  /**
   * Returns the query formatting options.
   *
   * @return The query formatting options.
   */
  public QueryFormat getUserFormatOptions() {
    return userFormatOptions;
  }

  /**
   * Sets the user formatting options map.
   * @param userFormatOptions A map of patterns to column IDs.
   */
  public void setUserFormatOptions(QueryFormat userFormatOptions) {
    this.userFormatOptions = userFormatOptions;
  }

  /**
   * Returns true if this query has user format options defined.
   *
   * @return True if this query has user format options defined.
   */
  public boolean hasUserFormatOptions() {
    return (userFormatOptions != null)
        && (!userFormatOptions.getColumns().isEmpty());
  }

  /**
   * Returns the query labels.
   *
   * @return The query labels.
   */
  public QueryLabels getLabels() {
    return labels;
  }

  /**
   * Sets the query labels.
   * @param labels A map of labels to column IDs.
   */
  public void setLabels(QueryLabels labels) {
    this.labels = labels;
  }

  /**
   * Returns true if this query has labels defined.
   *
   * @return True if this query has labels defined.
   */
  public boolean hasLabels() {
    return (labels != null)
        && (!labels.getColumns().isEmpty());
  }

  /**
   * Returns the options for this query.
   *
   * @return The options for this query.
   */
  public QueryOptions getOptions() {
    return options;
  }

  /**
   * Sets the required options for this query.
   *
   * @param options The required options.
   */
  public void setOptions(QueryOptions options) {
    this.options = options;
  }

  /**
   * Returns true if this query has any nondefault options set.
   *
   * @return True if this query has any nondefault options set.
   */
  public boolean hasOptions() {
    return (options != null)
        && (!options.isDefault());
  }

  /**
   * Returns true if the query is empty, i.e. has no clauses or all the clauses are empty.
   *
   * @return true if the query is empty. 
   */
  public boolean isEmpty() {
    return (!hasSort() && !hasSelection() && !hasFilter() && !hasGroup() && !hasPivot()
            && !hasRowSkipping() && !hasRowLimit() && !hasRowOffset()
            && !hasUserFormatOptions()  && !hasLabels() && !hasOptions());
  }
  
  
  /**
   * Sets the user locale for creating localized messages.
   * @param userLocale the user locale.
   */
  public void setLocaleForUserMessages(ULocale localeForUserMessges) {
    this.localeForUserMessages = localeForUserMessges;
  }

  /**
   * Copies all information from the given query to this query.
   *
   * @param query The query to copy from.
   */
  public void copyFrom(Query query) {
    setSort(query.getSort());
    setSelection(query.getSelection());
    setFilter(query.getFilter());
    setGroup(query.getGroup());
    setPivot(query.getPivot());
    copyRowSkipping(query);
    copyRowLimit(query);
    copyRowOffset(query);
    setUserFormatOptions(query.getUserFormatOptions());
    setLabels(query.getLabels());
    setOptions(query.getOptions());
  }

  /**
   * Validates the query. Runs a sanity check on the query, verifies that there are no
   * duplicates, and that the query follows a basic set of rules required for its execution.
   * 
   * Specifically, verifies the following:
   * - There are no duplicate columns in the clauses.
   * - No column appears both as a selection and as an aggregation.
   * - When aggregation is used, checks that all selected columns are valid (a column is valid if
   *   it is either grouped-by, or is a scalar function column the arguments of which are all 
   *   valid columns).
   * - No column is both grouped-by, and aggregated in, the select clause.
   * - No column both appears as pivot, and aggregated in, the select clause.
   * - No grouping/pivoting is used when there is no aggregation in the select clause.
   * - If aggregation is used in the select clause, no column is ordered-by that is not in the
   *   select clause.
   * - No column appears both as a group-by and a pivot.
   * - If pivoting is used, the order by clause does not contain aggregation columns.
   * - The order-by clause does not contain aggregation columns that were not defined in the select
   *   clause.
   *   
   * @throws InvalidQueryException
   */
  public void validate() throws InvalidQueryException {
    // Set up some variables.
    List<String> groupColumnIds =
        hasGroup() ? group.getColumnIds() : Lists.<String>newArrayList();
    List<AbstractColumn> groupColumns = hasGroup() ? group.getColumns() :
        Lists.<AbstractColumn>newArrayList();
    List<String> pivotColumnIds =
        hasPivot() ? pivot.getColumnIds() : Lists.<String>newArrayList();
    List<AbstractColumn> selectionColumns = hasSelection()
        ? selection.getColumns() : Lists.<AbstractColumn>newArrayList();
    List<AggregationColumn> selectionAggregated =
        hasSelection() ? selection.getAggregationColumns() :
            Lists.<AggregationColumn>newArrayList();
    List<SimpleColumn> selectionSimple = hasSelection()
        ? selection.getSimpleColumns() : Lists.<SimpleColumn>newArrayList();
    List<ScalarFunctionColumn> selectedScalarFunctionColumns = hasSelection()
        ? selection.getScalarFunctionColumns()
        : Lists.<ScalarFunctionColumn>newArrayList();
    selectedScalarFunctionColumns.addAll(selectedScalarFunctionColumns);
    List<AbstractColumn> sortColumns = hasSort() ? sort.getColumns() :
        Lists.<AbstractColumn>newArrayList();
    List<AggregationColumn> sortAggregated = hasSort()
        ? sort.getAggregationColumns()
        : Lists.<AggregationColumn>newArrayList();

    // Check for duplicates.
    checkForDuplicates(selectionColumns, "SELECT", localeForUserMessages);
    checkForDuplicates(sortColumns, "ORDER BY", localeForUserMessages);
    checkForDuplicates(groupColumnIds, "GROUP BY", localeForUserMessages);
    checkForDuplicates(pivotColumnIds, "PIVOT", localeForUserMessages);

    // Cannot have aggregations in either group by, pivot, or where.
    if (hasGroup()) {
      for (AbstractColumn column : group.getColumns()) {
        if (!column.getAllAggregationColumns().isEmpty()) {
          String messageToLogAndUser = MessagesEnum.CANNOT_BE_IN_GROUP_BY.getMessageWithArgs(
              localeForUserMessages, column.toQueryString());
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
    if (hasPivot()) {
      for (AbstractColumn column : pivot.getColumns()) {
        if (!column.getAllAggregationColumns().isEmpty()) {
          String messageToLogAndUser = MessagesEnum.CANNOT_BE_IN_PIVOT.getMessageWithArgs(
              localeForUserMessages, column.toQueryString());
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
    if (hasFilter()) {
      List<AggregationColumn> filterAggregations = filter.getAggregationColumns();
      if (!filterAggregations.isEmpty()) {
        String messageToLogAndUser = MessagesEnum.CANNOT_BE_IN_WHERE.getMessageWithArgs(
            localeForUserMessages, filterAggregations.get(0).toQueryString());;
        log.error(messageToLogAndUser);
        throw new InvalidQueryException(messageToLogAndUser);
      }
    }
    
    // A column cannot appear both as an aggregation column and as a regular
    // column in the selection.
    for (SimpleColumn column1 : selectionSimple) {
      String id = column1.getColumnId();
      for (AggregationColumn column2 : selectionAggregated) {
        if (id.equals(column2.getAggregatedColumn().getId())) {
          String messageToLogAndUser = MessagesEnum.SELECT_WITH_AND_WITHOUT_AGG.getMessageWithArgs(
              localeForUserMessages, id);
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    // When aggregation is used, check that all selected columns are valid
    // (a column is valid if it is either grouped-by, or is a
    // scalar function column whose arguments are all valid columns).
    if (!selectionAggregated.isEmpty()) {
      for (AbstractColumn col : selectionColumns) {
        checkSelectedColumnWithGrouping(groupColumns, col);
      }
    }

    // Cannot group by a column that appears in an aggregation.
    if (hasSelection() && hasGroup()) {
      for (AggregationColumn column : selectionAggregated) {
        String id = column.getAggregatedColumn().getId();
        if (groupColumnIds.contains(id)) {
          String messageToLogAndUser = MessagesEnum.COL_AGG_NOT_IN_SELECT.getMessageWithArgs(
              localeForUserMessages, id);
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    // Cannot use grouping or pivoting when no aggregations are defined in the
    // selection.
    if (hasGroup() && selectionAggregated.isEmpty()) {
      String messageToLogAndUser = MessagesEnum.CANNOT_GROUP_WITNOUT_AGG.getMessage(
          localeForUserMessages);
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    if (hasPivot() && selectionAggregated.isEmpty()) {
      String messageToLogAndUser = MessagesEnum.CANNOT_PIVOT_WITNOUT_AGG.getMessage(
          localeForUserMessages);
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }

    // Cannot order by a column that is not in the selection when aggregations
    // are defined.
    if (hasSort() && !selectionAggregated.isEmpty()) {
      for (AbstractColumn column : sort.getColumns()) {
        String messageToLogAndUser = MessagesEnum.COL_IN_ORDER_MUST_BE_IN_SELECT.getMessageWithArgs(
            localeForUserMessages, column.toQueryString());
        checkColumnInList(selection.getColumns(), column,
            messageToLogAndUser);
      }
    }

    // Cannot pivot by a column that appears in an aggregation.
    if (hasPivot()) {
      for (AggregationColumn column : selectionAggregated) {
        String id = column.getAggregatedColumn().getId();
        if (pivotColumnIds.contains(id)) {
          String messageToLogAndUser = MessagesEnum.AGG_IN_SELECT_NO_PIVOT.getMessageWithArgs(
              localeForUserMessages, id);
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    // Cannot have a column appear in both group by and pivot.
    if (hasGroup() && hasPivot()) {
      for (String id : groupColumnIds) {
        if (pivotColumnIds.contains(id)) {
          String messageToLogAndUser = MessagesEnum.NO_COL_IN_GROUP_AND_PIVOT.getMessageWithArgs(
              localeForUserMessages, id);
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    // Cannot order by aggregation column when pivoting is used.
    if (hasPivot() && !sortAggregated.isEmpty()) {
      AggregationColumn column = sortAggregated.get(0);
      String messageToLogAndUser = MessagesEnum.NO_AGG_IN_ORDER_WHEN_PIVOT.getMessageWithArgs(
          localeForUserMessages, column.getAggregatedColumn().getId());
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }

    // Cannot order by aggregation columns that weren't defined in the
    // selection.
    for (AggregationColumn column : sortAggregated) {
        String messageToLogAndUser = MessagesEnum.AGG_IN_ORDER_NOT_IN_SELECT.getMessageWithArgs(
            localeForUserMessages, column.toQueryString());
        checkColumnInList(selectionAggregated, column, messageToLogAndUser);
    }

    Set<AbstractColumn> labelColumns = (hasLabels()
        ? labels.getColumns() : Sets.<AbstractColumn>newHashSet());
    Set<AbstractColumn> formatColumns = (hasUserFormatOptions()
        ? userFormatOptions.getColumns() : Sets.<AbstractColumn>newHashSet());

    if (hasSelection()) {
      for (AbstractColumn col : labelColumns) {
        if (!selectionColumns.contains(col)) {
          String messageToLogAndUser = MessagesEnum.LABEL_COL_NOT_IN_SELECT.getMessageWithArgs(
              localeForUserMessages, col.toQueryString());
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
      for (AbstractColumn col : formatColumns) {
        if (!selectionColumns.contains(col)) {
          String messageToLogAndUser = MessagesEnum.FORMAT_COL_NOT_IN_SELECT.getMessageWithArgs(
              localeForUserMessages, col.toQueryString());
          log.error(messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
  }

  /**
   * Returns all simple column IDs in use in all parts of the query.
   *
   * @return All simple column IDs in use in all parts of the query.
   */
  public Set<String> getAllColumnIds() {
    Set<String> result = Sets.newHashSet();
    if (hasSelection()) {
      for (AbstractColumn col : selection.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }
    if (hasSort()) {
      for (AbstractColumn col : sort.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }
    if (hasGroup()) {
      result.addAll(getGroup().getSimpleColumnIds());
    }
    if (hasPivot()) {
      result.addAll(getPivot().getSimpleColumnIds());
    }
    if (hasFilter()) {
      result.addAll(getFilter().getAllColumnIds());
    }
    if (hasLabels()) {
      for (AbstractColumn col : labels.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }
    if (hasUserFormatOptions()) {
      for (AbstractColumn col : userFormatOptions.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }

    return result;
  }

  /**
   * Returns all aggregation columns used in all parts of this query.
   *
   * @return All aggregation columns used in all parts of this query.
   */
  public Set<AggregationColumn> getAllAggregations() {
    Set<AggregationColumn> result = Sets.newHashSet();
    if (hasSelection()) {
      result.addAll(selection.getAggregationColumns());
    }
    if (hasSort()) {
      for (AbstractColumn col : sort.getColumns()) {
        if (col instanceof AggregationColumn) {
          result.add((AggregationColumn) col);
        }
      }
    }

    if (hasLabels()) {
      for (AbstractColumn col : labels.getColumns()) {
        if (col instanceof AggregationColumn) {
          result.add((AggregationColumn) col);
        }
      }
    }
    if (hasUserFormatOptions()) {
      for (AbstractColumn col : userFormatOptions.getColumns()) {
        if (col instanceof AggregationColumn) {
          result.add((AggregationColumn) col);
        }
      }
    }
    return result;
  }

  /**
   * Returns all scalar function columns in this query.
   *
   * @return All scalar function columns in this query.
   */
  public Set<ScalarFunctionColumn> getAllScalarFunctionsColumns() {
    Set<ScalarFunctionColumn> mentionedScalarFunctionColumns =
        Sets.newHashSet();
    if (hasSelection()) {
      mentionedScalarFunctionColumns.addAll(
          selection.getScalarFunctionColumns());
    }
    if (hasFilter()) {
      mentionedScalarFunctionColumns.addAll(filter.getScalarFunctionColumns());
    }
    if (hasGroup()) {
      mentionedScalarFunctionColumns.addAll(group.getScalarFunctionColumns());
    }
    if (hasPivot()) {
      mentionedScalarFunctionColumns.addAll(pivot.getScalarFunctionColumns());
    }
    if (hasSort()) {
      mentionedScalarFunctionColumns.addAll(sort.getScalarFunctionColumns());
    }
    if (hasLabels()) {
      mentionedScalarFunctionColumns.addAll(labels.getScalarFunctionColumns());
    }
    if (hasUserFormatOptions()) {
      mentionedScalarFunctionColumns.addAll(userFormatOptions.getScalarFunctionColumns());
    }
    return mentionedScalarFunctionColumns;
  }


  /**
   * Checks that the given column is valid, i.e., is in the given list of
   * columns, or is a scalar function column and all its inner columns are
   * valid (i.e., in the list).
   *
   * @param columns The given list of columns.
   * @param column  The given column.
   * @param messageToLogAndUser The error message for the exception that is
   *     thrown when the column is not in the list.
   *
   * @throws InvalidQueryException Thrown when the column is invalid.
   */
  private void checkColumnInList(List<? extends AbstractColumn> columns,
      AbstractColumn column, String messageToLogAndUser)
      throws InvalidQueryException {
    if (columns.contains(column)) {
      return;
    } else if (column instanceof ScalarFunctionColumn) {
      List<AbstractColumn> innerColumns =
          ((ScalarFunctionColumn) column).getColumns();
      for (AbstractColumn innerColumn : innerColumns) {
        checkColumnInList(columns, innerColumn, messageToLogAndUser);
      }
    } else {
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
  }

  /**
   * Checks the selection of the given column is valid, given the group
   * columns. A column is valid in this sense if it is either grouped-by, or is
   * a scalar function column whose arguments are all valid columns.
   *
   * @param groupColumns The group columns.
   * @param col The selected column.
   *
   * @throws InvalidQueryException Thrown when the given column is a simple
   *     column that is not grouped by.
   */
  private void checkSelectedColumnWithGrouping(
      List<AbstractColumn> groupColumns, AbstractColumn col)
      throws InvalidQueryException {
    if (col instanceof SimpleColumn) {
      if (!groupColumns.contains(col)) {
        String messageToLogAndUser = MessagesEnum.ADD_COL_TO_GROUP_BY_OR_AGG.getMessageWithArgs(
            localeForUserMessages, col.getId());
        log.error(messageToLogAndUser);
        throw new InvalidQueryException(messageToLogAndUser);
      }
    } else if (col instanceof ScalarFunctionColumn) {
      // A selected scalar function column is valid if it is grouped by, or if
      // its inner columns are all valid.
      if (!groupColumns.contains(col)) {
        List<AbstractColumn> innerColumns =
            ((ScalarFunctionColumn) col).getColumns();
        for (AbstractColumn innerColumn : innerColumns) {
          checkSelectedColumnWithGrouping(groupColumns, innerColumn);
        }
      }
    }
    // An aggregation column is always valid (the inner aggregated column
    // validity is checked elsewhere).
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    result = prime * result + ((filter == null) ? 0 : filter.hashCode());
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((labels == null) ? 0 : labels.hashCode());
    result = prime * result + ((options == null) ? 0 : options.hashCode());
    result = prime * result + ((pivot == null) ? 0 : pivot.hashCode());
    result = prime * result + rowSkipping;
    result = prime * result + rowLimit;
    result = prime * result + rowOffset;
    result = prime * result + ((selection == null) ? 0 : selection.hashCode());
    result = prime * result + ((sort == null) ? 0 : sort.hashCode());
    result = prime * result + ((userFormatOptions == null) ? 0 : userFormatOptions.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Query other = (Query) obj;
    if (filter == null) {
      if (other.filter != null) {
        return false;
      }
    } else if (!filter.equals(other.filter)) {
      return false;
    }
    if (group == null) {
      if (other.group != null) {
        return false;
      }
    } else if (!group.equals(other.group)) {
      return false;
    }
    if (labels == null) {
      if (other.labels != null) {
        return false;
      }
    } else if (!labels.equals(other.labels)) {
      return false;
    }
    if (options == null) {
      if (other.options != null) {
        return false;
      }
    } else if (!options.equals(other.options)) {
      return false;
    }
    if (pivot == null) {
      if (other.pivot != null) {
        return false;
      }
    } else if (!pivot.equals(other.pivot)) {
      return false;
    }
    if (rowSkipping != other.rowSkipping) {
      return false;
    }
    if (rowLimit != other.rowLimit) {
      return false;
    }
    if (rowOffset != other.rowOffset) {
      return false;
    }
    if (selection == null) {
      if (other.selection != null) {
        return false;
      }
    } else if (!selection.equals(other.selection)) {
      return false;
    } 
    if (sort == null) {
      if (other.sort != null) {
        return false;
      }
    } else if (!sort.equals(other.sort)) {
      return false;
    }
    if (userFormatOptions == null) {
      if (other.userFormatOptions != null) {
        return false;
      }
    } else if (!userFormatOptions.equals(other.userFormatOptions)) {
      return false;
    }
    return true;
  }
  
  /**
   * Creates a comma separated string of the query strings of the given columns.
   * 
   * @param l The list of columns.
   * 
   * @return A comma separated string of the query strings of the given columns.
   */
  /* package */ static String columnListToQueryString(List<AbstractColumn> l) {
    StrBuilder builder = new StrBuilder();
    List<String> stringList = Lists.newArrayList();
    for (AbstractColumn col : l) {
      stringList.add(col.toQueryString());
    }
    builder.appendWithSeparators(stringList, ", ");
    return builder.toString();
  }
  
  /**
   * Creates a query-language representation of the given string, i.e., delimits it with either
   * double-quotes (") or single-quotes ('). Throws a runtime exception if the given string
   * contains both double-quotes and single-quotes and so cannot be expressed in the query
   * language. 
   * 
   * @param s The string to create a query-language representation for.
   * 
   * @return The query-language representation of the given string.
   */
  /* package */ static String stringToQueryStringLiteral(String s) {
    if (s.contains("\"")) {
      if (s.contains("'")) {
        throw new RuntimeException("Cannot represent string that contains both double-quotes (\") "
            + " and single quotes (').");
      } else {
        return "'" + s + "'";
      }
    } else {
      return "\"" + s + "\"";
    }
  }
  
  /**
   * Returns a string that when fed to the query parser will yield an identical Query.
   * Used mainly for debugging purposes.
   * 
   * @return The query string.
   */
  public String toQueryString() {
    List<String> clauses = Lists.newArrayList();
    if (hasSelection()) {               
      clauses.add("SELECT " + selection.toQueryString());
    }
    if (hasFilter()) {
      clauses.add("WHERE " + filter.toQueryString());      
    }
    if (hasGroup()) {
      clauses.add("GROUP BY " + group.toQueryString());
    }
    if (hasPivot()) {
      clauses.add("PIVOT " + pivot.toQueryString());
    }
    if (hasSort()) {
      clauses.add("ORDER BY " + sort.toQueryString());
    }
    if (hasRowSkipping()) {
      clauses.add("SKIPPING " + rowSkipping);
    }
    if (hasRowLimit()) {
      clauses.add("LIMIT " + rowLimit);
    }
    if (hasRowOffset()) {
      clauses.add("OFFSET " + rowOffset);
    }
    if (hasLabels()) {
      clauses.add("LABEL " + labels.toQueryString());
    }
    if (hasUserFormatOptions()) {
      clauses.add("FORMAT " + userFormatOptions.toQueryString());
    }
    if (hasOptions()) {
      clauses.add("OPTIONS " + options.toQueryString());
    }
    StrBuilder result = new StrBuilder();
    result.appendWithSeparators(clauses, " ");
    return result.toString();
  }
}
