/*
 * This file is part of Gradoop.
 *
 * Gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradoop.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.operators.combination;

import org.apache.flink.api.java.DataSet;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.impl.LogicalGraph;
import org.gradoop.model.impl.functions.epgm.Id;
import org.gradoop.model.impl.operators.base.BinaryGraphToGraphOperatorBase;

/**
 * Creates a new logical graph by combining the vertex and edge sets of two
 * input graphs. Vertex and edge equality is based on their
 * respective identifiers.
 *
 * @param <G> EPGM graph head type
 * @param <V> EPGM vertex type
 * @param <E> EPGM edge type
 */
public class Combination
  <G extends EPGMGraphHead, V extends EPGMVertex, E extends EPGMEdge>
  extends BinaryGraphToGraphOperatorBase<G, V, E> {

  /**
   * {@inheritDoc}
   */
  @Override
  protected LogicalGraph<G, V, E> executeInternal(
    LogicalGraph<G, V, E> firstGraph, LogicalGraph<G, V, E> secondGraph) {

    // build distinct union of vertex sets and update graph ids at vertices
    // cannot use Gelly union here because of missing argument for KeySelector
    DataSet<V> newVertexSet = firstGraph.getVertices()
      .union(secondGraph.getVertices())
      .distinct(new Id<V>());

    DataSet<E> newEdgeSet = firstGraph.getEdges()
      .union(secondGraph.getEdges())
      .distinct(new Id<E>());

    return LogicalGraph.fromDataSets(
      newVertexSet, newEdgeSet, firstGraph.getConfig());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return Combination.class.getName();
  }
}