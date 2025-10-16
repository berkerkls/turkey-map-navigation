// TurkeyCities.java
// @author Berker Kelesoglu
// 81 Turkish province centroids (approximate) + a simplified Turkey border polygon
// Draws a bordered “container” within the StdDraw canvas and renders the map + cities inside it.
// Works with Princeton StdDraw: https://introcs.cs.princeton.edu/java/stdlib/
// NOTE: Coordinates are approximate (suitable for cartographic visualization, not surveying).

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public final class TurkeyCities {
    // --- Geographic bounds to project lon/lat into [0,1] (before container inset) --s

    private static String departureCity = "";
    private static String destinationCity = "";
    private static String activeField = ""; // "departure" or "destination"
    private static String errorMessage = "";


    public static void main(String[] args) {
        HashMap<String, double[]> cityPositions = renderMapWithCities();
        drawRoads(cityPositions);
        drawInputUI();
        StdDraw.show();
        handleInput(cityPositions);
    }

    public static void drawInputUI() {
        drawInputUI(null, 0.0);
    }

    public static void drawInputUI(ArrayList<String> path, double totalDistance) {
        // Draw input boxes and button below the map
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.filledRectangle(1188.5, 65, 1188.5, 65); // Clear bottom area

        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 12));

        // Labels
        StdDraw.text(150, 80, "Departure:");
        StdDraw.text(150, 40, "Destination:");

        // Input boxes
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.filledRectangle(450, 80, 150, 15);
        StdDraw.filledRectangle(450, 40, 150, 15);

        // Input box borders (highlight active field)
        if (activeField.equals("departure")) {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius(0.003);
        } else {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.001);
        }
        StdDraw.rectangle(450, 80, 150, 15);

        if (activeField.equals("destination")) {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius(0.003);
        } else {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.001);
        }
        StdDraw.rectangle(450, 40, 150, 15);

        // Display input text
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 11));
        StdDraw.text(450, 80, departureCity);
        StdDraw.text(450, 40, destinationCity);

        // Start button
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.filledRectangle(700, 60, 60, 25);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 14));
        StdDraw.text(700, 60, "Start");

        // Display path information on the right side
        if (path != null && path.size() > 0) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new Font("Arial", Font.BOLD, 11));

            // Starting city
            StdDraw.textLeft(900, 95, "Enter starting city: " + path.get(0));

            // Destination city
            StdDraw.textLeft(900, 75, "Enter destination city: " + path.get(path.size() - 1));

            // Total distance
            StdDraw.textLeft(900, 55, "Total Distance: " + String.format("%.2f", totalDistance) + " km");

            // Build and display shortest path on one line
            String pathString = String.join(" -> ", path);
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new Font("Arial", Font.BOLD, path.size() > 12 ? 8 : 11));
            StdDraw.textLeft(900, 35, "Shortest Path: " + pathString);
        }

        // Error message
        if (!errorMessage.isEmpty()) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 11));
            StdDraw.text(450, 15, errorMessage);
        }
    }

    public static boolean validateCity(String cityName, HashMap<String, double[]> cityPositions) {
        return cityPositions.containsKey(cityName);
    }

    public static void handleInput(HashMap<String, double[]> cityPositions) {
        boolean inputActive = true;

        while (inputActive) {
            // Handle mouse clicks
            if (StdDraw.isMousePressed()) {
                double x = StdDraw.mouseX();
                double y = StdDraw.mouseY();

                // Check if departure input box clicked
                if (x >= 300 && x <= 600 && y >= 65 && y <= 95) {
                    activeField = "departure";
                    errorMessage = ""; // Clear error when clicking input
                    drawInputUI();
                    StdDraw.show();
                    StdDraw.pause(200); // giving timeout to get rid of from flash effect
                }
                // Check if destination input box clicked
                else if (x >= 300 && x <= 600 && y >= 25 && y <= 55) {
                    activeField = "destination";
                    errorMessage = ""; // Clear error when clicking input
                    drawInputUI();
                    StdDraw.show();
                    StdDraw.pause(200); // giving timeout to get rid of from flash effect
                }
                // Check if Start button clicked
                else if (x >= 640 && x <= 760 && y >= 35 && y <= 85) {
                    errorMessage = ""; // Clear previous error
                    if (!departureCity.isEmpty() && !destinationCity.isEmpty()) {
                        if (validateCity(departureCity, cityPositions) && validateCity(destinationCity, cityPositions)) {
                            System.out.println("Finding path from " + departureCity + " to " + destinationCity);

                            // Build graph structure
                            HashMap<String, HashMap<String, Double>> graph = getRoadConnections(cityPositions);

                            // Find shortest path using Dijkstra's algorithm
                            ArrayList<String> path = findShortestPath(departureCity, destinationCity, graph);

                            // Display results
                            if (path != null) {
                                double totalDist = getTotalDistance(path, graph);

                                System.out.println("Shortest path found:");
                                System.out.println("Path: " + String.join(" → ", path));
                                System.out.println("Total distance: " + String.format("%.2f", totalDist) + " units");

                                // Redraw the map to clear any previous path (without reinitializing canvas)
                                redrawMapOnly(cityPositions);

                                // Draw the shortest path on the map
                                drawShortestPath(path, cityPositions);

                                // Draw UI with path information
                                drawInputUI(path, totalDist);
                                StdDraw.show();
                            } else {
                                errorMessage = "No path found between these cities!";
                                drawInputUI();
                                StdDraw.show();
                            }
                        } else {
                            errorMessage = "Invalid city names! Please check your input.";
                            drawInputUI();
                            StdDraw.show();
                        }
                    } else {
                        errorMessage = "Please enter both departure and destination cities!";
                        drawInputUI();
                        StdDraw.show();
                    }

                    StdDraw.pause(200);
                }
            }

            // Handle keyboard input
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                errorMessage = ""; // Clear error message when user types

                if (activeField.equals("departure")) {
                    if (key == '\b' && !departureCity.isEmpty()) {
                        departureCity = departureCity.substring(0, departureCity.length() - 1);
                    } else if (key != '\b' && key != '\n') {
                        departureCity += key;
                    }
                } else if (activeField.equals("destination")) {
                    if (key == '\b' && !destinationCity.isEmpty()) {
                        destinationCity = destinationCity.substring(0, destinationCity.length() - 1);
                    } else if (key != '\b' && key != '\n') {
                        destinationCity += key;
                    }
                }

                // Only redraw the input UI, not the entire canvas
                drawInputUI();
                StdDraw.show(20);
            }

            StdDraw.pause(20);
        }
    }

    public static HashMap<String, double[]> renderMapWithCities() {
        // Render the map with space below for input UI
        // setting canvas size and adding map.png according to width and height of canvas
        StdDraw.setCanvasSize(2377 / 2, (1055 + 130) / 2);
        StdDraw.setXscale(0, 2377);
        StdDraw.setYscale(0, 1055 + 130);
        StdDraw.picture(2380 / 2.0, (1055 + 130) / 2.0 + 65, "map.png", 2377, 1055);

        // HashMap to store city positions as key and value(x,y)
        HashMap<String, double[]> cityPositions = new HashMap<>();

        // Read and store city coordinates
        try {
            File file = new File("src/datas/city-coordinates.txt"); // get the file to scan cities line by line
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {                             // check if we are in the last line
                String line = scanner.nextLine();                       // get the line data
                String[] parts = line.split(", ");               // divides the line as cityName and coordinates

                if (parts.length == 3) {                                // if divided correctly we move on
                    String cityName = parts[0];                         // get the cityName
                    double x = Double.parseDouble(parts[1]);            // get long data
                    double y = Double.parseDouble(parts[2]);            // get lat data

                    // Store city position
                    cityPositions.put(cityName, new double[]{x, y});  // put each city inside hashmap as <key,value> pair
                }
            }
            scanner.close();                                          // since we converted text file to hash map we are done
        } catch (FileNotFoundException e) {
            System.err.println("Error: city-coordinates.txt file not found!");
            e.printStackTrace();
        }

        // Draw cities (shifted up by 130 pixels for input area)
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.005);

        for (String cityName : cityPositions.keySet()) {                    // get key name of each city and loop through
            double[] pos = cityPositions.get(cityName);                     // get coordinate values of the city
            double x = pos[0];                                              // get long data
            double y = pos[1] + 130; // Shift up for input area             // get lat data

            // Draw city point
            StdDraw.filledCircle(x, y, 5);                           // according to position put filled circle on the map

            // Draw city name
            StdDraw.setPenColor(StdDraw.BLACK);                             // city name color
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 8));  // set font
            StdDraw.text(x, y - 15, cityName);                          // write city name under the dot
            StdDraw.setPenColor(StdDraw.RED);
        }

        return cityPositions;
    }

    public static void drawRoads(HashMap<String, double[]> cityPositions) {
        // cityPosition arguments has been filled in renderMapWithCities() as <cityName,[x,y]positions>
         try {
            File file = new File("src/datas/cities-roads.txt");         // get roads
            Scanner scanner = new Scanner(file);

            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius(0.002);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cities = line.split(" - ");                // divide by text to find start and end point of road

                if (cities.length == 2) {
                    String city1Name = cities[0];                           // start point
                    String city2Name = cities[1];                           // end point

                    // Get coordinates for both cities
                    double[] city1Pos = cityPositions.get(city1Name);           // get start city position
                    double[] city2Pos = cityPositions.get(city2Name);           // get end city position

                    // Draw line if both cities exist (shifted up by 130 pixels)
                    if (city1Pos != null && city2Pos != null) {
                        StdDraw.line(city1Pos[0], city1Pos[1] + 130, city2Pos[0], city2Pos[1] + 130); // draw lines between cities we add 130 because we gave some gap for input
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: city-roads.txt file not found!");
            e.printStackTrace();
        }

        StdDraw.show();
    }

    /**
     * Redraw the entire map (background, cities, and roads) without reinitializing canvas
     * This is used to clear any previous paths before drawing a new one
     * @param cityPositions HashMap containing city positions
     */
    public static void redrawMapOnly(HashMap<String, double[]> cityPositions) {
        // Redraw the background map image
        StdDraw.picture(2380 / 2.0, (1055 + 130) / 2.0 + 65, "map.png", 2377, 1055);

        // Redraw cities (shifted up by 130 pixels for input area)
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.005);

        for (String cityName : cityPositions.keySet()) {
            double[] pos = cityPositions.get(cityName);
            double x = pos[0];
            double y = pos[1] + 130; // Shift up for input area

            // Draw city point
            StdDraw.filledCircle(x, y, 5);

            // Draw city name
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 8));
            StdDraw.text(x, y - 15, cityName);
            StdDraw.setPenColor(StdDraw.RED);
        }

        // Redraw all roads
        try {
            File file = new File("src/datas/cities-roads.txt");
            Scanner scanner = new Scanner(file);

            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius(0.002);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cities = line.split(" - ");

                if (cities.length == 2) {
                    String city1Name = cities[0];
                    String city2Name = cities[1];

                    double[] city1Pos = cityPositions.get(city1Name);
                    double[] city2Pos = cityPositions.get(city2Name);

                    if (city1Pos != null && city2Pos != null) {
                        StdDraw.line(city1Pos[0], city1Pos[1] + 130, city2Pos[0], city2Pos[1] + 130);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: city-roads.txt file not found!");
        }
    }

    // ==================== DIJKSTRA ALGORITHM FUNCTIONS ====================

    /**
     * Calculate Euclidean distance between two points
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Distance between the two points
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Build graph structure with road connections and distances
     * @param cityPositions HashMap containing city positions
     * @return Graph structure: city -> (neighbor city -> distance)
     */
    public static HashMap<String, HashMap<String, Double>> getRoadConnections(
            HashMap<String, double[]> cityPositions) {

        // Main structure: city -> (neighbor city -> distance)
        HashMap<String, HashMap<String, Double>> graph = new HashMap<>();

        // Initialize empty neighbor list for all cities
        for (String city : cityPositions.keySet()) {
            graph.put(city, new HashMap<>());
        }

        try {
            File file = new File("src/datas/cities-roads.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cities = line.split(" - ");

                if (cities.length == 2) {
                    String city1 = cities[0];
                    String city2 = cities[1];

                    double[] pos1 = cityPositions.get(city1);
                    double[] pos2 = cityPositions.get(city2);

                    if (pos1 != null && pos2 != null) {
                        // Calculate distance between cities
                        double distance = calculateDistance(pos1[0], pos1[1], pos2[0], pos2[1]);

                        // Add bidirectional road (A->B and B->A)
                        graph.get(city1).put(city2, distance);
                        graph.get(city2).put(city1, distance);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: cities-roads.txt not found!");
        }

        return graph;
    }

    /**
     * Find shortest path between two cities using Dijkstra's algorithm
     * @param startCity Starting city name
     * @param endCity Destination city name
     * @param graph Graph structure with road connections
     * @return List of cities in shortest path, or null if no path exists
     */
    public static ArrayList<String> findShortestPath(
            String startCity,
            String endCity,
            HashMap<String, HashMap<String, Double>> graph) {

        // Step 1: Initialize distances and tracking structures
        HashMap<String, Double> distances = new HashMap<>();
        HashMap<String, String> previousCity = new HashMap<>();
        HashMap<String, Boolean> visited = new HashMap<>();

        // Set initial values for all cities
        for (String city : graph.keySet()) {
            distances.put(city, Double.POSITIVE_INFINITY);
            visited.put(city, false);
        }
        distances.put(startCity, 0.0);

        // Step 2: Main loop - process each city
        for (int i = 0; i < graph.size(); i++) {
            // Step 2a: Find unvisited city with minimum distance
            String currentCity = null;
            double minDistance = Double.POSITIVE_INFINITY;

            for (String city : graph.keySet()) {
                if (!visited.get(city) && distances.get(city) < minDistance) {
                    minDistance = distances.get(city);
                    currentCity = city;
                }
            }

            // No more reachable cities
            if (currentCity == null) break;

            // Reached destination, exit early
            if (currentCity.equals(endCity)) break;

            // Step 2b: Mark current city as visited
            visited.put(currentCity, true);

            // Step 2c: Check all neighbors and update their distances
            HashMap<String, Double> neighbors = graph.get(currentCity);

            for (String neighbor : neighbors.keySet()) {
                if (!visited.get(neighbor)) {
                    double roadDistance = neighbors.get(neighbor);
                    double newDistance = distances.get(currentCity) + roadDistance;

                    // Found a shorter path?
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        previousCity.put(neighbor, currentCity);
                    }
                }
            }
        }

        // Step 3: Reconstruct path by following previous cities backwards
        ArrayList<String> path = new ArrayList<>();
        String current = endCity;

        // No path exists if we never reached the end city
        if (!previousCity.containsKey(endCity) && !startCity.equals(endCity)) {
            return null;
        }

        // Build path backwards from end to start
        while (current != null) {
            path.add(0, current); // Add to beginning
            current = previousCity.get(current);
        }

        return path;
    }

    /**
     * Calculate total distance of a path
     * @param path List of cities in the path
     * @param graph Graph structure with distances
     * @return Total distance of the path
     */
    public static double getTotalDistance(
            ArrayList<String> path,
            HashMap<String, HashMap<String, Double>> graph) {

        if (path == null || path.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;

        // Sum distances between each consecutive city pair
        for (int i = 0; i < path.size() - 1; i++) {
            String city1 = path.get(i);
            String city2 = path.get(i + 1);

            Double distance = graph.get(city1).get(city2);
            if (distance != null) {
                totalDistance += distance;
            }
        }

        return totalDistance;
    }

    /**
     * Draw the shortest path on the map with animation
     * Animation includes:
     * 1. Immediate green path drawing (all at once)
     * 2. Moving dot that travels along the path
     * @param path List of cities in the shortest path
     * @param cityPositions HashMap containing city positions
     */
    public static void drawShortestPath(
            ArrayList<String> path,
            HashMap<String, double[]> cityPositions) {

        if (path == null || path.size() < 2) {
            return;
        }

        // Phase 1: Draw entire green path immediately (no animation)
        StdDraw.setPenColor(StdDraw.GREEN);
        StdDraw.setPenRadius(0.005);

        for (int i = 0; i < path.size() - 1; i++) {
            String city1 = path.get(i);
            String city2 = path.get(i + 1);

            double[] pos1 = cityPositions.get(city1);
            double[] pos2 = cityPositions.get(city2);

            if (pos1 != null && pos2 != null) {
                // Draw path segment immediately
                StdDraw.line(pos1[0], pos1[1] + 130, pos2[0], pos2[1] + 130);
            }
        }

        StdDraw.show();
        StdDraw.pause(300);

        // Phase 2: Moving dot animation along the completed path
        animateMovingDot(path, cityPositions);
    }

    /**
     * Highlight a city with a specific color (creates a pulsing effect)
     * @param x X coordinate of city
     * @param y Y coordinate of city (already shifted)
     * @param color Color to highlight with
     */
    private static void highlightCity(double x, double y, java.awt.Color color) {
        // Draw larger circle for highlight effect
        StdDraw.setPenColor(color);
        StdDraw.filledCircle(x, y, 8);

        // Draw original city point on top
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.filledCircle(x, y, 5);
    }

    /**
     * Restore city to its normal red appearance
     * @param x X coordinate of city
     * @param y Y coordinate of city (already shifted)
     * @param cityName Name of the city to display
     */
    private static void restoreCity(double x, double y, String cityName) {
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.filledCircle(x, y, 5);

        // Redraw city name
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 8));
        StdDraw.text(x, y - 15, cityName);
    }

    /**
     * Animate a moving dot along the path
     * @param path List of cities in the path
     * @param cityPositions HashMap containing city positions
     */
    private static void animateMovingDot(ArrayList<String> path,
                                         HashMap<String, double[]> cityPositions) {
        if (path == null || path.size() < 2) {
            return;
        }

        int steps = 30; // Number of interpolation steps between each city pair

        for (int i = 0; i < path.size() - 1; i++) {
            String city1 = path.get(i);
            String city2 = path.get(i + 1);

            double[] pos1 = cityPositions.get(city1);
            double[] pos2 = cityPositions.get(city2);

            if (pos1 != null && pos2 != null) {
                double x1 = pos1[0];
                double y1 = pos1[1] + 130;
                double x2 = pos2[0];
                double y2 = pos2[1] + 130;

                // Interpolate between two cities
                for (int step = 0; step <= steps; step++) {
                    double t = (double) step / steps; // Progress from 0 to 1
                    double currentX = x1 + t * (x2 - x1);
                    double currentY = y1 + t * (y2 - y1);

                    // Redraw the map segment (just the area around the dot to avoid flickering)
                    // Draw the moving dot
                    StdDraw.setPenColor(StdDraw.BLUE);
                    StdDraw.filledCircle(currentX, currentY, 7);

                    // Draw a smaller white core for better visibility
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.filledCircle(currentX, currentY, 4);

                    StdDraw.show();
                    StdDraw.pause(5);

                    // Erase the dot by redrawing the path
                    StdDraw.setPenColor(StdDraw.GREEN);
                    StdDraw.setPenRadius(0.005);
                    StdDraw.line(x1, y1, x2, y2);
                }
            }
        }

        // Final highlight at destination
        String endCity = path.get(path.size() - 1);
        double[] endPos = cityPositions.get(endCity);
        if (endPos != null) {
            highlightCity(endPos[0], endPos[1] + 130, StdDraw.BOOK_LIGHT_BLUE);
            StdDraw.show();
            StdDraw.pause(200);
            restoreCity(endPos[0], endPos[1] + 130, endCity);
            StdDraw.show();
        }
    }
}
