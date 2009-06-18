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

import com.google.visualization.datasource.query.Query;

/**
 * A product of splitQuery() method, composed of a data source query to be executed first by
 * the data source and a second completion query to be executed on the results of the
 * first one by the query engine.
 * The application of the first query and the second query is equivalent to the application of the
 * original query.
 * 
 * @author Yonatan B.Y.
 */
public class QueryPair {

  /**
   * The data source query.
   */
  private Query dataSourceQuery;
  
  /**
   * The completion query.
   */
  private Query completionQuery;
  
  /**
   * Construct a new query pair.
   * 
   * @param dataSourceQuery The data source query.
   * @param completionQuery The completion query.
   */
  public QueryPair(Query dataSourceQuery, Query completionQuery) {
    this.dataSourceQuery = dataSourceQuery;
    this.completionQuery = completionQuery;
  }
  
  /**
   * Returns the data source query.
   * 
   * @return The data source query.
   */
  public Query getDataSourceQuery() {
    return dataSourceQuery;
  }

  /**
   * Returns the completion query.
   * 
   * @return The completion query.
   */
  public Query getCompletionQuery() {
    return completionQuery;   
  }
}
