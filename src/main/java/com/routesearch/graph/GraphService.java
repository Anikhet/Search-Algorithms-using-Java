package com.routesearch.graph;

import com.routesearch.model.Graph;
import com.routesearch.model.Vertex;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the city graph once at startup from two whitespace-delimited resources:
 * <pre>
 *   cities file:  &lt;name&gt; &lt;lat&gt; &lt;lon&gt;
 *   edges  file:  &lt;cityA&gt; &lt;cityB&gt;
 * </pre>
 * Lines beginning with {@code #} and blank lines are ignored.
 */
@Service
public class GraphService {

    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    private final ResourceLoader resourceLoader;
    private final String citiesLocation;
    private final String edgesLocation;
    private volatile Graph graph;

    public GraphService(ResourceLoader resourceLoader,
                        @Value("${route.graph.cities:classpath:graph/cities.dat}") String citiesLocation,
                        @Value("${route.graph.edges:classpath:graph/edges.dat}") String edgesLocation) {
        this.resourceLoader = resourceLoader;
        this.citiesLocation = citiesLocation;
        this.edgesLocation = edgesLocation;
    }

    @PostConstruct
    public void load() {
        Graph g = new Graph();
        int cities = loadCities(g);
        int edges = loadEdges(g);
        this.graph = g;
        log.info("Loaded city graph: {} cities, {} edges from {} / {}",
                cities, edges, citiesLocation, edgesLocation);
    }

    private int loadCities(Graph g) {
        int count = 0;
        for (String[] f : readRows(citiesLocation)) {
            if (f.length < 3) {
                throw new IllegalStateException(
                        "Malformed city row (expected: name lat lon): " + String.join(" ", f));
            }
            g.addVertex(f[0], Double.parseDouble(f[1]), Double.parseDouble(f[2]));
            count++;
        }
        if (count == 0) {
            throw new IllegalStateException("No cities loaded from " + citiesLocation);
        }
        return count;
    }

    private int loadEdges(Graph g) {
        int count = 0;
        for (String[] f : readRows(edgesLocation)) {
            if (f.length < 2) {
                throw new IllegalStateException(
                        "Malformed edge row (expected: cityA cityB): " + String.join(" ", f));
            }
            g.addEdge(f[0], f[1]);
            count++;
        }
        return count;
    }

    private List<String[]> readRows(String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException("Graph resource not found: " + location);
        }
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                rows.add(trimmed.split("\\s+"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read graph resource: " + location, e);
        }
        return rows;
    }

    public Graph graph() {
        return graph;
    }

    public List<Vertex> cities() {
        return graph.vertices().stream().toList();
    }
}
