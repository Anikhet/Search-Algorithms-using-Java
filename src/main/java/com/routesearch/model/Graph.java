package com.routesearch.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An undirected graph of cities. Vertices are keyed by their (case-insensitive)
 * name so lookups from API requests are forgiving of casing.
 */
public class Graph {

    private final Map<String, Vertex> vertices = new LinkedHashMap<>();

    public void addVertex(String name, double lat, double lon) {
        vertices.putIfAbsent(key(name), new Vertex(name, lat, lon));
    }

    /**
     * Adds an undirected edge between two existing vertices.
     *
     * @throws IllegalArgumentException if either endpoint is unknown
     */
    public void addEdge(String src, String dest) {
        Vertex a = get(src);
        Vertex b = get(dest);
        if (a == null || b == null) {
            throw new IllegalArgumentException(
                    "Cannot add edge between unknown vertices: " + src + " -> " + dest);
        }
        a.addNeighbour(b);
        b.addNeighbour(a);
    }

    public Vertex get(String name) {
        return name == null ? null : vertices.get(key(name));
    }

    public boolean contains(String name) {
        return get(name) != null;
    }

    public Collection<Vertex> vertices() {
        return vertices.values();
    }

    public int size() {
        return vertices.size();
    }

    private static String key(String name) {
        return name.toLowerCase();
    }
}
