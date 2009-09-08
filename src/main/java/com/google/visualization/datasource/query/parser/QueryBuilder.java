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

package com.google.visualization.datasource.query.parser;

import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.MessagesEnum;
import com.google.visualization.datasource.query.Query;

import com.ibm.icu.util.ULocale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A singleton class that can parse a user query string, i.e., accept a string such as
 * "SELECT dept, max(salary) GROUP BY dept" and return a Query object. This class basically
 * wraps the QueryParser class that is auto-generated from the QueryParser.jj specification.  
 *
 * @author Hillel M.
 */
public class QueryBuilder {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(QueryBuilder.class.getName());

  /**
   * A singleton instance of this class.
   */
  private static final QueryBuilder SINGLETON = new QueryBuilder();

  /**
   * Returns the singleton instance of this class.
   *
   * @return The singleton query builder.
   */
  public static QueryBuilder getInstance() {
    return SINGLETON;
  }

  /**
   * Private constructor, to prevent instantiation other than that of the singleton instance.
   */
  private QueryBuilder() {
  }

  /**
   * Parses a user query into a Query object.
   *
   * @param tqValue The user query string.
   *
   * @return The parsed Query object.
   *
   * @throws InvalidQueryException Thrown if the query is invalid.
   */
  public Query parseQuery(String tqValue) throws InvalidQueryException {
    return parseQuery(tqValue, null);
  }
  
  /**
   * Parses a user query into a Query object.
   *
   * @param tqValue The user query string.
   * @param ulocale The user locale.
   *
   * @return The parsed Query object.
   *
   * @throws InvalidQueryException Thrown if the query is invalid.
   */
  public Query parseQuery(String tqValue, ULocale ulocale) throws InvalidQueryException {
    Query query;
    if (StringUtils.isEmpty(tqValue)) {
      query = new Query();
    } else {
      try {
        query = QueryParser.parseString(tqValue);
      } catch (ParseException ex) {
        String messageToUserAndLog = ex.getMessage();
        log.error("Parsing error: " + messageToUserAndLog);
        throw new InvalidQueryException(MessagesEnum.PARSE_ERROR.getMessageWithArgs(ulocale, 
            messageToUserAndLog));
      }
      query.setLocaleForUserMessages(ulocale);
      query.validate();
    }
    return query;
  }
}
