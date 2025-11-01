package graph.scc;

import graph.model.Graph;
import graph.metrics.Metrics;

import java.util.*;

public class TarjanSCC {
    private final Graph graph;
    private final Metrics metrics;
    private int index;
    private int[] indices;
    private int[] lowlinks;
    private boolean[] onStack;
    private Deque<Integer> stack;
    private List<List<Integer>> components;

    public TarjanSCC(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public SCCResult findSCC() {
        int n = graph.getN();
        indices = new int[n];
        lowlinks = new int[n];
        onStack = new boolean[n];
        stack = new ArrayDeque<>();
        components = new ArrayList<>();

        Arrays.fill(indices, -1);

        metrics.startTimer();

        for (int v = 0; v < n; v++) {
            if (indices[v] == -1) {
                strongConnect(v);
            }
        }

        metrics.stopTimer();
        return new SCCResult(components, buildCondensationGraph());
    }

    private void strongConnect(int v) {
        metrics.incrementDfsVisits();
        indices[v] = index;
        lowlinks[v] = index;
        index++;
        stack.push(v);
        onStack[v] = true;

        for (Graph.Edge edge : graph.getEdges(v)) {
            metrics.incrementEdgesTraversed();
            int w = edge.v;

            if (indices[w] == -1) {
                strongConnect(w);
                lowlinks[v] = Math.min(lowlinks[v], lowlinks[w]);
            } else if (onStack[w]) {
                lowlinks[v] = Math.min(lowlinks[v], indices[w]);
            }
        }

        if (lowlinks[v] == indices[v]) {
            List<Integer> component = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                component.add(w);
            } while (w != v);
            components.add(component);
        }
    }

    private Graph buildCondensationGraph() {
        Map<Integer, Integer> vertexToComponent = new HashMap<>();
        for (int i = 0; i < components.size(); i++) {
            for (int vertex : components.get(i)) {
                vertexToComponent.put(vertex, i);
            }
        }

        Graph condensation = new Graph(components.size(), true);
        Set<String> addedEdges = new HashSet<>();

        for (int u = 0; u < graph.getN(); u++) {
            int compU = vertexToComponent.get(u);
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.v;
                int compV = vertexToComponent.get(v);
                if (compU != compV) {
                    String edgeKey = compU + "->" + compV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(compU, compV, 0);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    public static class SCCResult {
        public final List<List<Integer>> components;
        public final Graph condensationGraph;

        public SCCResult(List<List<Integer>> components, Graph condensationGraph) {
            this.components = components;
            this.condensationGraph = condensationGraph;
        }
    }
}
