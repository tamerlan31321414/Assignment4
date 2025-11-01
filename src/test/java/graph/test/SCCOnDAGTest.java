package graph.test;

import graph.scc.TarjanSCC;
import graph.model.Graph;
import graph.metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCCOnDAGTest {
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
    void testSCC() {
        TarjanSCC tarjan = new TarjanSCC(simpleGraph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCC();

        assertEquals(4, result.components.size());
        assertTrue(metrics.getDfsVisits() > 0);
    }
}
