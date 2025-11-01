package graph.test;

import graph.scc.TarjanSCC;
import graph.model.Graph;
import graph.metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCCOnCyclicGraphTest {
    private Graph cyclicGraph;
    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new Metrics();
        cyclicGraph = new Graph(4, true);
        cyclicGraph.addEdge(0, 1, 1.0);
        cyclicGraph.addEdge(1, 2, 1.0);
        cyclicGraph.addEdge(2, 0, 1.0);
        cyclicGraph.addEdge(2, 3, 1.0);
    }

    @Test
    void testSCC() {
        TarjanSCC tarjan = new TarjanSCC(cyclicGraph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCC();

        boolean foundCycleComponent = result.components.stream()
                .anyMatch(comp -> comp.size() == 3);
        assertTrue(foundCycleComponent);
    }
}
