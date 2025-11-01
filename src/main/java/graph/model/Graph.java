package graph.model;

import java.util.*;

public class Graph {
    private final int n;
    private final List<List<Edge>> adjList;
    private final boolean directed;

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adjList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, double weight) {
        adjList.get(u).add(new Edge(u, v, weight));
        if (!directed) {
            adjList.get(v).add(new Edge(v, u, weight));
        }
    }

    public List<Edge> getEdges(int u) {
        return adjList.get(u);
    }

    public int getN() { return n; }
    public boolean isDirected() { return directed; }

    public int getNodeCount() {
        return n;
    }

    public static class Edge {
        public final int u;
        public final int v;
        public final double weight;

        public Edge(int u, int v, double weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return u + "->" + v + "(" + weight + ")";
        }
    }
}
