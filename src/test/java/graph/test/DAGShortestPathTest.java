package graph.test;

import graph.dagsp.DAGShortestPath;
import graph.model.Graph;
import graph.metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DAGShortestPathTest {
    private Graph simpleGraph;
    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
        simpleGraph = new Graph(4, true);
        simpleGraph.addEdge(0, 1, 2.0);
        simpleGraph.addEdge(1, 2, 3.0);
        simpleGraph.addEdge(2, 3, 1.0);
        simpleGraph.addEdge(0, 3, 5.0);
    }

    @Test
    void testShortestPath() {
        DAGShortestPath dagSP = new DAGShortestPath(simpleGraph, metrics);
        DAGShortestPath.ShortestPathResult result = dagSP.shortestPathFromSource(0);

        assertEquals(0.0, result.dist[0], 0.001);
        assertEquals(2.0, result.dist[1], 0.001);
        assertEquals(5.0, result.dist[2], 0.001);
        assertEquals(5.0, result.dist[3], 0.001);
    }
}
