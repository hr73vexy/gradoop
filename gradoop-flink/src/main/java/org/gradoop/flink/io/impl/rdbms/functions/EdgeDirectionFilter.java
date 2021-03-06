/*
 * Copyright © 2014 - 2018 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradoop.flink.io.impl.rdbms.functions;

import org.apache.flink.api.common.functions.FilterFunction;
import org.gradoop.flink.io.impl.rdbms.metadata.TableToEdge;

/**
 * Filters directed edges
 */
public class EdgeDirectionFilter implements FilterFunction<TableToEdge> {

  /**
   * serial version uid
   */
  private static final long serialVersionUID = 1L;

  @Override
  public boolean filter(TableToEdge table) throws Exception {
    return table.isDirectionIndicator();
  }
}
