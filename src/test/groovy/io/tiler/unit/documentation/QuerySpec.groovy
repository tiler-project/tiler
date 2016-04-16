package io.tiler.unit.documentation

import io.tiler.internal.queries.QueryFactory
import org.vertx.java.core.json.JsonArray
import spock.lang.Shared
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class QuerySpec extends Specification {
  @Shared def documentationEntries = [
  [
    heading: 'From clause',
    description: 'A query starts with a from clause. In the example below, the `from` clause is followed by the name of a metric.',
    query: '''
from some.metric
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      },
      {
        "value": "B"
      }
    ]
  }
]
''',
    expectedOutput: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      },
      {
        "value": "B"
      }
    ]
  }
]
'''
  ],
  [
    heading: 'From clause with regular expression',
    description: 'The from clause can also be used with a regular expression to match multiple metrics.',
    query: '''
from /^some\\./
''',
metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      }
    ]
  },
  {
    "name": "some.other.metric",
    "points": [
      {
        "value": "B"
      }
    ]
  },
  {
    "name": "third.metric",
    "points": [
      {
        "value": "C"
      }
    ]
  }
]
''',
    expectedOutput: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      }
    ]
  },
  {
    "name": "some.other.metric",
    "points": [
      {
        "value": "B"
      }
    ]
  }
]
'''
  ]
]

  @Shared def expectedMarkdown = '''
### From clause

A query starts with a from clause. In the example below, the `from` clause is followed by the name of a metric.

```
from some.metric
```

Example metrics:

```
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      },
      {
        "value": "B"
      }
    ]
  }
]
```

Output of query:

```
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      },
      {
        "value": "B"
      }
    ]
  }
]
```

### From clause with regular expression

The from clause can also be used with a regular expression to match multiple metrics.

```
from /^some\\./
```

Example metrics:

```
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      }
    ]
  },
  {
    "name": "some.other.metric",
    "points": [
      {
        "value": "B"
      }
    ]
  },
  {
    "name": "third.metric",
    "points": [
      {
        "value": "C"
      }
    ]
  }
]
```

Output of query:

```
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A"
      }
    ]
  },
  {
    "name": "some.other.metric",
    "points": [
      {
        "value": "B"
      }
    ]
  }
]
```

'''

  def normaliseJsonArrayString(String jsonArrayString) {
    new JsonArray(jsonArrayString).encode()
  }

  def evaluateQuery(queryString, metricsString, expectedOutputString) {
    def queryFactory = new QueryFactory()
    def query = queryFactory.parseQuery(queryString)

    def clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Z"))
    def metrics = new JsonArray(metricsString)

    def actualOutput = query.applyToMetrics(clock, metrics)

    return actualOutput.encode()
  }

  def generateMarkdown(documentationEntries) {
    def builder = new StringBuilder()
      .append('\n')

    documentationEntries.each { entry ->
      builder
        .append('### ').append(entry.heading).append('\n')
        .append('\n')
        .append(entry.description).append('\n')
        .append('\n')
        .append('```')
        .append(entry.query)
        .append('```\n')
        .append('\n')
        .append("Example metric${entry.metrics.length() > 1 ? 's' : ''}:\n")
        .append('\n')
        .append('```')
        .append(entry.metrics)
        .append('```\n')
        .append('\n')
        .append("Output of query:\n")
        .append('\n')
        .append('```')
        .append(entry.expectedOutput)
        .append('```\n')
        .append('\n')
    }

    return builder.toString()
  }

  def "documentation examples should have no bugs"() {
    given:
    def expectedOutput = normaliseJsonArrayString(entry.expectedOutput)

    when:
    def actualOutput = evaluateQuery(entry.query, entry.metrics, entry.expectedOutput)

    then:
    actualOutput == expectedOutput

    where:
    entry << documentationEntries
  }

  def "documentation entries should generate expected markdown"() {
    when:
    def actualMarkdown = generateMarkdown(documentationEntries)

    then:
    actualMarkdown == expectedMarkdown
  }
}
