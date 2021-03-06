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
package org.gradoop.flink.io.impl.rdbms.tuples;

import java.sql.JDBCType;

import org.apache.flink.api.java.tuple.Tuple4;

/**
 * Represents a foreign key f0 : attribute name of referencing foreign key f1 :
 * jdbc type of referencing foreign key f2 : name of referenced primary key
 * attribute f3 : name of referenced table
 */
public class FkTuple extends Tuple4<String, JDBCType, String, String> {

  /**
   * serial version uid
   */
  private static final long serialVersionUID = 1L;

  /**
   * Foreign key name
   */
  private String fkName;

  /**
   * JDBC data type of foreign key name
   */
  private JDBCType type;

  /**
   * Column name of referenced table
   */
  private String refdAttName;

  /**
   * Name of referenced Table
   */
  private String refdTableName;

  /**
   * Empty Constructor
   */
  public FkTuple() {
  }

  /**
   * Constructor
   *
   * @param fkName
   *          Foreign key name
   * @param type
   *          SQL Type of foreign key
   * @param refdAttName
   *          Name of referenced primary key
   * @param refdTableName
   *          Name of referenced primary key table
   */
  public FkTuple(String fkName, JDBCType type, String refdAttName, String refdTableName) {
    this.fkName = fkName;
    this.f0 = fkName;
    this.type = type;
    this.f1 = type;
    this.refdAttName = refdAttName;
    this.f2 = refdAttName;
    this.refdTableName = refdTableName;
    this.f3 = refdTableName;
  }

  public String getFkName() {
    return fkName;
  }

  public void setFkName(String fkName) {
    this.fkName = fkName;
  }

  public JDBCType getType() {
    return type;
  }

  public void setType(JDBCType type) {
    this.type = type;
  }

  public String getRefdAttName() {
    return refdAttName;
  }

  public void setRefdAttName(String refdAttName) {
    this.refdAttName = refdAttName;
  }

  public String getRefdTableName() {
    return refdTableName;
  }

  public void setRefdTableName(String refdTableName) {
    this.refdTableName = refdTableName;
  }
}
