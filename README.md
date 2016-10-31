# metrics-cdi-extentions

## Purpose

To provide a simplified mechanism for managing dropwizard metrics in a Java EE environment.  

## TODOs

* add test cases
* other dropwizard-metrics
* rename some configuration parameters

## Dependencies

* metrics-cdi; [https://github.com/astefanutti/metrics-cdi]
* dropwizard-metrics; [https://github.com/dropwizard/metrics]
* metrics-influxdb; [https://github.com/davidB/metrics-influxdb]
* slf4j; 

## Usage

Configuration of one class is required; MetricsRegistryBean.  This bean contains the HealthCheckRegistry and the MetricsRegistry.  Plus options for writing to slf4j and influxdb.
Currently, because I @Inject most configuration parameters, they need to be configured whether using them or not, kinda sucks.

* influxUrl; url to your influxdb instance
* port; port to your influxdb instance
* password; password to your influxdb instance
* database; the influxdb database you want to store your metrics in
* protocol; http / https
* slf4jReportFrequency; currently in seconds
* influxReportFrequence; currently in seconds
* enableSlf4jReporter; true if you want to enable it, false otherwise
* enableInfluxReporter; true if you want it enable it, false otherwise
* enableJvmCapture; true if you want to capture jvm statistics and metrics, false otherwise
* healthCheckNamesToRegister; list of @Named classes who extend HealthCheck, by default the dropwizard ThreadDeadlock health check is provided
* appName; name of the application for tagging in influxdb reported metrics
* clusterName; name of the cluster the app runs in for tagging in influxdb reported metrics
