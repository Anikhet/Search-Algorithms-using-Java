import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class Vertex{
    protected final String vertex;
    protected final float lat;
    protected final float lon;
    protected LinkedList<Vertex> neighbours = new LinkedList<>();
    protected LinkedList<String> neighbours_string = new LinkedList<>();
    protected LinkedList<String> neighbours_reverse = new LinkedList<>();

    Vertex(String vertex, float lat, float lon) {
        this.vertex = vertex;
        this.lat = lat;
        this.lon = lon;
    }
    void add_neighbour(Vertex neighbour) {
        neighbours.add(neighbour);
        neighbours_string.add(neighbour.vertex);
        neighbours_reverse.add(neighbour.vertex);
    }
}


class Graph {

    protected HashMap<String,Vertex> vertices = new HashMap<>();
    protected LinkedList<String> lists = new LinkedList<>();

    void add_vertex(String src, float lat, float lon){
        Vertex temp = new Vertex(src, lat, lon);
        vertices.put(src,temp);
        lists.add(src);
    }
    void add_edge (String src, String dest) {
        vertices.get(src).add_neighbour(vertices.get(dest));
        vertices.get(dest).add_neighbour(vertices.get(src));
    }
}

class Search {

    public static List<Vertex> dfs(Vertex start, Vertex dest,Graph city) {
        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();
        HashMap<Vertex, Vertex> parents = new HashMap<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            Vertex curr = stack.pop();
            visited.add(curr);

            if (curr == dest) {
                List<Vertex> path = new ArrayList<>();
                Vertex node = dest;
                while (node != start) {
                    path.add(node);
                    node = parents.get(node);
                }
                path.add(start);
                Collections.reverse(path);
                return path;
            }
            for (int index = 0; index < curr.neighbours_reverse.size();index++) {
                Vertex neighbor = city.vertices.get(curr.neighbours_reverse.get(index));
                if (!visited.contains(neighbor) && (!stack.contains(neighbor))) {
                    parents.put(neighbor, curr);
                    stack.push(neighbor);
                }
            }
        }
        return null;
    }

    public static List<Vertex> bfs(Vertex start, Vertex dest,Graph city) {
        Queue<Vertex> queue = new LinkedList<>();
        Set<Vertex> visited = new HashSet<>();
        Map<Vertex, Vertex> parentMap = new HashMap<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Vertex curr = queue.remove();
            visited.add(curr);

            if (curr == dest) {
                List<Vertex> path = new ArrayList<>();
                Vertex node = dest;
                while (node != start) {
                    path.add(node);
                    node = parentMap.get(node);
                }
                path.add(start);
                Collections.reverse(path);
                return path;
            }
            for (int index = 0; index < curr.neighbours_string.size();index++) {
                Vertex neighbor = city.vertices.get(curr.neighbours_string.get(index));
                if (!visited.contains(neighbor) && (!queue.contains(neighbor))) {
                    parentMap.put(neighbor, curr);
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }


    public static List<Vertex> astar(Vertex start,Vertex target, Graph city){
        HashMap<Vertex,Integer> heuristic = new HashMap<>();
        HashMap<Integer,Vertex> heuristic_ = new HashMap<>();
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        Set<Vertex> visited = new HashSet<>();
        Map<Vertex, Vertex> parentMap = new HashMap<>();

        int distance = 0;
        for (int index = 0; index < city.lists.size(); index++){
            float lat1 = city.vertices.get(city.lists.get(index)).lat;
            float lon1 = city.vertices.get(city.lists.get(index)).lon;
            float lat2 = target.lat;
            float lon2 = target.lon;
            distance += Math.sqrt( (lat1-lat2)*(lat1-lat2) + (lon1-lon2)*(lon1-lon2) ) * 100;
            heuristic.put(city.vertices.get(city.lists.get(index)),distance);
            heuristic_.put(distance,city.vertices.get(city.lists.get(index)));
        }
        heap.add(heuristic.get(start));
        if (start == target){
            System.out.println(start.vertex);
            System.out.println(target.vertex);
            System.out.println("That took 1 hops to find.");
            System.out.println("Total distance = 0 miles.");
        }

        while (!heap.isEmpty()){
            Vertex curr =  heuristic_.get(heap.poll());
            visited.add(curr);

            if (curr == target) {
                List<Vertex> path = new ArrayList<>();
                Vertex node = target;
                while (node != start) {
                    path.add(node);
                    node = parentMap.get(node);
                }
                path.add(start);
                Collections.reverse(path);
                return path;
            }

            for (int index = 0; index < curr.neighbours_string.size();index++) {
                Vertex neighbor = city.vertices.get(curr.neighbours_string.get(index));
                if (!visited.contains(neighbor) && (!heap.contains(heuristic.get(neighbor)))) {
                    parentMap.put(neighbor, curr);
                    heap.add(heuristic.get(neighbor));
                }
            }
        }
        return null;
    }


    public static void sort(Graph city){
        for(int index = 0 ; index < city.lists.size();  index++){
            String city_name = city.lists.get(index);
            Collections.sort(city.vertices.get(city_name).neighbours_string);
            Collections.sort(city.vertices.get(city_name).neighbours_reverse);
            Collections.reverse(city.vertices.get(city_name).neighbours_reverse);
        }
    }
    public static String __bfs(Graph city, String start, String dest){
        double distance = 0;
        String file = "";
        file += "Breadth-First Search Results:  \n";
        List <Vertex> path_bfs = bfs(city.vertices.get(start),city.vertices.get(dest),city);
        for(Vertex p : path_bfs){
            file += p.vertex;
            file += '\n';
        }
        file += "That took "+ (path_bfs.size() - 1)+" hops to find. \n";
        for(int index = 1 ; index < path_bfs.size();  index+=1){
            float lat1 = path_bfs.get(index - 1).lat;
            float lon1 = path_bfs.get(index - 1).lon;
            float lat2 = path_bfs.get(index).lat;
            float lon2 = path_bfs.get(index).lon;

            distance += Math.sqrt( (lat1-lat2)*(lat1-lat2) + (lon1-lon2)*(lon1-lon2) ) * 100;
        }
        file += "Total distance = "+ Math.round(distance) + " miles.  \n\n";
        return file;

    }
    public static String __dfs(Graph city, String start, String dest){
        double distance = 0;
        String file = "";
        file += "Depth-First Search Results: \n";
        List <Vertex> path = dfs(city.vertices.get(start),city.vertices.get(dest),city);
        for(Vertex p : path){
            file += p.vertex;
            file += '\n';
        }
        file += "That took "+ (path.size() - 1)+" hops to find.\n";
        distance = 0;
        for(int index = 1 ; index < path.size();  index+=1 ){
            float lat1 = path.get(index - 1).lat;
            float lon1 = path.get(index - 1).lon;
            float lat2 = path.get(index).lat;
            float lon2 = path.get(index).lon;
            distance += Math.sqrt( (lat1-lat2)*(lat1-lat2) + (lon1-lon2)*(lon1-lon2) ) * 100;
        }

        file += "Total distance = "+ Math.round(distance) + " miles.  \n\n";
        return file;
    }

    public static String __astar(Graph city, String start, String dest){
        double distance = 0;
        String file = "";
        file += "A* Search Results: \n";
        List <Vertex> path_a = astar(city.vertices.get(start),city.vertices.get(dest),city);
        for(Vertex p : path_a){
            file += p.vertex;
            file += '\n';
        }
        distance = 0;
        file += "That took "+ (path_a.size() - 1)+" hops to find.\n";
        for(int index = 1 ; index < path_a.size();  index+=1 ){
            float lat1 = path_a.get(index - 1).lat;
            float lon1 = path_a.get(index - 1).lon;
            float lat2 = path_a.get(index).lat;
            float lon2 = path_a.get(index).lon;
            distance += Math.sqrt( (lat1-lat2)*(lat1-lat2) + (lon1-lon2)*(lon1-lon2) ) * 100;
        }

        file += "Total distance = "+ Math.round(distance)  + " miles.";
        return file;
    }

    public static String[] error_handling(String[] args) throws IOException {
        String start, dest;
        String count = "0";
        if (args.length < 2) {
            System.err.println("Usage: java Search inputFile outputFile");
            System.exit(0);
        }
        if (args[0].charAt(0) == '-' && args[1].charAt(0) == '-'){
            Scanner scanner = new Scanner(System.in);
            start = scanner.next();
            dest = scanner.next();
        }

        else if (args[1].charAt(0) == '-'){
            String input = args[0];
            BufferedReader read = new BufferedReader(new FileReader(input));
            start = read.readLine();
            dest = read.readLine();
            count = "1";
        }
        else if (args[0].charAt(0) == '-'){
            Scanner scanner = new Scanner(System.in);
            start = scanner.next();
            dest = scanner.next();
            scanner.close();
            count = "2";
        }
        else {
            String input = args[0];
            String output = args[1];
            BufferedReader read = new BufferedReader(new FileReader(input));
            String line;
            start = read.readLine();
            dest = read.readLine();
            count = "3";
        }
        String[] cities = new String[3];
        cities[0] = start;
        cities[1] = dest;
        cities[2] = count;
        return cities;
    }

    private static void read_input(Graph city) throws IOException {
        try {
            File file = new File("city.dat");
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: city.dat");
            }
            FileReader reader = new FileReader(file);


            BufferedReader read_city = new BufferedReader(reader);
            String line;
            while ((line = read_city.readLine()) != null) {
                String[] fields = line.split("\\s+");
                city.add_vertex(fields[0], Float.parseFloat(fields[2]), Float.parseFloat(fields[3]));
            }
            read_city.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            File file = new File("edge.dat");
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: edge.dat");

            }
            FileReader reader = new FileReader(file);

            BufferedReader read_edges = new BufferedReader(reader);
            String line_2;
            while ((line_2 = read_edges.readLine()) != null) {
                String[] fields = line_2.split("\\s+");
                city.add_edge(fields[0], fields[1]);


            }
            read_edges.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String[] cities = error_handling(args);
        String start = cities[0];
        String dest = cities[1];
        String text = "\n";
        Path fileName = Path.of("output");

        Graph city = new Graph();
        read_input(city);
        if(!(city.vertices.containsKey(start) && city.vertices.containsKey(dest))){
            if(!((city.vertices.containsKey(start)) && city.vertices.containsKey(dest))){
                System.err.println("No such city: ("+start+","+ dest +")");
                System.exit(0);
            }
            else if (!(city.vertices.containsKey(start))){
                System.err.println("No such city: ("+start+")");
                System.exit(0);

            }
            else{
                System.err.println("No such city: ("+dest+")");
                System.exit(0);
            }
        }
        sort(city);
        text += __bfs(city,start,dest);
        text += "\n";
        text += __dfs(city,start,dest);
        text += "\n";
        text += __astar(city,start,dest);
        if (cities[2].equals("0") || cities[2].equals("1")){
            System.out.println(text);
        }
        else {
            Files.writeString(fileName, text);
        }



    }
}