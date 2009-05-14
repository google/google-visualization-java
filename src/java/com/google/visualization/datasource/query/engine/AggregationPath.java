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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.value.Value;

import java.util.Collections;
import java.util.List;

/**
 * An ordered list of values representing a path in an aggregation tree, from the root to a node.
 * Only the values are stored, not the nodes themselves.
 *
 * @author Yoav G.
 */

public class AggregationPath {

  /**
   * The list of values forming the path. Each value, in turn, is used to
   * navigate down from the current node to one of its children.
   */
  private List<Value> values;

  /**
   * Construct an empty path.
   */
  public AggregationPath() {
    values = Lists.newArrayList();
  }

  /**
   * Adds a value to this path.
   *
   * @param value The value to add.
   */
  public void add(Value value) {
    values.add(value);
  }

  /**
   * Returns the list of values. This list is immutable.
   *
   * @return The list of values. This list is immutable.
   */

  public List<Value> getValues() {
    return ImmutableList.copyOf(values);
  }

  /**
   * Reverses this path.
   */
  public void reverse() {
    Collections.reverse(values);
  }
}
