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
package org.gradoop.flink.io.impl.rdbms;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.rdbms.connection.RdbmsConfig;
import org.gradoop.flink.io.impl.rdbms.connection.RdbmsConnect;
import org.gradoop.flink.io.impl.rdbms.functions.CreateEdges;
import org.gradoop.flink.io.impl.rdbms.functions.CreateVertices;
import org.gradoop.flink.io.impl.rdbms.functions.DeletePkFkProperties;
import org.gradoop.flink.io.impl.rdbms.metadata.MetaDataParser;
import org.gradoop.flink.io.impl.rdbms.metadata.TableToEdge;
import org.gradoop.flink.io.impl.rdbms.metadata.TableToNode;
import org.gradoop.flink.model.api.epgm.GraphCollection;
import org.gradoop.flink.model.api.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;

/**
 * A graph data import for relational databases.
 */
public class RdbmsDataSource implements DataSource {

  /**
   * Gradoop Flink configuration
   */
  private GradoopFlinkConfig flinkConfig;

  /**
   * Configuration for the relational database connection
   */
  private RdbmsConfig rdbmsConfig;

  /**
   * Flink Execution Environment
   */
  private ExecutionEnvironment env;

  /**
   * Vertices
   */
  private DataSet<Vertex> vertices;

  /**
   * Edges
   */
  private DataSet<Edge> edges;

  /**
   * Transforms a relational database into an EPGM instance
   *
   * The data source expects a standard jdbc url, e.g.
   * (jdbc:mysql://localhost/employees) and a valid path to a fitting jdbc driver
   *
   * @param url
   *          Valid jdbc url (e.g. jdbc:mysql://localhost/employees)
   * @param user
   *          User name of relational database user
   * @param pw
   *          Password of relational database user
   * @param jdbcDriverPath
   *          Valid path to jdbc driver
   * @param jdbcDriverClassName
   *          Valid jdbc driver class name
   * @param flinkConfig
   *          Valid gradoop flink configuration
   */
  public RdbmsDataSource(String url, String user, String pw, String jdbcDriverPath,
      String jdbcDriverClassName, GradoopFlinkConfig flinkConfig) {
    this.flinkConfig = flinkConfig;
    this.env = flinkConfig.getExecutionEnvironment();
    this.rdbmsConfig = new RdbmsConfig(null, url, user, pw, jdbcDriverPath, jdbcDriverClassName);
  }

  @Override
  public LogicalGraph getLogicalGraph() {

    Connection con = RdbmsConnect.connect(rdbmsConfig);
    DataSet<Vertex> tempVertices = null;
    MetaDataParser metadataParser = null;

    try {
      rdbmsConfig.setRdbms(con.getMetaData().getDatabaseProductName().toLowerCase());

      metadataParser = new MetaDataParser(con, rdbmsConfig.getRdbmsType());
      metadataParser.parse();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // tables going to convert to vertices
    ArrayList<TableToNode> tablesToNodes = metadataParser.getTablesToNodes();

    // tables going to convert to edges
    ArrayList<TableToEdge> tablesToEdges = metadataParser.getTablesToEdges();

    // creates vertices from rdbms table tuples
    tempVertices = CreateVertices.create(flinkConfig, rdbmsConfig, tablesToNodes);

    edges = CreateEdges.create(flinkConfig, rdbmsConfig, tablesToEdges, tempVertices);

    // cleans vertices by deleting primary key and foreign key
    // properties
    vertices = tempVertices.map(new DeletePkFkProperties())
        .withBroadcastSet(env.fromCollection(tablesToNodes), "tablesToNodes");

    return flinkConfig.getLogicalGraphFactory().fromDataSets(vertices, edges);
  }

  @Override
  public GraphCollection getGraphCollection() throws IOException {
    return flinkConfig.getGraphCollectionFactory().fromGraph(getLogicalGraph());
  }
}
