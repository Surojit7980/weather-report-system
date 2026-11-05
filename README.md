# Object-Oriented Weather Report System (CSE Project)

A modular, robust Java 17 application that encapsulates atmospheric metrics, interfaces with live REST API endpoints via `HttpClient`, leverages the Java Collections Framework (`HashMap` and `ArrayList`) for in-memory caching and audit logging, and enforces multi-tiered exception handling.

#  Features

* **Object-Oriented Encapsulation:** Implements an immutable `WeatherReport` domain model encapsulating temperature, humidity, wind speed, condition, and timestamp.
* **In-Memory Caching (`HashMap`):** Provides $O(1)$ average lookup time for previously queried cities, reducing latency by over 99% compared to remote network round-trips.
* **Historical Audit Trail (`ArrayList`):** Tracks all user inquiries sequentially with amortized $O(1)$ appends and enables real-time statistical aggregations (min, max, and average temperature).
* **REST API Integration:** Dispatches HTTP/2 requests using standard `java.net.http.HttpClient` with zero third-party dependencies.
* **Robust Exception Handling:** Implements custom checked exceptions (`CityNotFoundException`, `NetworkException`) and input validation guards against HTTP 404, 401, timeout, and malformed inputs.
* *Interactive Web Showcase:** Includes an interactive web app (`weather_system_app.html`) for visual demonstration of cache hits, telemetry, and fault simulation.

##  Repository Structure

```text
├── WeatherReportSystem.java              # Main Java source code (100% functional, zero comments)
├── weather_system_app.html              # Interactive web dashboard & collections visualizer
├── Weather_Report_System_Project_Report.md # Full project documentation (Implementation & Results)
├── README.md                            # Project overview & execution guide
└── .gitignore                           # Git ignore rules for compiled Java binaries
