# Tiler

Plugable dashboard framework.  Tiler is a Vert.x based framework that uses React for the browser-side UI.

## Example

See [tiler-example](https://github.com/tiler-project/tiler-example) for an example of how to create a dashboard project with Tiler.

## Collectors

There are two ways to get metrics into Tiler: i) using collectors like [tiler-collector-sonarqube](https://github.com/tiler-project/tiler-collector-sonarqube) and ii) using Tiler's API.

Often the best way to get metrics is to use a collector.  A collector is a vert.x module that pulls metrics into Tiler.  A collector often runs on a schedule, collecting new or updated metrics every few minutes.  See [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ctiler-collector) for a list of all the collectors available for Tiler.

## API

Tiler provides an API, that can be used to push metrics into Tiler.

### Create Metrics

HTTP POST http://localhost:8080/api/v1/metrics

Headers:

  * Content-Type: application/json

Request Body:

``` json
{
    "metrics": [{
        "name": "examples.api",
        "points": [{
            "time": 1,
            "value": 10
        },
        {
            "time": 2,
            "value": 20
        }]
    }]
}
```

View the metric by browsing to http://localhost:8080/dashboards/api

### Query Metrics

HTTP POST http://localhost:8080/api/v1/query

Headers:

  * Content-Type: application/json

Request Body:

``` json
{
    "queries": [
        "from /^.*$/"
    ]
}```
