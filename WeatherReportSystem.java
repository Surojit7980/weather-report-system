import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpConnectTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherReportSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WeatherApiClient apiClient = new WeatherApiClient("DEMO_KEY");
        WeatherManager manager = new WeatherManager(apiClient);

        System.out.println("==================================================");
        System.out.println("   OBJECT-ORIENTED WEATHER REPORT SYSTEM (CSE)    ");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-6): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleFetchWeather(scanner, manager);
                    break;
                case "2":
                    handleCacheLookup(scanner, manager);
                    break;
                case "3":
                    handleViewHistory(manager);
                    break;
                case "4":
                    manager.displaySummaryStatistics();
                    break;
                case "5":
                    handleConfigureApiKey(scanner, apiClient);
                    break;
                case "6":
                    System.out.println("Terminating Weather Report System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("[Error] Invalid selection. Please enter a number between 1 and 6.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println("1. Fetch Live Weather Data (REST API)");
        System.out.println("2. Lookup Cached Report by City (HashMap)");
        System.out.println("3. Display All Historical Logs (ArrayList)");
        System.out.println("4. Show Weather Analytics & Summary Stats");
        System.out.println("5. Configure / Update OpenWeatherMap API Key");
        System.out.println("6. Exit");
        System.out.println("---------------------------------------------");
    }

    private static void handleFetchWeather(Scanner scanner, WeatherManager manager) {
        System.out.print("Enter City Name: ");
        String city = scanner.nextLine().trim();
        if (city.isEmpty()) {
            System.out.println("[Validation Error] City name cannot be blank.");
            return;
        }

        System.out.print("Force refresh from API? (y/n, default n): ");
        String refreshChoice = scanner.nextLine().trim().toLowerCase();
        boolean forceRefresh = refreshChoice.equals("y") || refreshChoice.equals("yes");

        try {
            System.out.println("Contacting weather endpoint...");
            WeatherReport report = manager.getRealTimeWeather(city, forceRefresh);
            System.out.println("\nSUCCESS: Weather Report Retrieved");
            System.out.println(report);
        } catch (CityNotFoundException e) {
            System.out.println("[City Error] " + e.getMessage());
        } catch (NetworkException e) {
            System.out.println("[Network Error] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[Input Error] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Unexpected Error] An unexpected error occurred: " + e.getMessage());
        }
    }

    private static void handleCacheLookup(Scanner scanner, WeatherManager manager) {
        System.out.print("Enter City Name to search in cache: ");
        String city = scanner.nextLine().trim();
        if (city.isEmpty()) {
            System.out.println("[Validation Error] City name cannot be blank.");
            return;
        }

        try {
            WeatherReport cached = manager.lookupCachedReport(city);
            System.out.println("\nSUCCESS: Cache Hit (O(1) HashMap Lookup)");
            System.out.println(cached);
        } catch (CityNotFoundException e) {
            System.out.println("[Cache Miss] " + e.getMessage());
        }
    }

    private static void handleViewHistory(WeatherManager manager) {
        List<WeatherReport> history = manager.getAllHistory();
        if (history.isEmpty()) {
            System.out.println("No weather inquiries recorded yet.");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("       CHRONOLOGICAL WEATHER INQUIRY LOGS         ");
        System.out.println("==================================================");
        int index = 1;
        for (WeatherReport report : history) {
            System.out.printf("#%d | City: %-15s | Temp: %5.1f°C | Humidity: %3d%% | Wind: %4.1fm/s | Condition: %-12s | Time: %s\n",
                    index++,
                    report.getCityName(),
                    report.getTemperature(),
                    report.getHumidity(),
                    report.getWindSpeed(),
                    report.getCondition(),
                    report.getFormattedTimestamp());
        }
        System.out.println("==================================================");
    }

    private static void handleConfigureApiKey(Scanner scanner, WeatherApiClient apiClient) {
        System.out.println("Current API Key: " + (apiClient.getApiKey().equals("DEMO_KEY") ? "DEMO_KEY (Mock / Fallback active)" : "Configured"));
        System.out.print("Enter new OpenWeatherMap API Key (or type 'DEMO_KEY' for fallback mode): ");
        String newKey = scanner.nextLine().trim();
        if (!newKey.isEmpty()) {
            apiClient.setApiKey(newKey);
            System.out.println("API Key updated successfully.");
        } else {
            System.out.println("No changes made to API Key.");
        }
    }
}

