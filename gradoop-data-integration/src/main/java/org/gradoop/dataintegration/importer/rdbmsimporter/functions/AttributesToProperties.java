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

import org.apache.flink.types.Row;
import org.apache.log4j.Logger;
import org.gradoop.common.model.impl.properties.Properties;
import org.gradoop.dataintegration.importer.rdbmsimporter.constants.RdbmsConstants;
import org.gradoop.dataintegration.importer.rdbmsimporter.metadata.RowHeader;
import org.gradoop.dataintegration.importer.rdbmsimporter.tuples.RowHeaderTuple;

/**
 * Converts database tuples to valid Epgm properties
 */
public class AttributesToProperties {

  /**
   * Converts relational attributes to epgm properties
   *
   * @param tuple Row of relational database
   * @param rowheader Rowheader of database table
   * @return Epgm Properties
   */
  public static Properties getProperties(Row tuple, RowHeader rowheader) {

    Logger logger = Logger.getLogger(AttributesToProperties.class);
    Properties props = Properties.create();

    for (RowHeaderTuple rht : rowheader.getRowHeader()) {
      try {
        props.set(rht.getName(), PropertyValueParser.parse(tuple.getField(rht.getPos())));
      } catch (IndexOutOfBoundsException e) {
        logger.warn("Empty value field in column " + rht.getName());
      }
    }

    return props;
  }

  /**
   * Converts relational attributes to epgm properties without foreign key
   * attributes
   *
   * @param tuple Row of relational database
   * @param rowheader Rowheader of database table
   * @return Epgm Properties without foreign key attributes
   */
  public static Properties getPropertiesWithoutFKs(Row tuple, RowHeader rowheader) {
    Properties props = new Properties();
    for (RowHeaderTuple rht : rowheader.getRowHeader()) {
      if (!rht.getAttType().equals(RdbmsConstants.FK_FIELD)) {
        props.set(rht.getName(), PropertyValueParser.parse(tuple.getField(rht.getPos())));
      }
    }
    return props;
  }
}