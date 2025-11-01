package graph.test;

import graph.dagsp.DAGShortestPath;
import graph.model.Graph;
import graph.metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DAGCriticalPathTest {
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
    void testCriticalPath() {
        DAGShortestPath dagSP = new DAGShortestPath(simpleGraph, metrics);
        DAGShortestPath.CriticalPathResult result = dagSP.findCriticalPath();

        assertTrue(result.length > 0);
        assertFalse(result.path.isEmpty());
    }
}
