package graph.metrics;

public class Metrics {
    private long startTime;
    private long endTime;
    private int dfsVisits;
    private int edgesTraversed;
    private int queuePushes;
    private int queuePops;
    private int relaxations;

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        endTime = System.nanoTime();
    }

    public long getElapsedTime() {
        return endTime - startTime;
    }

    public void incrementDfsVisits() { dfsVisits++; }
    public void incrementEdgesTraversed() { edgesTraversed++; }
    public void incrementQueuePushes() { queuePushes++; }
    public void incrementQueuePops() { queuePops++; }
    public void incrementRelaxations() { relaxations++; }

    public int getDfsVisits() { return dfsVisits; }
    public int getEdgesTraversed() { return edgesTraversed; }
    public int getQueuePushes() { return queuePushes; }
    public int getQueuePops() { return queuePops; }
    public int getRelaxations() { return relaxations; }

    public void reset() {
        dfsVisits = edgesTraversed = queuePushes = queuePops = relaxations = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Time: %d ns, DFS Visits: %d, Edges: %d, Queue Pushes: %d, Queue Pops: %d, Relaxations: %d",
                getElapsedTime(), dfsVisits, edgesTraversed, queuePushes, queuePops, relaxations
        );
    }
}
