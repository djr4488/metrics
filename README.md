# metrics-cdi-extensions

## Purpose

To provide a simplified mechanism for managing dropwizard metrics in a Java EE environment.  

## Status
* Build: [![Build Status](https://travis-ci.org/djr4488/metrics.svg?branch=master)](https://travis-ci.org/djr4488/metrics)
* Coverage: [![Coverage Status](https://coveralls.io/repos/github/djr4488/metrics/badge.svg?branch=master)](https://coveralls.io/github/djr4488/metrics?branch=master)
* Maintenance: [![Percentage of issues still open](http://isitmaintained.com/badge/open/djr4488/metrics.svg)](http://isitmaintained.com/project/djr4488/metrics "Percentage of issues still open")

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

To configure the MetricsRegistryBean you need a properties file called MetricsRegistryBeanConfig.properties an example
below is provided -

```
enableSlf4jReporter=true
slf4jReporterNames=slf4jReporterBean
enableScheduledReporters=false
scheduledReporterNames=influxReporterBean
enableJvmCapture=true
healthCheckNamesToRegister=
eclispseLinkProfileWeight=ALL
```
If you have more than one reporter bean or healthcheck bean, then separate them by a ";".

If you choose to use the default Slf4jReporterBean, then you'll want to also include the following configuration for it
in the file Slf4jReporterBeanConfig.

```
slf4jReportFrequency=30
```
The frequency for this is in seconds.


If you choose the default provided InfluxReporterBean then you'll need a InfluxReporterBeanConfig.properties with the
following config -

```
influxUrl=url to your influxdb
influxPort=port to use for your influxdb
username=username to use for your influxdb
password=password for your influxdb
database=the database schema for your influxdb
protocol=protocol to use for your influxdb(http / https)
appName=appName to use for tagging
clusterName=clusterName to use for tagging
influxReportFrequency=frequency in seconds it should report
```

If you choose not to use the default reporters just implement the ReporterBean interface and provide the properties to
those anyway you wish.

Likewise with health checks, if you choose not to use the default health checks just write your own and extend HealthCheck
from dropwizard.

Then be sure to include an @Named("name_of_your_bean") annotation on it, so CDI can find it.
