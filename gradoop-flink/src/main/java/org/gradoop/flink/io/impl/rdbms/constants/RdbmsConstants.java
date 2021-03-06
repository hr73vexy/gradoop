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

package org.gradoop.flink.io.impl.rdbms.constants;

/**
 * Stores constants for relational database to graph conversion
 */
public class RdbmsConstants {

  /**
   * Vertex key identifier for primary keys
   */
  public static final String PK_ID = "*#primary_key_identifier#*";

  /**
   * Field identifier for primary keys
   */
  public static final String PK_FIELD = "pk";

  /**
   * Field identifier for foreign keys
   */
  public static final String FK_FIELD = "fk";

  /**
   * Field identifier for further attributes
   */
  public static final String ATTRIBUTE_FIELD = "att";

  /**
   * Database Identifier for mysql,mariadb,postgresql,h2,...
   */
  public static final int MYSQL_TYPE_ID = 0;

  /**
   * Database Identifier for oracle,sqlserver,derby
   */
  public static final int SQLSERVER_TYPE_ID = 1;
}
