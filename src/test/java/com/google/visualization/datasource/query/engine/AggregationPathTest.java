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

import junit.framework.TestCase;

import java.util.List;

import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.NumberValue;

/**
 * Tests for the AggregationPath class
 *
 * @author Yoav G.
 */

public class AggregationPathTest extends TestCase {

  public void simpleTest() {
    AggregationPath path = new AggregationPath();
    path.add(new NumberValue(3));
    path.add(new NumberValue(4));
    List<Value> values = path.getValues();
    assertEquals(3.0, ((NumberValue)values.get(0)).getValue());
    assertEquals(4.0, ((NumberValue)values.get(1)).getValue());
  }

  public void testReverse() {
    AggregationPath path = new AggregationPath();
    path.add(new NumberValue(3));
    path.add(new NumberValue(4));
    path.reverse();
    List<Value> values = path.getValues();
    assertEquals(4.0, ((NumberValue)values.get(0)).getValue());
    assertEquals(3.0, ((NumberValue)values.get(1)).getValue());
  }

  public void testEmptyPath() {
    AggregationPath path = new AggregationPath();
    path.reverse();
    List<Value> values = path.getValues();
    assertEquals(0, values.size());
  }

}