package com.routesearch.model;

/**
 * Great-circle distance between two lat/lon points using the haversine formula.
 * Used both as edge cost (g) and as the A* heuristic (h). Because straight-line
 * distance never overestimates the real path distance, the heuristic is
 * admissible and A* returns an optimal (shortest-distance) path.
 */
public final class GeoDistance {

    private static final double EARTH_RADIUS_MILES = 3958.8;

    private GeoDistance() {
    }

    public static double miles(Vertex a, Vertex b) {
        return miles(a.getLat(), a.getLon(), b.getLat(), b.getLon());
    }

    public static double miles(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);
        double h = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLon * sinLon;
        return 2 * EARTH_RADIUS_MILES * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }
}
