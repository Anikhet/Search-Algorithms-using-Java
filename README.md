# Route Search API

A REST service exposing **BFS / DFS / A\*** pathfinding over a city graph, built
with **Java 21**, **Spring Boot 3**, and **Redis** route caching.

Given a source and destination city, the API returns the route (ordered list of
cities with coordinates), the number of hops, and the total travel distance in
miles. Computed routes are cached in Redis so repeated queries are served
without recomputation.

## Algorithms

| Algorithm | Optimises for | Notes |
|-----------|---------------|-------|
| **BFS**   | Fewest hops   | Unweighted shortest path |
| **DFS**   | Any path      | Explores depth-first; not distance-optimal |
| **A\***   | Shortest travel distance | `g` = accumulated haversine distance, `h` = straight-line distance to goal (admissible ⇒ optimal) |

Distances use the **haversine** great-circle formula over each city's real
latitude/longitude.

## Tech stack

- Java 21, Spring Boot 3 (Spring Web, Actuator, Validation)
- Spring Data Redis (Lettuce) for route caching, with graceful degradation when
  Redis is unavailable
- Maven build; Docker / Docker Compose for a one-command run

## Running

### With Docker Compose (app + Redis)

```bash
docker compose up --build
```

### Locally

```bash
# start Redis (optional — the API still works without it, just uncached)
redis-server --daemonize yes

mvn spring-boot:run
```

The service listens on `http://localhost:8080`.

## API

### `GET /api/routes`

| Param       | Required | Default | Description |
|-------------|----------|---------|-------------|
| `from`      | yes      | —       | Source city |
| `to`        | yes      | —       | Destination city |
| `algorithm` | no       | `astar` | `bfs`, `dfs`, or `astar` |

City names are matched case-insensitively.

```bash
curl "http://localhost:8080/api/routes?from=Seattle&to=Boston&algorithm=astar"
```

```json
{
  "algorithm": "astar",
  "from": "Seattle",
  "to": "Boston",
  "found": true,
  "hops": 13,
  "totalDistanceMiles": 3677.58,
  "path": [
    { "city": "Seattle", "lat": 47.6062, "lon": -122.3321 },
    { "city": "Portland", "lat": 45.5152, "lon": -122.6784 },
    "..."
  ],
  "computeTimeMicros": 1986,
  "cached": false
}
```

A second identical request returns `"cached": true`, served from Redis.

### `GET /api/cities`

Lists every city in the loaded graph with its coordinates.

### `GET /actuator/health`

Health check endpoint.

## Configuration

Set via environment variables or `src/main/resources/application.yml`:

| Variable                  | Default   | Description |
|---------------------------|-----------|-------------|
| `REDIS_HOST`              | `localhost` | Redis host |
| `REDIS_PORT`              | `6379`    | Redis port |
| `PORT`                    | `8080`    | HTTP port |
| `route.cache.ttl-seconds` | `3600`    | Cache TTL for routes |

## The graph

The city graph loads at startup from two whitespace-delimited resource files:

- `src/main/resources/graph/cities.dat` — `<name> <lat> <lon>`
- `src/main/resources/graph/edges.dat` — `<cityA> <cityB>` (undirected)

Lines starting with `#` are comments. Point `route.graph.cities` /
`route.graph.edges` at other files to swap in a different map.

## Project structure

```
src/main/java/com/routesearch/
├── model/    Vertex, Graph, GeoDistance, PathStep, PathResult
├── graph/    GraphService — loads the graph at startup
├── search/   Algorithm, Pathfinder (BFS/DFS/A*), RouteService
├── cache/    RouteCacheService — Redis caching with graceful fallback
├── config/   RedisConfig
└── web/      RouteController, CityController, error handling
```

## Tests

```bash
mvn test
```

Unit tests cover each algorithm's contract: BFS minimises hops, A\* produces a
connected shortest-distance path, DFS returns a valid path, zero-distance
self-routes, and disconnected (not-found) cases.
