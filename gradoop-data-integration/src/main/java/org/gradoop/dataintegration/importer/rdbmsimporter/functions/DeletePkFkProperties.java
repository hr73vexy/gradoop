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
package org.gradoop.dataintegration.importer.rdbmsimporter.functions;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.hadoop.shaded.com.google.common.collect.Lists;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.properties.Properties;
import org.gradoop.common.model.impl.properties.Property;
import org.gradoop.dataintegration.importer.rdbmsimporter.constants.RdbmsConstants;
import org.gradoop.dataintegration.importer.rdbmsimporter.metadata.TableToNode;
import org.gradoop.dataintegration.importer.rdbmsimporter.tuples.RowHeaderTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Cleans Epgm vertices by deleting primary key and foreign key propeties
 */
public class DeletePkFkProperties extends RichMapFunction<Vertex, Vertex> {

  /**
   * serial version uid
   */
  private static final long serialVersionUID = 1L;

  /**
   * List of tables to nodes representation
   */
  private List<TableToNode> tablesToNodes;

  @Override
  public Vertex map(Vertex v) throws Exception {
    Properties newProps = Properties.create();

    for (TableToNode table : tablesToNodes) {
      if (table.getTableName().equals(v.getLabel())) {

        ArrayList<String> foreignKeys = Lists.newArrayList();
        for (RowHeaderTuple rht : table.getRowheader().getRowHeader()) {
          if (rht.getAttType().equals(RdbmsConstants.FK_FIELD)) {
            foreignKeys.add(rht.f0);
          }
        }

        for (Property oldProp : v.getProperties()) {
          if (!oldProp.getKey().equals(RdbmsConstants.PK_ID) &&
              !foreignKeys.contains(oldProp.getKey())) {
            newProps.set(oldProp);
          }
        }
      }
    }

    v.setProperties(newProps);
    return v;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.tablesToNodes = getRuntimeContext()
        .getBroadcastVariable(RdbmsConstants.BROADCAST_VARIABLE);
  }
}