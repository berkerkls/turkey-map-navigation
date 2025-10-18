// @author Berker Kelesoglu
// Student ID: 2025719045
// Date: 17 October 2025
// 81 Turkish province centroids (approximate) + a simplified Turkey border polygon
// Draws a bordered “container” within the StdDraw canvas and renders the map + cities inside it.
// Works with Princeton StdDraw: https://introcs.cs.princeton.edu/java/stdlib/
// NOTE: Coordinates are approximate (suitable for cartographic visualization, not surveying).

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public final class berker_kelesoglu {
    private static String departureCity = "";
    private static String destinationCity = "";
    private static String activeField = ""; // "departure" or "destination"
    private static String errorMessage = "";

    // Arrays to store city data
    private static String[] cityNames;
    private static double[] xCoordinates;
    private static double[] yCoordinates;


    public static void main(String[] args) {
        // Load city data into static arrays
        renderMapWithCities();
        drawRoads(cityNames, xCoordinates, yCoordinates);
        drawInputUI(null, 0.0);
        StdDraw.show();
        handleInput(cityNames, xCoordinates, yCoordinates);
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
            StdDraw.textLeft(900, 95, "Starting city: " + path.get(0));

            // Destination city
            StdDraw.textLeft(900, 75, "Destination city: " + path.get(path.size() - 1));

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
    /**
     * Validate if a city name exists in the city names array
     * @param cityName Name of the city to validate
     * @param cityNames Array of all city names
     * @return true if city exists, false otherwise
     */
    public static boolean validateCity(String cityName, String[] cityNames) {
        return getCityIndex(cityName, cityNames) != -1;
    }

    public static void handleInput(String[] cityNames, double[] xCoordinates, double[] yCoordinates) {
        boolean inputActive = true;
        while (inputActive) {
            // Handle mouse clicks
            if (StdDraw.isMousePressed()) {
                double x = StdDraw.mouseX();
                double y = StdDraw.mouseY();

                // Check if departure input box clicked
                if (x >= 300 && x <= 600 && y >= 65 && y <= 95) {
                    activeField = "departure";  // keep last clicked input to assign its value.
                    errorMessage = ""; // Clear error when clicking input
                    drawInputUI(null, 0.0);
                    StdDraw.show();
                    StdDraw.pause(200); // giving timeout to get rid of from flash effect
                }
                // Check if destination input box clicked
                else if (x >= 300 && x <= 600 && y >= 25 && y <= 55) {
                    activeField = "destination"; // keep last clicked input to assign its value
                    errorMessage = ""; // Clear error when clicking input
                    drawInputUI(null, 0.0);
                    StdDraw.show();
                    StdDraw.pause(200); // giving timeout to get rid of from flash effect
                }
                // Check if Start button clicked
                else if (x >= 640 && x <= 760 && y >= 35 && y <= 85) {
                    errorMessage = ""; // Clear previous error
                    if (!departureCity.isEmpty() && !destinationCity.isEmpty()) { // if departure and destination names are not empty
                        if (validateCity(departureCity, cityNames) && validateCity(destinationCity, cityNames)) { // if the city names are included in cities
                            System.out.println("Finding path from " + departureCity + " to " + destinationCity);

                            // Build graph structure (using adjacency matrix - 2D array)
                            double[][] graph = getRoadConnections(cityNames, xCoordinates, yCoordinates);  // sets distances between two cities

                            // Find shortest path using Dijkstra's algorithm (array-based)
                            ArrayList<String> path = findShortestPath(departureCity, destinationCity, graph, cityNames);

                            // Display results
                            if (path != null) {
                                double totalDist = getTotalDistance(path, graph, cityNames);

                                System.out.println("Shortest path found:");
                                System.out.println("Path: " + String.join(" → ", path));
                                System.out.println("Total distance: " + String.format("%.2f", totalDist) + " units");

                                // Redraw the map to clear any previous path (without reinitializing canvas)
                                redrawMapOnly(cityNames, xCoordinates, yCoordinates);

                                // Draw the shortest path on the map
                                drawShortestPath(path, cityNames, xCoordinates, yCoordinates);

                                // Draw UI with path information
                                drawInputUI(path, totalDist);
                                StdDraw.show();
                            } else {
                                errorMessage = "No path found between these cities!";
                                drawInputUI(null, 0.0);
                                StdDraw.show();
                            }
                        } else {
                            errorMessage = "Invalid city names! Please check your input.";
                            drawInputUI(null, 0.0);
                            StdDraw.show();
                        }
                    } else {
                        errorMessage = "Please enter both departure and destination cities!";
                        drawInputUI(null, 0.0);
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
                    if (key == '\b' && !departureCity.isEmpty()) {  // if backspace is pressed and there is a value
                        departureCity = departureCity.substring(0, departureCity.length() - 1); // delete the last value of string
                    } else if (key != '\b' && key != '\n') { // if backspace or enter are not pressed
                        departureCity += key;                //then append the pressed key to value
                    }
                } else if (activeField.equals("destination")) {
                    if (key == '\b' && !destinationCity.isEmpty()) { // if backspace is pressed and there is a value
                        destinationCity = destinationCity.substring(0, destinationCity.length() - 1); // delete the last value of string
                    } else if (key != '\b' && key != '\n') {    // if backspace or enter are not pressed
                        destinationCity += key;                  //then append the pressed key to value
                    }
                }

                // Only redraw the input UI, not the entire canvas
                drawInputUI(null, 0.0);
                StdDraw.show(20);
            }

            StdDraw.pause(20); // pause effect is used to prevent flash effect
        }
    }

    /**
     * Render map with cities and load city data into static arrays
     */
    public static void renderMapWithCities() {
        // Render the map with space below for input UI
        // setting canvas size and adding map.png according to width and height of canvas
        StdDraw.setCanvasSize(2377 / 2, (1055 + 130) / 2);
        StdDraw.setXscale(0, 2377);
        StdDraw.setYscale(0, 1055 + 130);
        StdDraw.picture(2380 / 2.0, (1055 + 130) / 2.0 + 65, "map.png", 2377, 1055);

        // Temporary ArrayLists to store city data
        ArrayList<String> cityNamesList = new ArrayList<>();
        ArrayList<Double> xCoordsList = new ArrayList<>();
        ArrayList<Double> yCoordsList = new ArrayList<>();

        // Read and store city coordinates
        try {
            File file = new File("src/datas/city-coordinates.txt"); // get the file to scan cities line by line
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {                             // check if we are in the last line
                String line = scanner.nextLine();                       // get the line data
                String[] parts = line.split(", ");               // divides the line as cityName and coordinates

                if (parts.length == 3) {                                // if divided correctly we move on
                    String cityNameValue = parts[0];                         // get the cityName
                    double x = Double.parseDouble(parts[1]);            // get long data
                    double y = Double.parseDouble(parts[2]);            // get lat data

                    // Store city data in ArrayLists
                    cityNamesList.add(cityNameValue);
                    xCoordsList.add(x);
                    yCoordsList.add(y);
                }
            }
            scanner.close();                                          // since we converted text file to arrays we are done
        } catch (FileNotFoundException e) {
            System.err.println("Error: city-coordinates.txt file not found!");
            e.printStackTrace();
        }

        // Convert ArrayLists to static arrays
        cityNames = new String[cityNamesList.size()];
        xCoordinates = new double[xCoordsList.size()];
        yCoordinates = new double[yCoordsList.size()];

        for (int i = 0; i < cityNamesList.size(); i++) {
            cityNames[i] = cityNamesList.get(i);
            xCoordinates[i] = xCoordsList.get(i);
            yCoordinates[i] = yCoordsList.get(i);
        }

        // Draw cities (shifted up by 130 pixels for input area)
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.005);

        for (int i = 0; i < cityNames.length; i++) {
            double x = xCoordinates[i];
            double y = yCoordinates[i] + 130; // Shift up for input area

            // Draw city point
            StdDraw.filledCircle(x, y, 5);                           // according to position put filled circle on the map

            // Draw city name
            StdDraw.setPenColor(StdDraw.BLACK);                             // city name color
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 8));  // set font
            StdDraw.text(x, y - 15, cityNames[i]);                          // write city name under the dot
            StdDraw.setPenColor(StdDraw.RED);
        }
    }

    public static void drawRoads(String[] cityNames, double[] xCoordinates, double[] yCoordinates) {
        // Draw roads between connected cities
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
                    int index1 = getCityIndex(city1Name, cityNames);
                    int index2 = getCityIndex(city2Name, cityNames);

                    // Draw line if both cities exist (shifted up by 130 pixels)
                    if (index1 != -1 && index2 != -1) {
                        StdDraw.line(xCoordinates[index1], yCoordinates[index1] + 130,
                                     xCoordinates[index2], yCoordinates[index2] + 130); // draw lines between cities we add 130 because we gave some gap for input
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
     * @param cityNames Array of city names
     * @param xCoordinates Array of x coordinates
     * @param yCoordinates Array of y coordinates
     */
    public static void redrawMapOnly(String[] cityNames, double[] xCoordinates, double[] yCoordinates) {
        // Redraw the background map image
        StdDraw.picture(2380 / 2.0, (1055 + 130) / 2.0 + 65, "map.png", 2377, 1055);

        // Redraw cities (shifted up by 130 pixels for input area)
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.005);

        for (int i = 0; i < cityNames.length; i++) {
            double x = xCoordinates[i];
            double y = yCoordinates[i] + 130; // Shift up for input area

            // Draw city point
            StdDraw.filledCircle(x, y, 5);

            // Draw city name
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 8));
            StdDraw.text(x, y - 15, cityNames[i]);
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

                    int index1 = getCityIndex(city1Name, cityNames);
                    int index2 = getCityIndex(city2Name, cityNames);

                    if (index1 != -1 && index2 != -1) {
                        StdDraw.line(xCoordinates[index1], yCoordinates[index1] + 130,
                                     xCoordinates[index2], yCoordinates[index2] + 130);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: city-roads.txt file not found!");
        }
    }


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
     * Get city index from city name
     * @param cityName Name of the city to find
     * @param cityNames Array of city names
     * @return Index of the city, or -1 if not found
     */
    public static int getCityIndex(String cityName, String[] cityNames) {
        for (int i = 0; i < cityNames.length; i++) {
            if (cityNames[i].equals(cityName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Build graph structure with road connections and distances using adjacency matrix
     * @param cityNames Array of city names
     * @param xCoordinates Array of x coordinates
     * @param yCoordinates Array of y coordinates
     * @return 2D array representing adjacency matrix where graph[i][j] is distance from city i to city j
     *         Returns 0.0 if no direct road exists between cities
     * Example return value:
     *   graph[0][1] = 150.5 means distance from city at index 0 to city at index 1 is 150.5
     *   graph[0][5] = 0.0 means no direct road between city 0 and city 5
     */
    public static double[][] getRoadConnections(String[] cityNames, double[] xCoordinates, double[] yCoordinates) {
        int numCities = cityNames.length;   // keep number of cities

        // Create adjacency matrix (2D array)
        // graph[i][j] represents distance from city i to city j
        // 0.0 means no direct connection
        double[][] graph = new double[numCities][numCities];  // store departure and destination cities separate arrays in an array

        // Initialize all distances to 0 (no connection)
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                graph[i][j] = 0.0;
            }
        }

        try {
            File file = new File("src/datas/cities-roads.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cities = line.split(" - ");    // divide cities

                if (cities.length == 2) {
                    String city1 = cities[0];
                    String city2 = cities[1];

                    // Get indices for both cities
                    int index1 = getCityIndex(city1, cityNames);
                    int index2 = getCityIndex(city2, cityNames);

                    if (index1 != -1 && index2 != -1) {
                        // Calculate distance between cities
                        double distance = calculateDistance(xCoordinates[index1], yCoordinates[index1], xCoordinates[index2], yCoordinates[index2]);
                        // Set bidirectional road (A->B and B->A)
                        graph[index1][index2] = distance;
                        graph[index2][index1] = distance;
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
     * Find shortest path between two cities using Dijkstra's algorithm (array-based implementation)
     * @param startCity Starting city name
     * @param endCity Destination city name
     * @param graph 2D array adjacency matrix with road distances
     * @param cityNames Array mapping indices to city names
     * @return List of cities in shortest path, or null if no path exists
     */
    public static ArrayList<String> findShortestPath(
            String startCity,
            String endCity,
            double[][] graph,
            String[] cityNames) {

        int numCities = cityNames.length;

        // Get indices for start and end cities
        int startIndex = getCityIndex(startCity, cityNames);
        int endIndex = getCityIndex(endCity, cityNames);

        if (startIndex == -1 || endIndex == -1) {
            return null; // City not found
        }

        // Step 1: Initialize distances and tracking arrays
        double[] distances = new double[numCities];
        int[] previousCity = new int[numCities]; // Store index of previous city
        boolean[] visited = new boolean[numCities];

        // Set initial values for all cities
        for (int i = 0; i < numCities; i++) {
            distances[i] = Double.POSITIVE_INFINITY;
            previousCity[i] = -1; // -1 means no previous city
            visited[i] = false;
        }
        distances[startIndex] = 0.0;

        // Step 2: Main loop - process each city
        for (int i = 0; i < numCities; i++) {
            // Step 2a: Find unvisited city with minimum distance
            int currentCityIndex = -1;
            double minDistance = Double.POSITIVE_INFINITY;

            for (int j = 0; j < numCities; j++) {
                if (!visited[j] && distances[j] < minDistance) {
                    minDistance = distances[j];
                    currentCityIndex = j;
                }
            }

            // No more reachable cities
            if (currentCityIndex == -1) break;

            // Reached destination, exit early
            if (currentCityIndex == endIndex) break;

            // Step 2b: Mark current city as visited
            visited[currentCityIndex] = true;

            // Step 2c: Check all neighbors and update their distances
            for (int neighborIndex = 0; neighborIndex < numCities; neighborIndex++) {
                // If there's a road (distance > 0) and neighbor not visited
                if (graph[currentCityIndex][neighborIndex] > 0 && !visited[neighborIndex]) {
                    double roadDistance = graph[currentCityIndex][neighborIndex];
                    double newDistance = distances[currentCityIndex] + roadDistance;

                    // Found a shorter path?
                    if (newDistance < distances[neighborIndex]) {
                        distances[neighborIndex] = newDistance;
                        previousCity[neighborIndex] = currentCityIndex;
                    }
                }
            }
        }

        // Step 3: Reconstruct path by following previous cities backwards
        ArrayList<String> path = new ArrayList<>();

        // No path exists if we never reached the end city
        if (previousCity[endIndex] == -1 && startIndex != endIndex) {
            return null;
        }

        // Build path backwards from end to start
        int currentIndex = endIndex;
        while (currentIndex != -1) {
            path.add(0, cityNames[currentIndex]); // Add city name to beginning
            currentIndex = previousCity[currentIndex];
        }

        return path;
    }

    /**
     * Calculate total distance of a path (array-based implementation)
     * @param path List of cities in the path
     * @param graph 2D array adjacency matrix with road distances
     * @param cityNames Array mapping indices to city names
     * @return Total distance of the path
     */
    public static double getTotalDistance(
            ArrayList<String> path,
            double[][] graph,
            String[] cityNames) {

        if (path == null || path.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;

        // Sum distances between each consecutive city pair
        for (int i = 0; i < path.size() - 1; i++) {
            String city1 = path.get(i);
            String city2 = path.get(i + 1);

            // Get indices for both cities
            int index1 = getCityIndex(city1, cityNames);
            int index2 = getCityIndex(city2, cityNames);

            if (index1 != -1 && index2 != -1) {
                double distance = graph[index1][index2];
                if (distance > 0) {
                    totalDistance += distance;
                }
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
     * @param cityNames Array of city names
     * @param xCoordinates Array of x coordinates
     * @param yCoordinates Array of y coordinates
     */
    public static void drawShortestPath(
            ArrayList<String> path,
            String[] cityNames,
            double[] xCoordinates,
            double[] yCoordinates) {

        if (path == null || path.size() < 2) {
            return;
        }

        // Phase 1: Draw entire green path immediately (no animation)
        StdDraw.setPenColor(StdDraw.GREEN);
        StdDraw.setPenRadius(0.005);

        for (int i = 0; i < path.size() - 1; i++) {
            String city1 = path.get(i);
            String city2 = path.get(i + 1);

            int index1 = getCityIndex(city1, cityNames);
            int index2 = getCityIndex(city2, cityNames);

            if (index1 != -1 && index2 != -1) {
                // Draw path segment immediately
                StdDraw.line(xCoordinates[index1], yCoordinates[index1] + 130,
                           xCoordinates[index2], yCoordinates[index2] + 130);
            }
        }

        StdDraw.show();
        StdDraw.pause(300);

        // Phase 2: Moving dot animation along the completed path
        animateMovingDot(path, cityNames, xCoordinates, yCoordinates);
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
     * @param cityNames Array of city names
     * @param xCoordinates Array of x coordinates
     * @param yCoordinates Array of y coordinates
     */
    private static void animateMovingDot(ArrayList<String> path, String[] cityNames, double[] xCoordinates, double[] yCoordinates) {
        if (path == null || path.size() < 2) {
            return;
        }

        int steps = 30; // Number of interpolation steps between each city pair

        for (int i = 0; i < path.size() - 1; i++) {
            String city1 = path.get(i);
            String city2 = path.get(i + 1);

            int index1 = getCityIndex(city1, cityNames);
            int index2 = getCityIndex(city2, cityNames);

            if (index1 != -1 && index2 != -1) {
                double x1 = xCoordinates[index1];
                double y1 = yCoordinates[index1] + 130;
                double x2 = xCoordinates[index2];
                double y2 = yCoordinates[index2] + 130;

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
        int endIndex = getCityIndex(endCity, cityNames);
        if (endIndex != -1) {
            highlightCity(xCoordinates[endIndex], yCoordinates[endIndex] + 130, StdDraw.BOOK_LIGHT_BLUE);
            StdDraw.show();
            StdDraw.pause(200);
            restoreCity(xCoordinates[endIndex], yCoordinates[endIndex] + 130, endCity);
            StdDraw.show();
        }
    }
}