class CityNotFoundException extends Exception {
    public CityNotFoundException(String message) {
        super(message);
    }
}

class NetworkException extends Exception {
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
    public NetworkException(String message) {
        super(message);
    }
}

class WeatherReport {
    private final String cityName;
    private final double temperature;
    private final int humidity;
    private final double windSpeed;
    private final String condition;
    private final LocalDateTime timestamp;

    public WeatherReport(String cityName, double temperature, int humidity, double windSpeed, String condition) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.timestamp = LocalDateTime.now();
    }

    public WeatherReport(String cityName, double temperature, int humidity, double windSpeed, String condition, LocalDateTime timestamp) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    public String getCityName() {
        return cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getCondition() {
        return condition;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    @Override
    public String toString() {
        return String.format(
            "+--------------------------------------------------+\n" +
            "| Weather Report: %-32s |\n" +
            "+--------------------------------------------------+\n" +
            "| Temperature : %-6.1f °C                           |\n" +
            "| Humidity    : %-3d %%                              |\n" +
            "| Wind Speed  : %-6.1f m/s                          |\n" +
            "| Condition   : %-34s |\n" +
            "| Recorded At : %-34s |\n" +
            "+--------------------------------------------------+",
            cityName, temperature, humidity, windSpeed, condition, getFormattedTimestamp()
        );
    }
}

class WeatherApiClient {
    private String apiKey;
    private final HttpClient httpClient;

    public WeatherApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public WeatherReport fetchWeather(String city) throws CityNotFoundException, NetworkException {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty.");
        }
        String cleanCity = city.trim();

        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("DEMO_KEY")) {
            try {
                String encodedCity = URLEncoder.encode(cleanCity, StandardCharsets.UTF_8);
                String endpoint = "https:" + String.valueOf('/') + String.valueOf('/') + "api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&appid=" + apiKey.trim() + "&units=metric";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(8))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode == 200) {
                    return parseOpenWeatherJson(response.body(), cleanCity);
                } else if (statusCode == 404) {
                    throw new CityNotFoundException("City '" + cleanCity + "' not found on weather API servers.");
                } else if (statusCode == 401) {
                    throw new NetworkException("API Authentication failed (HTTP 401): Invalid OpenWeatherMap API key.");
                } else {
                    throw new NetworkException("Remote API returned HTTP error code: " + statusCode);
                }
            } catch (HttpConnectTimeoutException e) {
                throw new NetworkException("Connection timed out while reaching weather endpoint.", e);
            } catch (IOException e) {
                throw new NetworkException("Network communication failure: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NetworkException("API request was interrupted.", e);
            }
        } else {
            return fetchSimulatedOrPublicWeather(cleanCity);
        }
    }

    private WeatherReport parseOpenWeatherJson(String json, String fallbackCity) throws NetworkException {
        try {
            double temp = extractNumeric(json, "\"temp\":\\s*(-?[0-9.]+)");
            int humidity = (int) Math.round(extractNumeric(json, "\"humidity\":\\s*([0-9.]+)"));
            double speed = extractNumeric(json, "\"speed\":\\s*([0-9.]+)");
            String condition = extractString(json, "\"main\":\\s*\"([^\"]+)\"");
            String parsedName = extractString(json, "\"name\":\\s*\"([^\"]+)\"");
            String resolvedCity = (parsedName != null && !parsedName.isEmpty()) ? parsedName : fallbackCity;
            return new WeatherReport(resolvedCity, temp, humidity, speed, condition);
        } catch (Exception e) {
            throw new NetworkException("Failed to parse JSON payload from weather endpoint: " + e.getMessage(), e);
        }
    }

    private double extractNumeric(String json, String regex) throws Exception {
        Matcher matcher = Pattern.compile(regex).matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IllegalArgumentException("Key matching regex " + regex + " not found in response.");
    }

    private String extractString(String json, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Unknown";
    }

    private WeatherReport fetchSimulatedOrPublicWeather(String city) throws CityNotFoundException {
        String lower = city.toLowerCase().trim();
        Map<String, double[]> mockDatabase = new HashMap<>();
        mockDatabase.put("kolkata", new double[]{31.5, 78.0, 3.6});
        mockDatabase.put("london", new double[]{14.2, 82.0, 5.8});
        mockDatabase.put("tokyo", new double[]{18.4, 65.0, 4.2});
        mockDatabase.put("new york", new double[]{12.8, 55.0, 6.1});
        mockDatabase.put("delhi", new double[]{29.0, 60.0, 2.5});
        mockDatabase.put("paris", new double[]{16.0, 72.0, 3.9});
        mockDatabase.put("sydney", new double[]{22.5, 68.0, 7.2});
        mockDatabase.put("singapore", new double[]{30.8, 85.0, 2.8});

        if (mockDatabase.containsKey(lower)) {
            double[] data = mockDatabase.get(lower);
            String cond = data[0] > 28.0 ? "Sunny" : (data[0] < 15.0 ? "Rainy" : "Partly Cloudy");
            String properName = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            return new WeatherReport(properName, data[0], (int) data[1], data[2], cond);
        }

        if (lower.matches(".*[0-9!@#$%^&*()_+=\\[\\]{}|;':\",.<>?/].*")) {
            throw new CityNotFoundException("City name '" + city + "' contains invalid characters or does not exist.");
        }

        int hash = Math.abs(lower.hashCode());
        double pseudoTemp = 15.0 + (hash % 220) / 10.0;
        int pseudoHumidity = 45 + (hash % 50);
        double pseudoWind = 2.0 + (hash % 80) / 10.0;
        String[] conditions = {"Sunny", "Cloudy", "Clear", "Overcast", "Light Rain"};
        String pseudoCondition = conditions[hash % conditions.length];
        String properName = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        return new WeatherReport(properName, pseudoTemp, pseudoHumidity, pseudoWind, pseudoCondition);
    }
}

