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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Helper functions to assist the generated parser to deal with generic types. Unfortunately,
 * javacc is not good at handling generic types, such as List<QueryFilter>, inside
 * the .jj file. One solution is to use ArrayLists in the .jj file and then convert them
 * using this class. This class uses unsafe operations, which is unavoidable.
 *
 * @author Yonatan B.Y.
 */
/* package */ class GenericsHelper {
  private GenericsHelper()
  {}

  /**
   * Transforms, in an unsafe way, a typed List from a raw ArrayList.
   *
   * @param list The ArrayList to transform.
   *
   * @return The new List<T> containing all the elements in list.
   */
  /* package */ static<T> List<T> makeTypedList(ArrayList<? extends T> list) {
    List<T> result = Lists.newArrayListWithExpectedSize(list.size());
    for (T obj : list) {
      result.add(obj);
    }
    return result;
  }

  /**
   * Transforms a typed List from a raw array.
   *
   * @param array The array to transform.
   *
   * @return The new List<T> containing all the elements in array.
   */
  /* package */ static<T> List<T> makeAbstractColumnList(T[] array) {
    List<T> result = Lists.newArrayListWithExpectedSize(array.length);
    result.addAll(Arrays.asList(array));
    return result;
  }
}
