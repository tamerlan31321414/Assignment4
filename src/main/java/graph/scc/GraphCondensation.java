package graph.scc;

import graph.model.Graph;
import java.util.*;

public class GraphCondensation {
    private final List<List<Integer>> components;
    private final Graph condensationGraph;
    private final int[] componentId;

    public GraphCondensation(List<List<Integer>> components, Graph originalGraph) {
        this.components = components;
        this.componentId = new int[originalGraph.getNodeCount()];
        this.condensationGraph = buildCondensationGraph(originalGraph);
    }

    private Graph buildCondensationGraph(Graph originalGraph) {
        for (int i = 0; i < components.size(); i++) {
            for (int node : components.get(i)) {
                componentId[node] = i;
            }
        }

        Graph condensation = new Graph(components.size(), true);
        Set<String> addedEdges = new HashSet<>();

        for (int u = 0; u < originalGraph.getNodeCount(); u++) {
            for (Graph.Edge edge : originalGraph.getEdges(u)) {
                int v = edge.v;
                int compU = componentId[u];
                int compV = componentId[v];

                if (compU != compV) {
                    String edgeKey = compU + "->" + compV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(compU, compV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    public Graph getCondensationGraph() {
        return condensationGraph;
    }

    public int[] getComponentId() {
        return componentId;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }
}
