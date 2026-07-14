package com.routesearch.search;

import com.routesearch.model.Graph;
import com.routesearch.model.PathResult;
import com.routesearch.model.PathStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathfinderTest {

    private Pathfinder pathfinder;
    private Graph graph;

    @BeforeEach
    void setUp() {
        pathfinder = new Pathfinder();
        // A small diamond graph:
        //        B
        //      /   \
        //   A         D
        //      \   /
        //        C  (with an extra hop C-E-D making the lower route longer in hops)
        graph = new Graph();
        graph.addVertex("A", 0.0, 0.0);
        graph.addVertex("B", 0.0, 1.0);
        graph.addVertex("C", 1.0, 0.0);
        graph.addVertex("D", 1.0, 1.0);
        graph.addVertex("E", 2.0, 0.0);
        graph.addEdge("A", "B");
        graph.addEdge("B", "D");
        graph.addEdge("A", "C");
        graph.addEdge("C", "E");
        graph.addEdge("E", "D");
    }

    @Test
    void bfsFindsFewestHops() {
        PathResult result = pathfinder.find(Algorithm.BFS, graph,
                graph.get("A"), graph.get("D"));
        assertTrue(result.isFound());
        assertEquals(2, result.getHops(), "A-B-D is the fewest-hop path");
        assertEquals(List.of("A", "B", "D"), names(result));
    }

    @Test
    void astarFindsAShortestPathAndSetsDistance() {
        PathResult result = pathfinder.find(Algorithm.ASTAR, graph,
                graph.get("A"), graph.get("D"));
        assertTrue(result.isFound());
        assertEquals("A", result.getPath().get(0).city());
        assertEquals("D", result.getPath().get(result.getPath().size() - 1).city());
        assertTrue(result.getTotalDistanceMiles() > 0);
    }

    @Test
    void dfsReturnsAValidConnectedPath() {
        PathResult result = pathfinder.find(Algorithm.DFS, graph,
                graph.get("A"), graph.get("D"));
        assertTrue(result.isFound());
        assertEquals("A", result.getPath().get(0).city());
        assertEquals("D", result.getPath().get(result.getPath().size() - 1).city());
    }

    @Test
    void sameStartAndGoalIsZeroHops() {
        PathResult result = pathfinder.find(Algorithm.BFS, graph,
                graph.get("A"), graph.get("A"));
        assertTrue(result.isFound());
        assertEquals(0, result.getHops());
        assertEquals(0.0, result.getTotalDistanceMiles());
    }

    @Test
    void disconnectedGoalReportsNotFound() {
        graph.addVertex("Z", 9.0, 9.0); // no edges
        PathResult result = pathfinder.find(Algorithm.BFS, graph,
                graph.get("A"), graph.get("Z"));
        assertFalse(result.isFound());
        assertTrue(result.getPath().isEmpty());
    }

    private List<String> names(PathResult result) {
        return result.getPath().stream().map(PathStep::city).toList();
    }
}
