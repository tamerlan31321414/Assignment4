package graph.topo;

import graph.model.Graph;
import graph.metrics.Metrics;

import java.util.*;

public class TopologicalSort {
    private final Graph graph;
    private final Metrics metrics;

    public TopologicalSort(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public TopoResult topologicalOrder() {
        int n = graph.getN();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                inDegree[edge.v]++;
            }
        }

        metrics.startTimer();

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementQueuePushes();
            }
        }

        List<Integer> topoOrder = new ArrayList<>();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.incrementQueuePops();
            topoOrder.add(u);

            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.v;
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementQueuePushes();
                }
            }
        }

        metrics.stopTimer();

        if (topoOrder.size() != n) {
            throw new IllegalArgumentException("Graph has cycles, cannot perform topological sort");
        }

        return new TopoResult(topoOrder);
    }

    public static class TopoResult {
        public final List<Integer> order;

        public TopoResult(List<Integer> order) {
            this.order = order;
        }
    }
}
