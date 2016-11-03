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
* slf4j; [https://github.com/qos-ch/slf4j]
* javaee-api version 7.0

## Usage

Configuration is done by having a metrics-config.properties file that can be picked up by Apache DeltaSpike configuration
A configuration example is provided below;

```
influxUrl=<influxdb url goes here>
influxPort=<influxdb port goes here>
influxUsername=<influxdb username goes here>
influxPassword=<influxdb password goes here>
influxDatabaseSchema=<schema in your influxdb instance>
influxProtocol=<protocol to use for influxdb comms; e.g. http / https>
slf4jReportFrequency=<how often to log to slf4j in seconds>
influxReportFrequency=<how often to send reports to influxdb in seconds>
enableSlf4jReporter=<true if you want to send to slf4j, false otherwise>
enableInfluxReporter=<true if you want to send to influx, false otherwise>
enableJvmCapture=<true if you want to capture jvm statistics, false otherwise>
healthCheckNamesToRegister=<list of healthchecks you want to register, delimited by healthCheckNamesDelimiter>
healthCheckNamesDelimiter=<delimiter to use for separating out the health check names>
applicationName=<name of your application>
clusterName=<name of your cluster>
eclipseLinkProfileWeight=<one of ALL, HEAVY, NORMAL, or NONE>
```
