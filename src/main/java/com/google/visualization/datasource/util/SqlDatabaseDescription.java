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

package com.google.visualization.datasource.util;

/**
 * This class contains all information required to connect to the sql database.
 *
 * @author Liron L.
 */
public class SqlDatabaseDescription {

  /**
   * The url of the sql database.
   */
  private String url;

  /**
   * The user name used to access the sql database.
   */
  private String user;

  /**
   * The password used to access the sql database.
   */
  private String password;

  /**
   * The database table name. Has a {@code null} value in case table name is not provided.
   */
  private String tableName;

  /**
   * Constructs a sql database description.
   *
   * @param url The url of the sql databasae.
   * @param user The user name to access the sql database.
   * @param password The password to access the sql database.
   * @param tableName The database table name.
   */
  public SqlDatabaseDescription(String url, String user, String password, String tableName) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.tableName = tableName;
  }

  /**
   * Returns the url of the sql databasae.
   *
   * @return The url of the sql databasae.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns the user name used to access the sql database.
   *
   * @return The user name used to access the sql database.
   *
   */
  public String getUser() {
    return user;
  }

  /**
   * Returns the password used to access the sql database.
   *
   * @return The password used to access the sql database.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password.
   *
   * @param password The new password to set.
   */
  public void setPassword(String password) {
    this.password = password;
  } 

  /**
   * Returns the database table name.
   *
   * @return The database table name.
   */
  public String getTableName() {
    return tableName;
  }
}
