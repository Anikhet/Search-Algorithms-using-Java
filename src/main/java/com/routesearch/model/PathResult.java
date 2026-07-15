package com.routesearch.model;

import java.util.List;

/**
 * Result of a pathfinding query. This is what gets serialized to JSON for the
 * API response and cached in Redis, so it must be a plain bean with getters,
 * setters and a no-arg constructor for Jackson.
 */
public class PathResult {

    private String algorithm;
    private String from;
    private String to;
    private boolean found;
    private int hops;
    private double totalDistanceMiles;
    private List<PathStep> path;
    private long computeTimeMicros;
    private boolean cached;

    public PathResult() {
        // for Jackson deserialization from the Redis cache
    }

    public PathResult(String algorithm, String from, String to, boolean found,
                      int hops, double totalDistanceMiles, List<PathStep> path,
                      long computeTimeMicros) {
        this.algorithm = algorithm;
        this.from = from;
        this.to = to;
        this.found = found;
        this.hops = hops;
        this.totalDistanceMiles = totalDistanceMiles;
        this.path = path;
        this.computeTimeMicros = computeTimeMicros;
        this.cached = false;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public double getTotalDistanceMiles() {
        return totalDistanceMiles;
    }

    public void setTotalDistanceMiles(double totalDistanceMiles) {
        this.totalDistanceMiles = totalDistanceMiles;
    }

    public List<PathStep> getPath() {
        return path;
    }

    public void setPath(List<PathStep> path) {
        this.path = path;
    }

    public long getComputeTimeMicros() {
        return computeTimeMicros;
    }

    public void setComputeTimeMicros(long computeTimeMicros) {
        this.computeTimeMicros = computeTimeMicros;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }
}
