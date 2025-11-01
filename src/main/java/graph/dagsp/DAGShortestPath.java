package graph.dagsp;

import graph.model.Graph;
import graph.metrics.Metrics;
import graph.topo.TopologicalSort;

import java.util.*;

public class DAGShortestPath {
    private final Graph graph;
    private final Metrics metrics;

    public DAGShortestPath(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public ShortestPathResult shortestPathFromSource(int source) {
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        List<Integer> topoOrder = topoSort.topologicalOrder().order;

        metrics.startTimer();

        int n = graph.getN();
        double[] dist = new double[n];
        int[] prev = new int[n];

        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        for (int u : topoOrder) {
            if (dist[u] != Double.POSITIVE_INFINITY) {
                for (Graph.Edge edge : graph.getEdges(u)) {
                    metrics.incrementRelaxations();
                    double newDist = dist[u] + edge.weight;
                    if (newDist < dist[edge.v]) {
                        dist[edge.v] = newDist;
                        prev[edge.v] = u;
                    }
                }
            }
        }

        metrics.stopTimer();
        return new ShortestPathResult(dist, prev, source);
    }

    public CriticalPathResult findCriticalPath() {
        Graph invertedGraph = invertWeights();
        DAGShortestPath invertedSP = new DAGShortestPath(invertedGraph, metrics);

        double minDist = Double.POSITIVE_INFINITY;
        int bestSource = -1;
        int bestSink = -1;
        double[] bestDist = null;
        int[] bestPrev = null;

        for (int source = 0; source < graph.getN(); source++) {
            ShortestPathResult result = invertedSP.shortestPathFromSource(source);
            for (int i = 0; i < graph.getN(); i++) {
                if (result.dist[i] < minDist && result.dist[i] != Double.NEGATIVE_INFINITY) {
                    minDist = result.dist[i];
                    bestSource = source;
                    bestSink = i;
                    bestDist = result.dist;
                    bestPrev = result.prev;
                }
            }
        }

        List<Integer> criticalPath = reconstructPath(bestPrev, bestSource, bestSink);
        double criticalLength = -minDist;

        return new CriticalPathResult(criticalPath, criticalLength);
    }

    private Graph invertWeights() {
        Graph inverted = new Graph(graph.getN(), graph.isDirected());
        for (int u = 0; u < graph.getN(); u++) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                inverted.addEdge(edge.u, edge.v, -edge.weight);
            }
        }
        return inverted;
    }

    private List<Integer> reconstructPath(int[] prev, int source, int sink) {
        List<Integer> path = new ArrayList<>();
        for (int v = sink; v != -1; v = prev[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        if (!path.isEmpty() && path.get(0) != source) {
            return new ArrayList<>();
        }
        return path;
    }

    public static class ShortestPathResult {
        public final double[] dist;
        public final int[] prev;
        public final int source;

        public ShortestPathResult(double[] dist, int[] prev, int source) {
            this.dist = dist;
            this.prev = prev;
            this.source = source;
        }
    }

    public static class CriticalPathResult {
        public final List<Integer> path;
        public final double length;

        public CriticalPathResult(List<Integer> path, double length) {
            this.path = path;
            this.length = length;
        }
    }
}
