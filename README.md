[![Build Status](https://travis-ci.org/evolution-gaming/metered-scaffeine.svg)](https://travis-ci.org/evolution-gaming/metered-scaffeine) [![Coverage Status](https://coveralls.io/repos/evolution-gaming/metered-scaffeine/badge.svg)](https://coveralls.io/r/evolution-gaming/metered-scaffeine) [ ![version](https://api.bintray.com/packages/evolutiongaming/maven/metered-scaffeine/images/download.svg) ](https://bintray.com/evolutiongaming/maven/metered-scaffeine/_latestVersion)

# metered-scaffeine 
Scaffeine cache integrated with Prometheus metrics and support for scala ExecutionContext

## Installation

```scala
addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

libraryDependencies += "com.evolutiongaming" %% "metered-scaffeine" % "4.0.1"
```

## Metrics

Caches record their statistics with a `MeteredStatsCounter`. Pick the one for the Prometheus client you use:

```scala
// Prometheus simpleclient 0.x
Scaffeine().asyncCache[K, V]("cache_name", collectorRegistry)(loader)

// Prometheus client 1.x: add "io.prometheus" % "prometheus-metrics-core" to your dependencies first
Scaffeine().meteredAsyncCache[K, V](new PrometheusV1MetricsStatsCounter(prometheusRegistry, "cache_name"))(loader)
```

Both expose the same metrics: `<name>_hits`, `_misses`, `_evictions`, `_blocking_calls`, `_load_result`,
`_load_time` and `_estimated_size`. The 1.x client adds the `_total` suffix to the counters, and computes the
`_load_time` quantiles over the last 5 minutes instead of 10.
