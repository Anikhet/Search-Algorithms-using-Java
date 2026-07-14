package com.routesearch.web;

import com.routesearch.model.PathResult;
import com.routesearch.search.Algorithm;
import com.routesearch.search.RouteService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pathfinding endpoint.
 *
 * <pre>
 *   GET /api/routes?from=Seattle&amp;to=Boston&amp;algorithm=astar
 * </pre>
 */
@RestController
@RequestMapping("/api/routes")
@Validated
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public PathResult route(
            @RequestParam @NotBlank String from,
            @RequestParam @NotBlank String to,
            @RequestParam(required = false, defaultValue = "astar") String algorithm) {
        Algorithm algo = Algorithm.from(algorithm);
        return routeService.route(algo, from, to);
    }
}
