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
package org.gradoop.flink.io.impl.rdbms.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.gradoop.flink.io.impl.rdbms.functions.TableRowSize;
import org.gradoop.flink.io.impl.rdbms.tuples.FkTuple;
import org.gradoop.flink.io.impl.rdbms.tuples.NameTypeTuple;

/**
 * Relational database schema.
 */
public class MetaDataParser {

  /**
   * Database connection
   */
  private Connection con;

  /**
   * Management type of connected rdbms
   */
  private int rdbmsType;

  /**
   * Relational database metadata
   */
  private DatabaseMetaData metadata;

  /**
   * Parsed relational database tables
   */
  private ArrayList<RdbmsTableBase> tableBase;

  /**
   * Parses the schema of a relational database and provides base classes for
   * graph conversation
   *
   * @param con
   *          Database connection
   * @param rdbmsType
   *          Management type of connected rdbms
   * @throws SQLException
   */
  public MetaDataParser(Connection con, int rdbmsType) throws SQLException {
    this.con = con;
    this.rdbmsType = rdbmsType;
    this.metadata = con.getMetaData();
    this.tableBase = new ArrayList<RdbmsTableBase>();
  }

  /**
   * Parses the schema of a relational database to a relational database metadata
   * representation
   *
   * @throws SQLException
   */
  public void parse() throws SQLException {
    ResultSet rsTables = metadata.getTables(null, "%", "%", new String[] { "TABLE" });
    ArrayList<String> tables = new ArrayList<String>();

    while (rsTables.next()) {
      if (rsTables.getString("TABLE_TYPE").equals("TABLE")) {
        tables.add(rsTables.getString("TABLE_SCHEM") + "." + rsTables.getString("TABLE_NAME"));
      }
    }

    for (String table : tables) {
      String tableName = table.split("\\.")[1];
      String schemName = table.split("\\.")[0];

      // used to store primary key metadata representation
      ArrayList<NameTypeTuple> primaryKeys = new ArrayList<NameTypeTuple>();

      // used to store foreign key metadata representation
      ArrayList<FkTuple> foreignKeys = new ArrayList<FkTuple>();

      // used to store further attributes metadata representation
      ArrayList<NameTypeTuple> furtherAttributes = new ArrayList<NameTypeTuple>();

      // used to find further attributes, respectively no primary or
      // foreign key attributes
      ArrayList<String> pkfkAttributes = new ArrayList<String>();

      ResultSet rsPrimaryKeys = metadata.getPrimaryKeys(null, schemName, tableName);

      // parses primary keys if exists
      if (rsPrimaryKeys != null) {

        // assigning primary key name
        while (rsPrimaryKeys.next()) {
          primaryKeys.add(new NameTypeTuple(rsPrimaryKeys.getString("COLUMN_NAME"), null));
          pkfkAttributes.add(rsPrimaryKeys.getString("COLUMN_NAME"));
        }

        // assigning primary key data type
        for (NameTypeTuple pk : primaryKeys) {
          ResultSet rsColumns = metadata.getColumns(null, null, tableName, pk.f0);
          rsColumns.next();
          pk.f1 = JDBCType.valueOf(rsColumns.getInt("DATA_TYPE"));
        }
      }

      ResultSet rsForeignKeys = metadata.getImportedKeys(null, schemName, tableName);

      // parses foreign keys if exists
      if (rsForeignKeys != null) {

        // assigning foreign key name and name of belonging primary
        // and foreign key table
        while (rsForeignKeys.next()) {

          String refdTableName = rsForeignKeys.getString("PKTABLE_NAME");
          String refdTableSchem = rsForeignKeys.getString("PKTABLE_SCHEM");

          if (refdTableSchem == null) {
            foreignKeys.add(new FkTuple(rsForeignKeys.getString("FKCOLUMN_NAME"), null,
                rsForeignKeys.getString("PKCOLUMN_NAME"), refdTableName));
          } else {
            foreignKeys.add(new FkTuple(rsForeignKeys.getString("FKCOLUMN_NAME"), null,
                rsForeignKeys.getString("PKCOLUMN_NAME"), refdTableSchem + "." + refdTableName));
          }

          pkfkAttributes.add(rsForeignKeys.getString("FKCOLUMN_NAME"));
        }

        // assigning foreign key data type
        for (FkTuple fk : foreignKeys) {
          ResultSet rsColumns = metadata.getColumns(null, null, tableName, fk.f0);
          rsColumns.next();
          fk.f1 = JDBCType.valueOf(rsColumns.getInt("DATA_TYPE"));
        }
      }

      ResultSet rsAttributes = metadata.getColumns(null, schemName, tableName, null);

      // parses further attributes if exists
      // assigning attribute name and belonging data type
      while (rsAttributes.next()) {

        // catches unsupported data types
        if (!pkfkAttributes.contains(rsAttributes.getString("COLUMN_NAME")) &&
            JDBCType.valueOf(rsAttributes.getInt("DATA_TYPE")) != JDBCType.OTHER &&
            JDBCType.valueOf(rsAttributes.getInt("DATA_TYPE")) != JDBCType.ARRAY &&
            JDBCType.valueOf(rsAttributes.getInt("DATA_TYPE")) != JDBCType.LONGNVARCHAR &&
            JDBCType.valueOf(rsAttributes.getInt("DATA_TYPE")) != JDBCType.NVARCHAR) {

          NameTypeTuple att = new NameTypeTuple(rsAttributes.getString("COLUMN_NAME"),
              JDBCType.valueOf(rsAttributes.getInt("DATA_TYPE")));

          furtherAttributes.add(att);
        }
      }

      // number of rows (needed for distributed data querying via
      // flink)
      int rowCount = 0;

      if (schemName.equals("null")) {

        rowCount = TableRowSize.getTableRowSize(con, tableName);
        tableBase.add(
            new RdbmsTableBase(tableName, primaryKeys, foreignKeys, furtherAttributes, rowCount));
      } else {

        rowCount = TableRowSize.getTableRowSize(con, table);
        tableBase
            .add(new RdbmsTableBase(table, primaryKeys, foreignKeys, furtherAttributes, rowCount));
      }
    }
  }

