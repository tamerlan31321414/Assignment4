package graph.test;

import graph.topo.TopologicalSort;
import graph.model.Graph;
import graph.metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortTest {
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
    void testTopoSort() {
        TopologicalSort topoSort = new TopologicalSort(simpleGraph, metrics);
        TopologicalSort.TopoResult result = topoSort.topologicalOrder();

        assertEquals(4, result.order.size());
        assertTrue(metrics.getQueuePushes() > 0);
    }
}
