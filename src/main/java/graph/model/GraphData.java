package graph.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GraphData {
    @JsonProperty("directed")
    public boolean directed;

    @JsonProperty("n")
    public int n;

    @JsonProperty("edges")
    public List<EdgeData> edges;

    @JsonProperty("source")
    public int source;

    @JsonProperty("weight_model")
    public String weightModel;

    public static class EdgeData {
        @JsonProperty("u")
        public int u;

        @JsonProperty("v")
        public int v;

        @JsonProperty("w")
        public double w;
    }

    public graph.model.Graph toGraph() {
        graph.model.Graph graph = new graph.model.Graph(n, directed);
        for (EdgeData edge : edges) {
            graph.addEdge(edge.u, edge.v, edge.w);
        }
        return graph;
    }
}
