package graph;

import graph.model.GraphData;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class DatasetGenerator {
    private final Random random = new Random(42);
    private final ObjectMapper mapper = new ObjectMapper();

    public void generateAllDatasets() throws Exception {
        new File("data").mkdirs();

        generateDataset("data/dataset1.json", 8, 10, 0.3, true, "Simple case with 1-2 cycles");
        generateDataset("data/dataset2.json", 10, 12, 0.2, false, "Pure DAG");
        generateDataset("data/dataset3.json", 7, 15, 0.4, true, "Mixed structure");
        generateDataset("data/dataset4.json", 15, 25, 0.25, true, "Multiple SCCs");
        generateDataset("data/dataset5.json", 18, 30, 0.35, true, "Dense cyclic");
        generateDataset("data/dataset6.json", 12, 18, 0.15, false, "Sparse DAG");
        generateDataset("data/dataset7.json", 25, 60, 0.2, true, "Large mixed");
        generateDataset("data/dataset8.json", 35, 100, 0.3, true, "Large dense");
        generateDataset("data/dataset9.json", 20, 30, 0.1, false, "Large sparse DAG");

        System.out.println("Generated 9 datasets in data/ directory");
    }

    private void generateDataset(String filename, int n, int maxEdges, double cycleProbability,
                                 boolean allowCycles, String description) throws Exception {
        GraphData data = new GraphData();
        data.directed = true;
        data.n = n;
        data.source = random.nextInt(n);
        data.weightModel = "edge";
        data.edges = new ArrayList<>();

        Set<String> edgeSet = new HashSet<>();

        if (!allowCycles) {
            for (int i = 0; i < n - 1; i++) {
                int j = i + 1 + random.nextInt(n - i - 1);
                if (j < n) {
                    addEdge(data, edgeSet, i, j);
                }
            }
        }

        while (data.edges.size() < maxEdges && edgeSet.size() < n * (n - 1)) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);

            if (u != v) {
                if (allowCycles || isDAGEdgeSafe(data, u, v)) {
                    addEdge(data, edgeSet, u, v);
                }
            }
        }

        if (allowCycles && random.nextDouble() < cycleProbability && n >= 3) {
            createCycle(data, edgeSet, n);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), data);

        System.out.println("Generated: " + filename + " - " + description +
                " (n=" + n + ", edges=" + data.edges.size() + ")");
    }

    private void addEdge(GraphData data, Set<String> edgeSet, int u, int v) {
        String edgeKey = u + "->" + v;
        if (!edgeSet.contains(edgeKey)) {
            GraphData.EdgeData edge = new GraphData.EdgeData();
            edge.u = u;
            edge.v = v;
            edge.w = 1 + random.nextInt(10);
            data.edges.add(edge);
            edgeSet.add(edgeKey);
        }
    }

    private boolean isDAGEdgeSafe(GraphData data, int u, int v) {
        return u < v;
    }

    private void createCycle(GraphData data, Set<String> edgeSet, int n) {
        int cycleSize = 3 + random.nextInt(2);
        List<Integer> nodes = new ArrayList<>();
        Set<Integer> selected = new HashSet<>();

        while (selected.size() < cycleSize) {
            int node = random.nextInt(n);
            if (selected.add(node)) {
                nodes.add(node);
            }
        }

        for (int i = 0; i < cycleSize; i++) {
            int u = nodes.get(i);
            int v = nodes.get((i + 1) % cycleSize);
            addEdge(data, edgeSet, u, v);
        }
    }

    public static void main(String[] args) throws Exception {
        new DatasetGenerator().generateAllDatasets();
    }
}