  /**
   * Creates metadata representations of tables going to convert to vertices
   *
   * @return ArrayList containing metadata representations of rdbms tables going
   *         to convert to vertices
   */
  public ArrayList<TableToNode> getTablesToNodes() {
    ArrayList<TableToNode> tablesToNodes = new ArrayList<TableToNode>();

    for (RdbmsTableBase tables : tableBase) {
      if (!(tables.getForeignKeys().size() == 2) || !(tables.getPrimaryKeys().size() == 2)) {

        tablesToNodes.add(new TableToNode(rdbmsType, tables.getTableName(), tables.getPrimaryKeys(),
            tables.getForeignKeys(), tables.getFurtherAttributes(), tables.getRowCount()));
      }
    }
    return tablesToNodes;
  }

  /**
   * Creates metadata representations of tables going to convert to edges
   *
   * @return ArrayList containing metadata representations of rdbms tables going
   *         to convert to edges
   */
  public ArrayList<TableToEdge> getTablesToEdges() {
    ArrayList<TableToEdge> tablesToEdges = new ArrayList<TableToEdge>();

    for (RdbmsTableBase table : tableBase) {
      if (table.getForeignKeys() != null) {

        String tableName = table.getTableName();
        int rowCount = table.getRowCount();

        // table tuples going to convert to edges
        if (table.getForeignKeys().size() == 2 && table.getPrimaryKeys().size() == 2) {

          String refdTableNameOne = table.getForeignKeys().get(0).f3;
          String refdTableNameTwo = table.getForeignKeys().get(1).f3;

          NameTypeTuple fkAttOne = new NameTypeTuple(table.getForeignKeys().get(0).f0,
              table.getForeignKeys().get(0).f1);
          NameTypeTuple fkAttTwo = new NameTypeTuple(table.getForeignKeys().get(1).f0,
              table.getForeignKeys().get(1).f1);

          tablesToEdges.add(new TableToEdge(rdbmsType, tableName, refdTableNameOne,
              refdTableNameTwo, fkAttOne, fkAttTwo, null, table.getFurtherAttributes(),
              false, rowCount));
        } else {
          for (FkTuple fk : table.getForeignKeys()) {

            String refdTableName = fk.getRefdTableName();

            NameTypeTuple startAtt = new NameTypeTuple(fk.f0, fk.f1);
            NameTypeTuple endAtt = new NameTypeTuple(fk.f2, null);

            tablesToEdges.add(new TableToEdge(rdbmsType, null, tableName, refdTableName,
                startAtt, endAtt, table.getPrimaryKeys(), null, true, rowCount));
          }
        }
      }
    }
    return tablesToEdges;
  }

  public Connection getCon() {
    return con;
  }

  public void setCon(Connection con) {
    this.con = con;
  }

  public int getRdbmsType() {
    return rdbmsType;
  }

  public void setRdbmsType(int rdbmsType) {
    this.rdbmsType = rdbmsType;
  }

  public DatabaseMetaData getMetadata() {
    return metadata;
  }

  public void setMetadata(DatabaseMetaData metadata) {
    this.metadata = metadata;
  }

  public ArrayList<RdbmsTableBase> getTableBase() {
    return tableBase;
  }

  public void setTableBase(ArrayList<RdbmsTableBase> tableBase) {
    this.tableBase = tableBase;
  }
}
