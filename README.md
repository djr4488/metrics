# metrics-cdi-extensions

## Purpose

To provide a simplified mechanism for managing dropwizard metrics in a Java EE environment.  

## Status
* Build: [![Build Status](https://travis-ci.org/djr4488/metrics.svg?branch=master)](https://travis-ci.org/djr4488/metrics)
* Coverage: [![Coverage Status](https://coveralls.io/repos/github/djr4488/metrics/badge.svg?branch=master)](https://coveralls.io/github/djr4488/metrics?branch=master)  [![codecov](https://codecov.io/gh/djr4488/metrics/branch/master/graph/badge.svg)](https://codecov.io/gh/djr4488/metrics)
* Maintenance: [![Percentage of issues still open](http://isitmaintained.com/badge/open/djr4488/metrics.svg)](http://isitmaintained.com/project/djr4488/metrics "Percentage of issues still open")  [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/djr4488/metrics.svg)](http://isitmaintained.com/project/djr4488/metrics "Average time to resolve an issue")

## TODOs

* update this usage guide
* other dropwizard-metrics reporters
* separate out eclipselink jpa profiler into its own project

## Dependencies

* metrics-cdi; [https://github.com/astefanutti/metrics-cdi]
* dropwizard-metrics; [https://github.com/dropwizard/metrics]
* metrics-influxdb; [https://github.com/davidB/metrics-influxdb]
* slf4j; [https://github.com/qos-ch/slf4j]
* owner; [https://github.com/lviggiano/owner]
* javaee-api version 7.0

## Usage

### Configuration
I switched configuration to using aeonbits OWNER, so configuration is now done a little differently.
Normally I put my configuration files in my applications resources folder.

#### MetricsRegistryBean Configuration
To configure the MetricsRegistryBean you need a properties file called MetricsRegistryBeanConfig.properties an example
below is provided -

```
enableSlf4jReporter=true
slf4jReporterNames=slf4jReporterBean
enableScheduledReporters=true
scheduledReporterNames=influxReporterBean
enableJvmCapture=true
healthCheckNamesToRegister=databaseHealthCheck;jmsHealthCheck
```
If you have more than one reporter bean or healthcheck bean, then separate them by a ";".

#### Reporter Bean Configurations
Currently I have two reporter beans configured.  You can also write your own, but if you choose to use the two I have provided, then their configuration would look like the following.

To write your own reporter bean, simply implement the "ReporterBean" interface and give your class an @Named("whatEverNameYouWant") annotation.  Then you can add your bean name to the scheduledReporterNames if not using an slf4jReporterBean, or if you are using an slf4jReporterBean then you can add it there.

First the Slf4jReporterBean;

```
slf4jReportFrequency=30
```
The frequency for this is in seconds.


Second the InfluxReporterBean

```
influxUrl=influxdb.url
influxPort=443
username=influxUserName
password=influxPassword
database=metricsDatabase
protocol=https
appName=metrics_extensions
clusterName=metrics_cluster
influxReportFrequency=30
enableInfluxReporter=false
```
The frequency for this is in seconds as well.

#### Health checks configuration
With health checks I have provided two implementations one for database healthcheck which is configured in the tests to hsqldb.  I have also provided JMS health check bean as well.  You need to configure the MetricsRegistryBean to include the names of the healthchecks you want to provide.  See the above MetricsRegistryBean configuration and healthCheckNamesToRegister for an example.

Below is an example configuration for the databaseHealthCheck.  There is no needed configuration for the JMS healthcheck as it will create a temporary queue itself.
```
persistenceUnitNames=jdbc/metrics_persistence_unit
testSqlByPersistenceName=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
```


