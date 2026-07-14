package com.routesearch.model;

/**
 * One city along a computed route, with its coordinates. Serialized into the
 * API response so clients can plot the path.
 */
public record PathStep(String city, double lat, double lon) {

    public static PathStep of(Vertex v) {
        return new PathStep(v.getName(), v.getLat(), v.getLon());
    }
}
