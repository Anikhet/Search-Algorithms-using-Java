package com.routesearch.web;

import com.routesearch.graph.GraphService;
import com.routesearch.model.PathStep;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lists the cities available in the loaded graph. */
@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final GraphService graphService;

    public CityController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping
    public List<PathStep> cities() {
        return graphService.cities().stream()
                .map(PathStep::of)
                .toList();
    }
}
