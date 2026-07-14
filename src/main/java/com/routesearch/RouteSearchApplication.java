package com.routesearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Route Search API — a REST service exposing BFS/DFS/A* pathfinding over a city
 * graph, with Redis-backed route caching.
 */
@SpringBootApplication
public class RouteSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteSearchApplication.class, args);
    }
}
