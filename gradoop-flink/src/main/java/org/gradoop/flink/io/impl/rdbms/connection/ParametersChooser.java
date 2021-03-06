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

package org.gradoop.flink.io.impl.rdbms.connection;

import java.io.Serializable;

import org.gradoop.flink.io.impl.rdbms.constants.RdbmsConstants;

/**
 * Chooses fitting parameters for database pageination
 */
public class ParametersChooser {

  /**
   * Chooses fitting parameters for database pageination
   *
   * @param rdbmsType
   *          Databse identifier of connected database
   * @param parallelism
   *          Parallelism of flink job
   * @param rowCount
   *          Count of database tables' rows
   * @return 2D array of pageination parameters
   */
  public static Serializable[][] choose(int rdbmsType, int parallelism, int rowCount) {
    Serializable[][] parameters;

    // split database table in parts of same size
    int partitionNumber;
    int partitionRest;

    if (rowCount < parallelism) {
      partitionNumber = 1;
      partitionRest = 0;
      parameters = new Integer[rowCount][2];
    } else {
      partitionNumber = rowCount / parallelism;
      partitionRest = rowCount % parallelism;
      parameters = new Integer[parallelism][2];
    }

    int j = 0;

    switch (rdbmsType) {

    case RdbmsConstants.MYSQL_TYPE_ID:
    default:
      for (int i = 0; i < parameters.length; i++) {
        if (i == parameters.length - 1) {
          parameters[i] = new Integer[] { partitionNumber + partitionRest, j };
        } else {
          parameters[i] = new Integer[] { partitionNumber, j };
          j = j + partitionNumber;
        }
      }
      break;

    case RdbmsConstants.SQLSERVER_TYPE_ID:
      for (int i = 0; i < parameters.length; i++) {
        if (i == parameters.length - 1) {
          parameters[i] = new Integer[] { j, partitionNumber + partitionRest };
        } else {
          parameters[i] = new Integer[] { j, partitionNumber };
          j = j + partitionNumber;
        }
      }
      break;
    }

    return parameters;
  }
}
