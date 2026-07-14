package com.routesearch.search;

import com.routesearch.model.GeoDistance;
import com.routesearch.model.Graph;
import com.routesearch.model.PathResult;
import com.routesearch.model.PathStep;
import com.routesearch.model.Vertex;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Core pathfinding over a {@link Graph}. Each method reconstructs the path via a
 * parent map and returns a fully-populated {@link PathResult} (hops, total
 * haversine distance and compute time).
 */
@Component
public class Pathfinder {

    public PathResult find(Algorithm algorithm, Graph graph, Vertex start, Vertex goal) {
        long begin = System.nanoTime();
        List<Vertex> path = switch (algorithm) {
            case BFS -> bfs(graph, start, goal);
            case DFS -> dfs(graph, start, goal);
            case ASTAR -> astar(graph, start, goal);
        };
        long micros = (System.nanoTime() - begin) / 1_000;
        return build(algorithm, start, goal, path, micros);
    }

    /** Breadth-first search: minimises the number of hops. */
    private List<Vertex> bfs(Graph graph, Vertex start, Vertex goal) {
        Queue<Vertex> queue = new ArrayDeque<>();
        Set<Vertex> visited = new HashSet<>();
        Map<Vertex, Vertex> parents = new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Vertex curr = queue.poll();
            if (curr == goal) {
                return reconstruct(parents, start, goal);
            }
            for (Vertex neighbour : curr.getNeighbours()) {
                if (visited.add(neighbour)) {
                    parents.put(neighbour, curr);
                    queue.add(neighbour);
                }
            }
        }
        return null;
    }

    /** Depth-first search: finds a path (not necessarily the shortest). */
    private List<Vertex> dfs(Graph graph, Vertex start, Vertex goal) {
        Deque<Vertex> stack = new ArrayDeque<>();
        Set<Vertex> visited = new HashSet<>();
        Map<Vertex, Vertex> parents = new HashMap<>();

        stack.push(start);

        while (!stack.isEmpty()) {
            Vertex curr = stack.pop();
            if (curr == goal) {
                return reconstruct(parents, start, goal);
            }
            if (!visited.add(curr)) {
                continue;
            }
            for (Vertex neighbour : curr.getNeighbours()) {
                if (!visited.contains(neighbour)) {
                    parents.putIfAbsent(neighbour, curr);
                    stack.push(neighbour);
                }
            }
        }
        return null;
    }

    /**
     * A* search: minimises total travel distance. g = accumulated haversine
     * distance from the start, h = straight-line distance to the goal (an
     * admissible heuristic, so the result is optimal).
     */
    private List<Vertex> astar(Graph graph, Vertex start, Vertex goal) {
        Map<Vertex, Double> gScore = new HashMap<>();
        Map<Vertex, Vertex> parents = new HashMap<>();
        Set<Vertex> closed = new HashSet<>();

        gScore.put(start, 0.0);
        PriorityQueue<Vertex> open =
                new PriorityQueue<>(Comparator.comparingDouble(
                        v -> gScore.getOrDefault(v, Double.MAX_VALUE) + GeoDistance.miles(v, goal)));
        open.add(start);

        while (!open.isEmpty()) {
            Vertex curr = open.poll();
            if (curr == goal) {
                return reconstruct(parents, start, goal);
            }
            if (!closed.add(curr)) {
                continue;
            }
            double currG = gScore.getOrDefault(curr, Double.MAX_VALUE);
            for (Vertex neighbour : curr.getNeighbours()) {
                if (closed.contains(neighbour)) {
                    continue;
                }
                double tentativeG = currG + GeoDistance.miles(curr, neighbour);
                if (tentativeG < gScore.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    gScore.put(neighbour, tentativeG);
                    parents.put(neighbour, curr);
                    open.add(neighbour); // lazy decrease-key: stale entries are skipped via `closed`
                }
            }
        }
        return null;
    }

    private List<Vertex> reconstruct(Map<Vertex, Vertex> parents, Vertex start, Vertex goal) {
        List<Vertex> path = new ArrayList<>();
        Vertex node = goal;
        while (node != null && node != start) {
            path.add(node);
            node = parents.get(node);
        }
        path.add(start);
        java.util.Collections.reverse(path);
        return path;
    }

    private PathResult build(Algorithm algorithm, Vertex start, Vertex goal,
                             List<Vertex> path, long micros) {
        if (path == null) {
            return new PathResult(algorithm.apiName(), start.getName(), goal.getName(),
                    false, 0, 0.0, List.of(), micros);
        }
        double distance = 0.0;
        List<PathStep> steps = new ArrayList<>(path.size());
        for (int i = 0; i < path.size(); i++) {
            steps.add(PathStep.of(path.get(i)));
            if (i > 0) {
                distance += GeoDistance.miles(path.get(i - 1), path.get(i));
            }
        }
        double rounded = Math.round(distance * 100.0) / 100.0;
        return new PathResult(algorithm.apiName(), start.getName(), goal.getName(),
                true, path.size() - 1, rounded, steps, micros);
    }
}
