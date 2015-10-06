package org.gradoop.model.impl;

import org.gradoop.model.FlinkTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.gradoop.GradoopTestBaseUtils.LABEL_COMMUNITY;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class EPGMDatabaseTest extends FlinkTestBase {

  public EPGMDatabaseTest(TestExecutionMode mode) {
    super(mode);
  }

  @Test
  public void testGetExistingGraph() throws Exception {
    LogicalGraph<DefaultVertexData, DefaultEdgeData, DefaultGraphData> g =
      getGraphStore().getGraph(0L);
    assertNotNull("graph was null", g);
    assertEquals("vertex set has the wrong size", 3L, g.getVertices().count());
    assertEquals("edge set has the wrong size", 4L, g.getEdges().count());
    assertEquals("wrong label", LABEL_COMMUNITY, g.getLabel());
  }

  @Test
  public void testNonExistingGraph() throws Exception {
    assertNull("graph was not null", getGraphStore().getGraph(4L));
  }

  @Test
  public void testGetGraphs() throws Exception {
    GraphCollection<DefaultVertexData, DefaultEdgeData, DefaultGraphData>
      graphColl = getGraphStore().getCollection();

    GraphCollection<DefaultVertexData, DefaultEdgeData, DefaultGraphData>
      graphs = graphColl.getGraphs(0L, 1L, 2L);

    assertNotNull("graph collection is null", graphs);
    assertEquals("wrong number of graphs", 3L, graphs.size());
    assertEquals("wrong number of vertices", 6L, graphs.getVertexCount());
    assertEquals("wrong number of edges", 10L, graphs.getEdgeCount());
  }

  @Test
  public void testGetCollection() throws Exception {
    GraphCollection<DefaultVertexData, DefaultEdgeData, DefaultGraphData>
      graphColl = getGraphStore().getCollection();

    assertNotNull("graph collection was null", graphColl);
    assertEquals("graph collection has wrong size", 4L,
      graphColl.getGraphCount());
    assertEquals("graph collection has wrong vertex count", 7L,
      graphColl.getVertexCount());
    assertEquals("graph collection has wrong edge count", 13L,
      graphColl.getEdgeCount());
  }
}