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

package org.gradoop.dataintegration.importer.rdbmsimporter.metadata;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.gradoop.dataintegration.importer.rdbmsimporter.connection.SQLToBasicTypeMapper;
import org.gradoop.dataintegration.importer.rdbmsimporter.constants.RdbmsConstants.RdbmsType;
import org.gradoop.dataintegration.importer.rdbmsimporter.tuples.NameTypeTuple;
import org.gradoop.dataintegration.importer.rdbmsimporter.tuples.RowHeaderTuple;

import java.io.Serializable;
import java.util.ArrayList;

import static org.gradoop.dataintegration.importer.rdbmsimporter.constants.RdbmsConstants.ATTRIBUTE_FIELD;
import static org.gradoop.dataintegration.importer.rdbmsimporter.constants.RdbmsConstants.FK_FIELD;


/**
 * Stores metadata for tuple-to-edge conversation
 */
public class TableToEdge implements Serializable {

  /**
   * Serial version uid
   */
  private static final long serialVersionUID = 1L;

  /**
   * Management type of connecte rdbms
   */
  private RdbmsType rdbmsType;

  /**
   * Relationship type
   */
  private String relationshipType;

  /**
   * Name of relation start table
   */
  private String startTableName;

  /**
   * Name of relation end table
   */
  private String endTableName;

  /**
   * Name and datatype of relation start attribute
   */
  private NameTypeTuple startAttribute;

  /**
   * Name and datatype of relation end attribute
   */
  private NameTypeTuple endAttribute;

  /**
   * List of further attribute names and belonging datatypes
   */
  private ArrayList<NameTypeTuple> furtherAttributes;

  /**
   * Direction indicator
   */
  private boolean directionIndicator;

  /**
   * Number of rows
   */
  private int rowCount;

  /**
   * Valid sql query for querying needed relational data
   */
  private String sqlQuery;

  /**
   * Rowheader for row data representation of relational data
   */
  private RowHeader rowheader;

  /**
   * Flink type information for database table columns
   */
  private RowTypeInfo rowTypeInfo;

  /**
   * Conctructor
   *
   * @param rdbmsType Management type of connected rdbms
   * @param relationshipType Relationship type
   * @param startTableName Name of relation start table
   * @param endTableName Name of relation end table
   * @param startAttribute Name and type of relation start attribute
   * @param endAttribute Name and datatype of relation end attribute
   * @param furtherAttributes List of further attribute names and datatypes
   * @param directionIndicator Direction indicator
   * @param rowCount Number of rows
   */
  public TableToEdge(
    RdbmsType rdbmsType, String relationshipType, String startTableName,
    String endTableName, NameTypeTuple startAttribute, NameTypeTuple endAttribute,
    ArrayList<NameTypeTuple> furtherAttributes,
    boolean directionIndicator, int rowCount) {
    this.rdbmsType = rdbmsType;
    this.relationshipType = relationshipType;
    this.startTableName = startTableName;
    this.endTableName = endTableName;
    this.startAttribute = startAttribute;
    this.endAttribute = endAttribute;
    this.furtherAttributes = furtherAttributes;
    this.directionIndicator = directionIndicator;
    this.rowCount = rowCount;
    if (!directionIndicator) {
      this.init();
    }
  }

  /**
   * assigns proper sql query and generates belonging flink row type information
   * and row header
   */
  private void init() {
    TypeInformation<?>[] fieldTypes = null;
    SQLToBasicTypeMapper typeMapper = SQLToBasicTypeMapper.create();

    if (!directionIndicator) {
      this.sqlQuery = SQLQuery.getNtoMEdgeTableQuery(relationshipType, startAttribute.f0,
          endAttribute.f0, furtherAttributes, rdbmsType);

      rowheader = new RowHeader();

      fieldTypes = new TypeInformation<?>[furtherAttributes.size() + 2];
      fieldTypes[0] = typeMapper.getTypeInfo(startAttribute.f1, rdbmsType);
      rowheader.getRowHeader()
          .add(new RowHeaderTuple(startAttribute.f0, FK_FIELD, 0));
      fieldTypes[1] = typeMapper.getTypeInfo(endAttribute.f1, rdbmsType);
      rowheader.getRowHeader()
          .add(new RowHeaderTuple(endAttribute.f0, FK_FIELD, 1));

      int i = 2;
      if (!furtherAttributes.isEmpty()) {
        for (NameTypeTuple att : furtherAttributes) {
          fieldTypes[i] = typeMapper.getTypeInfo(att.f1, rdbmsType);
          rowheader.getRowHeader()
              .add(new RowHeaderTuple(att.f0, ATTRIBUTE_FIELD, i));
          i++;
        }
      }
    }
    this.rowTypeInfo = new RowTypeInfo(fieldTypes);
  }

  public String getRelationshipType() {
    return relationshipType;
  }

  public String getStartTableName() {
    return startTableName;
  }

  public String getEndTableName() {
    return endTableName;
  }

  public NameTypeTuple getStartAttribute() {
    return startAttribute;
  }

  public NameTypeTuple getEndAttribute() {
    return endAttribute;
  }

  public boolean isDirectionIndicator() {
    return directionIndicator;
  }

  public int getRowCount() {
    return rowCount;
  }

  public String getSqlQuery() {
    return sqlQuery;
  }

  public RowHeader getRowheader() {
    return rowheader;
  }

  public RowTypeInfo getRowTypeInfo() {
    return rowTypeInfo;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }
}
