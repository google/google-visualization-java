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

package com.google.visualization.datasource.base;

import com.google.common.collect.Maps;

import com.ibm.icu.util.ULocale;

import java.util.Locale;
import java.util.Map;

/**
 * An enum for messages used in the DataSource library. Each element references a message
 * in {@code ErrorMessages}. For example all the error messages produced in query parsing are
 * listed here.
 *
 * @author Hillel M.
 */
public enum MessagesEnum {

  /**
   * Column in query is missing from the data table.
   * @param column id.
   */
  NO_COLUMN,
  
  /**
   * Average and sum aggregation can be applied only on numeric columns.
   */
  AVG_SUM_ONLY_NUMERIC,
  
  /**
   * Invalid aggregation type.
   * @param aggregation type.
   */
  INVALID_AGG_TYPE,
  
  /**
   * An error when parsing the query.
   * @param detailed message.
   */
  PARSE_ERROR,
  
  /**
   * Column can not be in group by because it has an aggregation.
   * @param column id 
   */
  CANNOT_BE_IN_GROUP_BY,
  
  /**
   * Column can not be in pivot because it has an aggregation.
   * @param column id 
   */
  CANNOT_BE_IN_PIVOT,
  
  /**
   * Column can not be in where because it has an aggregation.
   * @param column id 
   */
  CANNOT_BE_IN_WHERE,
  
  /**
   * Column can not appear in select with and without aggregation.
   * @param column id.
   */
  SELECT_WITH_AND_WITHOUT_AGG,
  
  /**
   * Column which is aggregated in SELECT, cannot appear in GROUP BY.
   * @param column id
   */
  COL_AGG_NOT_IN_SELECT,
  
  /**
   * Cannot use GROUP BY when no aggregations are defined in SELECT. 
   */
  CANNOT_GROUP_WITNOUT_AGG,
  
  /**
   * Cannot use PIVOT when no aggregations are defined in SELECT.
   */
  CANNOT_PIVOT_WITNOUT_AGG,
  
  /**
   * Column which is aggregated in SELECT, cannot appear in PIVOT.
   * @param column id
   */
  AGG_IN_SELECT_NO_PIVOT,
  
  /**
   * Column which is referenced in FORMAT, is not part of SELECT clause.
   * @param column id
   */
  FORMAT_COL_NOT_IN_SELECT,
  
  /**
   * Column which is referenced in LABEL, is not part of SELECT clause.
   * @param column id 
   */
  LABEL_COL_NOT_IN_SELECT,
  
  /**
   * Column should be added to GROUP BY, removed from SELECT, or aggregated in SELECT.
   * @param column id
   */
  ADD_COL_TO_GROUP_BY_OR_AGG,
  
  
  /**
   * Aggregation found in ORDER BY but was not found in SELECT.
   * @param aggreation column
   */
   AGG_IN_ORDER_NOT_IN_SELECT,
   
   /**
    * Column cannot be aggregated in ORDER BY when PIVOT is used.
    * @param aggregation column
    */
   NO_AGG_IN_ORDER_WHEN_PIVOT,
  
  /**
   * Column which appears in ORDER BY, must be in SELECT as well, because SELECT
   * contains aggregated columns.
   */
   COL_IN_ORDER_MUST_BE_IN_SELECT,
  
  /**
   * Column cannot appear both in GROUP BY and in PIVOT.
   * @param column id 
   */
   NO_COL_IN_GROUP_AND_PIVOT,
  
  /**
   * Invalid value for row offset.
   * @param value
   */
   INVALID_OFFSET,

   /**
    * Invalid value for row skipping.
    * @param value
    */
    INVALID_SKIPPING,

   /**
    * Column cannot appear more than once.
    * @param column id
    * @param clause
    */
   COLUMN_ONLY_ONCE;
   
  /**
   * A mapping from reason type to message.
   */
  private static final Map<MessagesEnum, String>
      QUERY_ERROR_TO_MESSAGE = Maps.newEnumMap(MessagesEnum.class);

  static {
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.NO_COLUMN,
        "NO_COLUMN");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.AVG_SUM_ONLY_NUMERIC,
        "AVG_SUM_ONLY_NUMERIC");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.INVALID_AGG_TYPE,
        "INVALID_AGG_TYPE");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.PARSE_ERROR,
        "PARSE_ERROR");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.CANNOT_BE_IN_GROUP_BY,
        "CANNOT_BE_IN_GROUP_BY");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.CANNOT_BE_IN_PIVOT,
        "CANNOT_BE_IN_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.CANNOT_BE_IN_WHERE,
        "CANNOT_BE_IN_WHERE");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.SELECT_WITH_AND_WITHOUT_AGG,
        "SELECT_WITH_AND_WITHOUT_AGG");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.COL_AGG_NOT_IN_SELECT,
        "COL_AGG_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.CANNOT_GROUP_WITNOUT_AGG,
        "CANNOT_GROUP_WITNOUT_AGG");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.CANNOT_PIVOT_WITNOUT_AGG,
        "CANNOT_PIVOT_WITNOUT_AGG");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.AGG_IN_SELECT_NO_PIVOT,
        "AGG_IN_SELECT_NO_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.FORMAT_COL_NOT_IN_SELECT,
        "FORMAT_COL_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.LABEL_COL_NOT_IN_SELECT,
        "LABEL_COL_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.ADD_COL_TO_GROUP_BY_OR_AGG,
        "ADD_COL_TO_GROUP_BY_OR_AGG");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.AGG_IN_ORDER_NOT_IN_SELECT,
        "AGG_IN_ORDER_NOT_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.NO_AGG_IN_ORDER_WHEN_PIVOT,
        "NO_AGG_IN_ORDER_WHEN_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.COL_IN_ORDER_MUST_BE_IN_SELECT,
        "COL_IN_ORDER_MUST_BE_IN_SELECT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.NO_COL_IN_GROUP_AND_PIVOT,
        "NO_COL_IN_GROUP_AND_PIVOT");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.INVALID_OFFSET,
        "INVALID_OFFSET");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.INVALID_SKIPPING,
        "INVALID_SKIPPING");
    QUERY_ERROR_TO_MESSAGE.put(MessagesEnum.COLUMN_ONLY_ONCE,
        "COLUMN_ONLY_ONCE");
  }
  

  /**
   * Returns a localized message for this reason type and locale.
   *
   * @param locale The locale.
   * @param args An array of arguments.
   *
   * @return A localized message given a reason type and locale.
   */
  public String getMessageWithArgs(ULocale ulocale, String... args) {
    Locale locale = ulocale != null ? ulocale.toLocale() : null;
    return LocaleUtil.getLocalizedMessageFromBundleWithArguments(
        "com.google.visualization.datasource.base.ErrorMessages", QUERY_ERROR_TO_MESSAGE.get(this),
        args, locale);
  }
  
  /**
   * Returns a localized message for this reason type and locale.
   *
   * @param locale The locale.
   *
   * @return A localized message given a reason type and locale.
   */
  public String getMessage(ULocale ulocale) {
    Locale locale = ulocale != null ? ulocale.toLocale() : null;
    return LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", QUERY_ERROR_TO_MESSAGE.get(this), 
        locale);
  }
}
