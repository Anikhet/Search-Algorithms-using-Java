package com.routesearch.web;

import com.routesearch.model.PathResult;
import com.routesearch.model.PathStep;
import com.routesearch.search.Algorithm;
import com.routesearch.search.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test: exercises request mapping, parameter binding, JSON
 * serialization and the exception handler with the service mocked out.
 */
@WebMvcTest(RouteController.class)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @Test
    void returnsRouteAsJson() throws Exception {
        PathResult result = new PathResult("astar", "Seattle", "Boston", true, 2, 42.5,
                List.of(new PathStep("Seattle", 47.6, -122.3),
                        new PathStep("Boston", 42.3, -71.0)), 123);
        when(routeService.route(eq(Algorithm.ASTAR), eq("Seattle"), eq("Boston")))
                .thenReturn(result);

        mockMvc.perform(get("/api/routes")
                        .param("from", "Seattle")
                        .param("to", "Boston")
                        .param("algorithm", "astar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("astar"))
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.hops").value(2))
                .andExpect(jsonPath("$.path.length()").value(2));
    }

    @Test
    void defaultsToAstarWhenAlgorithmOmitted() throws Exception {
        when(routeService.route(eq(Algorithm.ASTAR), eq("Seattle"), eq("Boston")))
                .thenReturn(new PathResult("astar", "Seattle", "Boston", true, 0, 0, List.of(), 1));

        mockMvc.perform(get("/api/routes").param("from", "Seattle").param("to", "Boston"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownCityReturns404() throws Exception {
        when(routeService.route(eq(Algorithm.ASTAR), eq("Atlantis"), eq("Boston")))
                .thenThrow(new UnknownCityException("Atlantis"));

        mockMvc.perform(get("/api/routes").param("from", "Atlantis").param("to", "Boston"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Unknown city: 'Atlantis'"));
    }

    @Test
    void unknownAlgorithmReturns400() throws Exception {
        mockMvc.perform(get("/api/routes")
                        .param("from", "Seattle").param("to", "Boston")
                        .param("algorithm", "dijkstra"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void missingRequiredParamReturns400() throws Exception {
        mockMvc.perform(get("/api/routes").param("from", "Seattle"))
                .andExpect(status().isBadRequest());
    }
}
