// TurkeyCities.java
// 81 Turkish province centroids (approximate) + a simplified Turkey border polygon
// Draws a bordered “container” within the StdDraw canvas and renders the map + cities inside it.
// Works with Princeton StdDraw: https://introcs.cs.princeton.edu/java/stdlib/
// NOTE: Coordinates are approximate (suitable for cartographic visualization, not surveying).

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public final class TurkeyCities {
    // --- Geographic bounds to project lon/lat into [0,1] (before container inset) --s

    private static String departureCity = "";
    private static String destinationCity = "";
    private static String activeField = ""; // "departure" or "destination"
    private static String errorMessage = "";

    public static void drawInputUI() {
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
                    StdDraw.pause(200);
                }
                // Check if destination input box clicked
                else if (x >= 300 && x <= 600 && y >= 25 && y <= 55) {
                    activeField = "destination";
                    errorMessage = ""; // Clear error when clicking input
                    drawInputUI();
                    StdDraw.show();
                    StdDraw.pause(200);
                }
                // Check if Start button clicked
                else if (x >= 640 && x <= 760 && y >= 35 && y <= 85) {
                    errorMessage = ""; // Clear previous error
                    if (!departureCity.isEmpty() && !destinationCity.isEmpty()) {
                        if (validateCity(departureCity, cityPositions) && validateCity(destinationCity, cityPositions)) {
                            System.out.println("Finding path from " + departureCity + " to " + destinationCity);
                            // TODO: Add pathfinding algorithm here
                            // For now, just keep the UI active
                            errorMessage = "Path found! (Pathfinding not implemented yet)";
                            drawInputUI();
                            StdDraw.show();
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
        StdDraw.setCanvasSize(2377 / 2, (1055 + 130) / 2);
        StdDraw.setXscale(0, 2377);
        StdDraw.setYscale(0, 1055 + 130);
        StdDraw.picture(2377 / 2.0, (1055 + 130) / 2.0 + 65, "map.png", 2377, 1055);

        // HashMap to store city positions
        HashMap<String, double[]> cityPositions = new HashMap<>();

        // Read and store city coordinates
        try {
            File file = new File("src/datas/city-coordinates.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(", ");

                if (parts.length == 3) {
                    String cityName = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    // Store city position
                    cityPositions.put(cityName, new double[]{x, y});
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: city-coordinates.txt file not found!");
            e.printStackTrace();
        }

        // Draw cities (shifted up by 130 pixels for input area)
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

        return cityPositions;
    }

    public static void drawRoads(HashMap<String, double[]> cityPositions) {
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

                    // Get coordinates for both cities
                    double[] city1Pos = cityPositions.get(city1Name);
                    double[] city2Pos = cityPositions.get(city2Name);

                    // Draw line if both cities exist (shifted up by 130 pixels)
                    if (city1Pos != null && city2Pos != null) {
                        StdDraw.line(city1Pos[0], city1Pos[1] + 130, city2Pos[0], city2Pos[1] + 130);
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

    public static void main(String[] args) {
        HashMap<String, double[]> cityPositions = renderMapWithCities();
        drawRoads(cityPositions);
        drawInputUI();
        StdDraw.show();
        handleInput(cityPositions);
    }
}
