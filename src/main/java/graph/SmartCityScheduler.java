package graph;

import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import graph.model.Graph;
import graph.model.GraphData;
import graph.metrics.Metrics;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class SmartCityScheduler {
    private final ObjectMapper mapper = new ObjectMapper();

    public void processGraph(String filename) throws Exception {
        System.out.println("=== Processing: " + filename + " ===");

        File file = new File("data/" + filename);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        GraphData graphData = mapper.readValue(file, GraphData.class);
        Graph graph = graphData.toGraph();

        System.out.println("Graph: " + graphData.n + " vertices, " + graphData.edges.size() + " edges");
        System.out.println("Source: " + graphData.source + ", Weight model: " + graphData.weightModel);

        System.out.println("\n--- Strongly Connected Components ---");
        Metrics sccMetrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(graph, sccMetrics);
        TarjanSCC.SCCResult sccResult = tarjan.findSCC();

        System.out.println("Found " + sccResult.components.size() + " SCCs:");
        for (int i = 0; i < sccResult.components.size(); i++) {
            System.out.println("  SCC " + i + ": " + sccResult.components.get(i) +
                    " (size: " + sccResult.components.get(i).size() + ")");
        }
        System.out.println("SCC Metrics: " + sccMetrics);

        System.out.println("\n--- Topological Sort ---");
        Metrics topoMetrics = new Metrics();
        TopologicalSort topoSort = new TopologicalSort(sccResult.condensationGraph, topoMetrics);

        try {
            TopologicalSort.TopoResult topoResult = topoSort.topologicalOrder();
            System.out.println("Topological Order of Components: " + topoResult.order);
            System.out.println("Topo Sort Metrics: " + topoMetrics);
        } catch (IllegalArgumentException e) {
            System.out.println("Cannot perform topological sort: " + e.getMessage());
        }

        System.out.println("\n--- Shortest Paths in DAG ---");
        Metrics spMetrics = new Metrics();

        if (sccResult.components.size() == graph.getN()) {
            DAGShortestPath dagSP = new DAGShortestPath(graph, spMetrics);
            DAGShortestPath.ShortestPathResult spResult = dagSP.shortestPathFromSource(graphData.source);
            System.out.println("Shortest distances from source " + graphData.source + ":");
            for (int i = 0; i < graph.getN(); i++) {
                if (spResult.dist[i] != Double.POSITIVE_INFINITY) {
                    System.out.println("  to " + i + ": " + spResult.dist[i]);
                } else {
                    System.out.println("  to " + i + ": unreachable");
                }
            }

            DAGShortestPath.CriticalPathResult criticalResult = dagSP.findCriticalPath();
            System.out.println("Critical Path: " + criticalResult.path);
            System.out.println("Critical Path Length: " + criticalResult.length);
            System.out.println("Shortest Path Metrics: " + spMetrics);
        } else {
            System.out.println("Graph has cycles, performing DAG algorithms on condensation graph...");

            Graph weightedCondensation = buildWeightedCondensationGraph(graph, sccResult.components);
            DAGShortestPath dagSP = new DAGShortestPath(weightedCondensation, spMetrics);

            int sourceComponent = findComponentForVertex(sccResult.components, graphData.source);

            DAGShortestPath.ShortestPathResult spResult = dagSP.shortestPathFromSource(sourceComponent);
            System.out.println("Shortest distances from source " + graphData.source + " (component " + sourceComponent + "):");
            for (int i = 0; i < weightedCondensation.getN(); i++) {
                if (spResult.dist[i] != Double.POSITIVE_INFINITY) {
                    System.out.println("  to component " + i + ": " + spResult.dist[i]);
                    System.out.println("    vertices in component: " + sccResult.components.get(i));
                } else {
                    System.out.println("  to component " + i + ": unreachable");
                }
            }

            DAGShortestPath.CriticalPathResult criticalResult = dagSP.findCriticalPath();
            List<Integer> originalCriticalPath = convertToOriginalVertices(criticalResult.path, sccResult.components);
            System.out.println("Critical Path (components): " + criticalResult.path);
            System.out.println("Critical Path (original vertices): " + originalCriticalPath);
            System.out.println("Critical Path Length: " + criticalResult.length);
            System.out.println("Shortest Path Metrics: " + spMetrics);
        }

        System.out.println("=== Finished: " + filename + " ===\n");
    }

    private Graph buildWeightedCondensationGraph(Graph originalGraph, List<List<Integer>> components) {
        Map<Integer, Integer> vertexToComponent = new HashMap<>();
        for (int i = 0; i < components.size(); i++) {
            for (int vertex : components.get(i)) {
                vertexToComponent.put(vertex, i);
            }
        }

        Graph condensation = new Graph(components.size(), true);
        Map<String, Double> minWeights = new HashMap<>();

        for (int u = 0; u < originalGraph.getN(); u++) {
            int compU = vertexToComponent.get(u);
            for (Graph.Edge edge : originalGraph.getEdges(u)) {
                int v = edge.v;
                int compV = vertexToComponent.get(v);
                if (compU != compV) {
                    String edgeKey = compU + "->" + compV;
                    if (!minWeights.containsKey(edgeKey) || edge.weight < minWeights.get(edgeKey)) {
                        minWeights.put(edgeKey, edge.weight);
                    }
                }
            }
        }

        for (Map.Entry<String, Double> entry : minWeights.entrySet()) {
            String[] parts = entry.getKey().split("->");
            int compU = Integer.parseInt(parts[0]);
            int compV = Integer.parseInt(parts[1]);
            condensation.addEdge(compU, compV, entry.getValue());
        }

        return condensation;
    }

    private int findComponentForVertex(List<List<Integer>> components, int vertex) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).contains(vertex)) {
                return i;
            }
        }
        return -1;
    }

    private List<Integer> convertToOriginalVertices(List<Integer> componentPath, List<List<Integer>> components) {
        List<Integer> result = new ArrayList<>();
        if (componentPath.isEmpty()) return result;
        for (int compId : componentPath) {
            if (!components.get(compId).isEmpty()) {
                result.add(components.get(compId).get(0));
            }
        }
        return result;
    }

    private void createDefaultTasksFile() throws Exception {
        GraphData data = new GraphData();
        data.directed = true;
        data.n = 8;
        data.source = 4;
        data.weightModel = "edge";

        List<GraphData.EdgeData> edges = new ArrayList<>();
        edges.add(createEdge(0, 1, 3));
        edges.add(createEdge(1, 2, 2));
        edges.add(createEdge(2, 3, 4));
        edges.add(createEdge(3, 1, 1));
        edges.add(createEdge(4, 5, 2));
        edges.add(createEdge(5, 6, 5));
        edges.add(createEdge(6, 7, 1));
        data.edges = edges;

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("data/tasks.json"), data);
        System.out.println("Created default tasks.json");
    }

    private GraphData.EdgeData createEdge(int u, int v, double w) {
        GraphData.EdgeData edge = new GraphData.EdgeData();
        edge.u = u;
        edge.v = v;
        edge.w = w;
        return edge;
    }

    public static void main(String[] args) {
        SmartCityScheduler scheduler = new SmartCityScheduler();

        try {
            new File("data").mkdirs();

            File tasksFile = new File("data/tasks.json");
            if (!tasksFile.exists()) {
                System.out.println("Creating default tasks.json file...");
                scheduler.createDefaultTasksFile();
            }

            scheduler.processGraph("tasks.json");

            DatasetGenerator generator = new DatasetGenerator();
            generator.generateAllDatasets();

            for (int i = 1; i <= 9; i++) {
                scheduler.processGraph("dataset" + i + ".json");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
