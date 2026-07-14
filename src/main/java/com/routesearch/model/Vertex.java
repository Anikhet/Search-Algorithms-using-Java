package com.routesearch.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in the city graph: a named location with geographic coordinates
 * and an adjacency list of neighbouring vertices.
 */
public class Vertex {

    private final String name;
    private final double lat;
    private final double lon;
    private final List<Vertex> neighbours = new ArrayList<>();

    public Vertex(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public void addNeighbour(Vertex neighbour) {
        if (!neighbours.contains(neighbour)) {
            neighbours.add(neighbour);
        }
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public List<Vertex> getNeighbours() {
        return neighbours;
    }

    @Override
    public String toString() {
        return name;
    }
}