class WeatherManager {
    private final Map<String, WeatherReport> cache;
    private final List<WeatherReport> history;
    private final WeatherApiClient apiClient;

    public WeatherManager(WeatherApiClient apiClient) {
        this.cache = new HashMap<>();
        this.history = new ArrayList<>();
        this.apiClient = apiClient;
    }

    public WeatherReport getRealTimeWeather(String city, boolean forceRefresh) throws CityNotFoundException, NetworkException {
        String key = city.trim().toLowerCase();
        if (!forceRefresh && cache.containsKey(key)) {
            return cache.get(key);
        }
        WeatherReport report = apiClient.fetchWeather(city);
        cache.put(key, report);
        history.add(report);
        return report;
    }

    public WeatherReport lookupCachedReport(String city) throws CityNotFoundException {
        String key = city.trim().toLowerCase();
        if (!cache.containsKey(key)) {
            throw new CityNotFoundException("No cached weather record found for '" + city + "'. Please fetch live first.");
        }
        return cache.get(key);
    }

    public List<WeatherReport> getAllHistory() {
        return new ArrayList<>(history);
    }

    public Map<String, WeatherReport> getAllCached() {
        return new HashMap<>(cache);
    }

    public void displaySummaryStatistics() {
        if (history.isEmpty()) {
            System.out.println("No weather logs available to calculate statistics.");
            return;
        }

        double totalTemp = 0.0;
        double minTemp = Double.MAX_VALUE;
        double maxTemp = -Double.MAX_VALUE;
        String minCity = "";
        String maxCity = "";

        for (WeatherReport r : history) {
            double t = r.getTemperature();
            totalTemp += t;
            if (t < minTemp) {
                minTemp = t;
                minCity = r.getCityName();
            }
            if (t > maxTemp) {
                maxTemp = t;
                maxCity = r.getCityName();
            }
        }

        double avgTemp = totalTemp / history.size();

        System.out.println("\n==================================================");
        System.out.println("          HISTORICAL WEATHER ANALYTICS            ");
        System.out.println("==================================================");
        System.out.printf("Total Inquiries Logged : %d\n", history.size());
        System.out.printf("Unique Cities Cached   : %d\n", cache.size());
        System.out.printf("Average Temperature    : %.2f °C\n", avgTemp);
        System.out.printf("Maximum Recorded Temp  : %.1f °C (%s)\n", maxTemp, maxCity);
        System.out.printf("Minimum Recorded Temp  : %.1f °C (%s)\n", minTemp, minCity);
        System.out.println("==================================================\n");
    }
}
