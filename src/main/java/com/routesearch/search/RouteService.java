package com.routesearch.search;

import com.routesearch.cache.RouteCacheService;
import com.routesearch.graph.GraphService;
import com.routesearch.model.Graph;
import com.routesearch.model.PathResult;
import com.routesearch.model.Vertex;
import com.routesearch.web.UnknownCityException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Orchestrates a route query: validate cities, consult the Redis cache, and on
 * a miss run the requested algorithm and cache the result.
 */
@Service
public class RouteService {

    private final GraphService graphService;
    private final Pathfinder pathfinder;
    private final RouteCacheService cache;

    public RouteService(GraphService graphService, Pathfinder pathfinder, RouteCacheService cache) {
        this.graphService = graphService;
        this.pathfinder = pathfinder;
        this.cache = cache;
    }

    public PathResult route(Algorithm algorithm, String from, String to) {
        Graph graph = graphService.graph();
        Vertex start = graph.get(from);
        Vertex goal = graph.get(to);
        if (start == null) {
            throw new UnknownCityException(from);
        }
        if (goal == null) {
            throw new UnknownCityException(to);
        }

        Optional<PathResult> cached = cache.get(algorithm, from, to);
        if (cached.isPresent()) {
            return cached.get();
        }

        PathResult result = pathfinder.find(algorithm, graph, start, goal);
        cache.put(algorithm, from, to, result);
        return result;
    }
}
