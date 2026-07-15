package com.routesearch.search;

import java.util.Locale;

/**
 * Supported pathfinding strategies.
 * <ul>
 *   <li>{@code BFS} – fewest hops (unweighted shortest path)</li>
 *   <li>{@code DFS} – any path, explores deep first (not optimal)</li>
 *   <li>{@code ASTAR} – shortest travel distance using a haversine heuristic</li>
 * </ul>
 */
public enum Algorithm {
    BFS,
    DFS,
    ASTAR;

    public static Algorithm from(String value) {
        if (value == null || value.isBlank()) {
            return ASTAR;
        }
        String v = value.trim().toUpperCase(Locale.ROOT);
        return switch (v) {
            case "BFS" -> BFS;
            case "DFS" -> DFS;
            case "ASTAR", "A*", "A_STAR", "A-STAR" -> ASTAR;
            default -> throw new IllegalArgumentException(
                    "Unknown algorithm '" + value + "'. Use one of: bfs, dfs, astar");
        };
    }

    public String apiName() {
        return this == ASTAR ? "astar" : name().toLowerCase(Locale.ROOT);
    }
}
