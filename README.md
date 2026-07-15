# Route Search API

Find a route between two cities using BFS, DFS, or A\*, over an HTTP API.
Java 21, Spring Boot 3, Redis for caching.

You give it two cities and an algorithm; it gives you back the path, the number
of hops, and the total distance in miles. Repeated queries are served from
Redis instead of being recomputed.

## Why three algorithms

They optimise for different things, and the responses show it:

- **BFS** — fewest hops. Ignores distance, so the route can be long in miles.
- **DFS** — first path it stumbles into. Not shortest by any measure; included
  mostly for contrast.
- **A\*** — shortest actual distance. Cost so far is the great-circle distance
  walked; the heuristic is the straight-line distance to the goal. That
  heuristic never overshoots, so A\* is guaranteed to return an optimal route.

Seattle → Boston is a good example: BFS returns 12 hops / 4163 mi, A\* returns
13 hops but only 3678 mi.

## Run it

With Docker (starts Redis too):

```bash
docker compose up --build
```

Or locally:

```bash
redis-server --daemonize yes   # optional; without it, routes just aren't cached
mvn spring-boot:run
```

Either way the API is on `http://localhost:8080`.

## Endpoints

**`GET /api/routes`** — `from`, `to`, and optional `algorithm` (`bfs`, `dfs`,
`astar`; defaults to `astar`). City names are case-insensitive.

```bash
curl "localhost:8080/api/routes?from=Seattle&to=Boston&algorithm=astar"
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
    { "city": "Portland", "lat": 45.5152, "lon": -122.6784 }
  ],
  "computeTimeMicros": 1986,
  "cached": false
}
```

Ask for the same route twice and the second response comes back with
`"cached": true`.

**`GET /api/cities`** — every city in the graph, with coordinates.

**`GET /actuator/health`** — health check. It stays `UP` even when Redis is
down, because a missing cache doesn't stop the API from answering.

## The graph

Loaded once at startup from two files under `src/main/resources/graph/`:

```
cities.dat    Seattle  47.6062  -122.3321     # name  lat  lon
edges.dat     Seattle  Portland               # undirected edge
```

`#` lines are comments. The bundled data is 25 US cities wired up along rough
interstate lines. Point `route.graph.cities` / `route.graph.edges` at your own
files to use a different map.

## Config

| Env var                   | Default     | |
|---------------------------|-------------|-|
| `REDIS_HOST`              | `localhost` | |
| `REDIS_PORT`              | `6379`      | |
| `PORT`                    | `8080`      | HTTP port |
| `route.cache.ttl-seconds` | `3600`      | how long a cached route lives |

## Layout

```
model/    Vertex, Graph, GeoDistance (haversine), PathStep, PathResult
graph/    GraphService — reads the data files at startup
search/   Pathfinder (the three algorithms), RouteService (cache-then-compute)
cache/    RouteCacheService — Redis, falls back to recomputing on any error
config/   RedisConfig
web/      controllers + error handling
```

## Tests

```bash
mvn test
```

`PathfinderTest` checks each algorithm's contract on a small hand-built graph
(BFS minimises hops, A\* stays optimal, self-routes are zero, disconnected
cities report not-found). `RouteControllerTest` is a MockMvc slice over the
HTTP layer — JSON shape, the default algorithm, and the 404/400 responses.
