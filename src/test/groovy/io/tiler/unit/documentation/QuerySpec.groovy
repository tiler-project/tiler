package io.tiler.unit.documentation

import io.tiler.internal.queries.QueryFactory
import org.vertx.java.core.json.JsonArray
import spock.lang.Shared
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class QuerySpec extends Specification {
  @Shared def documentationEntries = [
  [
    content: '''
### From clause

A query starts with a from clause. In the example below, the `from` clause is followed by the name of a metric.
''',
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
    content: '''
#### From clause with regular expression

The from clause can also be used with a regular expression to match multiple metrics.
''',
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
  ],
  [
    content: '''
#### From clause with multiple expressions

A comma separated list of metric names and/or regular expressions can be used with the `from` clause.
''',
    query: '''
from /^some\\./,
third.metric
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
'''
  ],
  [
    content: '''
### Point clause',

The `point` clause can be used to select what fields to return for each point of the metric(s). Only field specified in the point clause will be returned in the metric(s) points.
''',
    query: '''
from some.metric
point time, value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A",
        "time": 123,
        "another.field": "A"
      },
      {
        "value": "B",
        "time": 456,
        "another.field": "B"
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
        "time": 123,
        "value": "A"
      },
      {
        "time": 456,
        "value": "B"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### Point clause with aliases

Aliases can be specified for fields in the `point` clause, which causes the fields to be returned with alternative names.
''',
    query: '''
from some.metric
point time, value as another.name
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A",
        "time": 123
      },
      {
        "value": "B",
        "time": 456
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
        "another.name": "A",
        "time": 123
      },
      {
        "another.name": "B",
        "time": 456
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Metric clause

The `metric` clause can be used to select the fields on the metric(s) to return.
''',
    query: '''
from some.metric
metric name, another.field
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "another.field": "Red",
    "third.field": "Green",
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
    "another.field": "Red",
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
    content: '''
### Where clause

The `where` clause filters the points that are returned.
''',
    query: '''
from some.metric
where value == 'B'
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
        "value": "B"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Sorting points

The `sort` clause sorts the points of each that is returned.  When sorting points, the sort clause must be used together with a point clause.
''',
    query: '''
from some.metric
point value, time
sort time
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "A",
        "time": "123"
      },
      {
        "value": "B",
        "time": "789"
      },
      {
        "value": "C",
        "time": "456"
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
        "time": "123",
        "value": "A"
      },
      {
        "time": "456",
        "value": "C"
      },
      {
        "time": "789",
        "value": "B"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### Sort ascending and descending

A sort can be based on multiple fields and fields can be sorted in ascending and descending order.
''',
    query: '''
from some.metric
point value, time
sort time desc, value asc
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "time": "123",
        "value": "B"
      },
      {
        "time": "123",
        "value": "A"
      },
      {
        "time": "456",
        "value": "D"
      },
      {
        "time": "456",
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
        "time": "456",
        "value": "C"
      },
      {
        "time": "456",
        "value": "D"
      },
      {
        "time": "123",
        "value": "A"
      },
      {
        "time": "123",
        "value": "B"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Sorting metrics

Metrics can also be sorted. In this case, the `sort` clause needs to be used together with the metric clause.
''',
    query: '''
from /.*/
metric name, another.field
sort another.field
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "another.field": "B",
    "points": [
      {
        "value": "A"
      }
    ]
  },
  {
    "name": "another.metric",
    "another.field": "A",
    "points": [
      {
        "value": "A"
      }
    ]
  }
]
''',
    expectedOutput: '''
[
  {
    "name": "another.metric",
    "another.field": "A",
    "points": [
      {
        "value": "A"
      }
    ]
  },
  {
    "name": "some.metric",
    "another.field": "B",
    "points": [
      {
        "value": "A"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Group clause

The `group` clause can be used to merge the points from multiple metrics. The group clause is used with a list of point fields, which is used to group related pointed together under new metrics. For the point fields specified, points that have the same field values will be placed in the same new metric and points with different point values will be placed in different new metrics. Any fields used to group points will also be copied to the new metrics as metric fields - see the `ocean` field on the new metrics in the example output below.
''',
    query: '''
from /.*/
group ocean
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Atlantic",
        "value": "A"
      },
      {
        "ocean": "Pacific",
        "value": "B"
      }
    ]
  },
  {
    "name": "another.metric",
    "points": [
      {
        "ocean": "Atlantic",
        "value": "C"
      },
      {
        "ocean": "Pacific",
        "value": "D"
      }
    ]
  }
]
''',
    expectedOutput: '''
[
  {
    "ocean": "Pacific",
    "points": [
      {
        "ocean": "Pacific",
        "value": "B"
      },
      {
        "ocean": "Pacific",
        "value": "D"
      }
    ]
  },
  {
    "ocean": "Atlantic",
    "points": [
      {
        "ocean": "Atlantic",
        "value": "A"
      },
      {
        "ocean": "Atlantic",
        "value": "C"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Aggregate clause

The `aggregate` clause can be used to combine the points of a metric. The clause is typically used with an aggregate function like `interval`.
When the aggregation expression is evaluated, points that evaluate to the same value are combined into a single point.
In the example below, points are placed into 10 second buckets.
''',
    query: '''
from some.metric
aggregate interval(time, 0, 10s) as time
point time, sum(value) as total
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "time": 0,
        "value": 1
      },
      {
        "time": 5000000,
        "value": 10
      },
      {
        "time": 10000000,
        "value": 100
      },
      {
        "time": 15000000,
        "value": 1000
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
        "total": 11.0,
        "time": 0
      },
      {
        "total": 1100.0,
        "time": 10000000
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Comparison operators

The following comparison operators are supported:

| Operator | Meaning                |
| -------- | ---------------------- |
| ==       | Equals                 |
| >        | Greater than           |
| >=       | Greater than or equals |
| <        | Less than              |
| <=       | Less than or equals    |
| !=       | Not equals             |
| ~=       | Regex find             |

Comparison operators, like all operators, constants and most functions, can be used in the `where`, `point`, `metric`, `group` and `aggregate` clauses.
''',
    query: '''
from some.metric
where value > 2
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": 1
      },
      {
        "value": 2
      },
      {
        "value": 3
      },
      {
        "value": 4
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
        "value": 3
      },
      {
        "value": 4
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### Regex find operator

The `regex find` operator Evaluates to true if the regular expression on the right can be found within the expression on the right.
''',
    query: '''
from some.metric
where value ~= /World/
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "Hello World"
      },
      {
        "value": "Goodbye World"
      },
      {
        "value": "Hello Mars"
      },
      {
        "value": "Goodbye Mars"
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
        "value": "Hello World"
      },
      {
        "value": "Goodbye World"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Boolean operators

The following boolean operators are supported:

| Operator | Meaning       |
| -------- | ------------- |
| &&       | Logical `and` |
| ||       | Logical `or`  |
| !        | Logical `not` |

Boolean operators, like all operators and constants, can be used in the `where`, `point`, `metric`, `group` and `aggregate` clauses.
''',
    query: '''
from some.metric
where value > 2 && !active
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "active": true,
        "value": 1
      },
      {
        "active": false,
        "value": 2
      },
      {
        "active": true,
        "value": 3
      },
      {
        "active": false,
        "value": 4
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
        "active": false,
        "value": 4
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Functions

The following functions are supported:

| Function  |
| --------- |
| concat    |
| first     |
| last      |
| max       |
| min       |
| mean      |
| sum       |
| now       |
| replace   |
| substring |

#### concat function

The `concat` function concatenates strings.
''',
    query: '''
from some.metric
point concat(a, b) as c
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "a": "Hello, ",
        "b": "World!"
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
        "c": "Hello, World!"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### first function

The `first` function is typically used with the aggregate clause, to select the first value of a field.
''',
    query: '''
from some.metric
aggregate ocean
point ocean, first(value) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Pacific",
        "value": "a"
      },
      {
        "ocean": "Pacific",
        "value": "b"
      },
      {
        "ocean": "Atlantic",
        "value": "c"
      },
      {
        "ocean": "Atlantic",
        "value": "d"
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
        "ocean": "Pacific",
        "value": "a"
      },
      {
        "ocean": "Atlantic",
        "value": "c"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### last function

The `last` function is also used with the aggregate clause and selects the last value of a merged field.
''',
    query: '''
from some.metric
aggregate ocean
point ocean, last(value) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Pacific",
        "value": "a"
      },
      {
        "ocean": "Pacific",
        "value": "b"
      },
      {
        "ocean": "Atlantic",
        "value": "c"
      },
      {
        "ocean": "Atlantic",
        "value": "d"
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
        "ocean": "Pacific",
        "value": "b"
      },
      {
        "ocean": "Atlantic",
        "value": "d"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### max function

The `max` function can be used with the aggregate clause to select the field value with the highest numerical value.
''',
    query: '''
from some.metric
aggregate ocean
point ocean, max(value) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Pacific",
        "value": 1
      },
      {
        "ocean": "Pacific",
        "value": 2
      },
      {
        "ocean": "Atlantic",
        "value": 4
      },
      {
        "ocean": "Atlantic",
        "value": 3
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
        "ocean": "Pacific",
        "value": 2.0
      },
      {
        "ocean": "Atlantic",
        "value": 4.0
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### min function

The `min` function can be used with the aggregate clause to select the field value with the lowest numerical value.
''',
    query: '''
from some.metric
aggregate ocean
point ocean, min(value) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Pacific",
        "value": 1
      },
      {
        "ocean": "Pacific",
        "value": 2
      },
      {
        "ocean": "Atlantic",
        "value": 4
      },
      {
        "ocean": "Atlantic",
        "value": 3
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
        "ocean": "Pacific",
        "value": 1.0
      },
      {
        "ocean": "Atlantic",
        "value": 3.0
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### mean function

The `mean` function can be used with the aggregate clause to calculate the numerical mean of field values being merged.
''',
    query: '''
from some.metric
aggregate ocean
point ocean, mean(value) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Pacific",
        "value": 1
      },
      {
        "ocean": "Pacific",
        "value": 2
      },
      {
        "ocean": "Atlantic",
        "value": 4
      },
      {
        "ocean": "Atlantic",
        "value": 3
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
        "ocean": "Pacific",
        "value": 1.5
      },
      {
        "ocean": "Atlantic",
        "value": 3.5
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### sum function

The `sum` function can be used with the aggregate clause to calculate the numerical sum of field values being merged.
''',
    query: '''
from some.metric
aggregate ocean
point ocean, sum(value) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "ocean": "Pacific",
        "value": 1
      },
      {
        "ocean": "Pacific",
        "value": 2
      },
      {
        "ocean": "Atlantic",
        "value": 4
      },
      {
        "ocean": "Atlantic",
        "value": 3
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
        "ocean": "Pacific",
        "value": 3.0
      },
      {
        "ocean": "Atlantic",
        "value": 7.0
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### now function

The `now` function returns the number of microseconds since 00:00 on 1 January 1970.
It is often used with the where clause to filter points to a certain time frame, relative to the current date time.
In the following example, the "current date time" is 2010-01-01T00:00Z.
''',
    query: '''
from some.metric
where time >= now()
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "time": 1262303000000000,
        "value": "a"
      },
      {
        "time": 1262304000000000,
        "value": "b"
      },
      {
        "time": 1262305000000000,
        "value": "c"
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
        "time": 1262304000000000,
        "value": "b"
      },
      {
        "time": 1262305000000000,
        "value": "c"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### replace function

The `replace` function performs a regular expression based replacement on a string based value.
The function has the following parameters:

| Parameter   | Description                                                                                                                                                                                     |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| value       | field or other value                                                                                                                                                                            |
| regex       | Regular expression to search for in the `value`                                                                                                                                                 |
| replacement | The replacement string works the same as the argument in Java's Matcher.replaceAll function https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html#replaceAll-java.lang.String- |
''',
    query: '''
from some.metric
point replace(value, /Hello ([a-z]+)/i, "Goodbye $1") as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "Hello World"
      },
      {
        "value": "Hello Mars"
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
        "value": "Goodbye World"
      },
      {
        "value": "Goodbye Mars"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### substring function

The `substring` function returns a substring of the specified value.
The function has the following parameters:

| Parameter  | Description                                   |
| ---------- | --------------------------------------------- |
| value      | field or other value                          |
| beginIndex | The starting index of the substring to return |
| endIndex   | The ending index of the substring to return   |
''',
    query: '''
from some.metric
point substring(value, 6, 11) as value
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "Hello World"
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
        "value": "World"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Aggregations

The following aggregations are supported:

| Function |
| -------- |
| all      |
| interval |

#### all function

The `all` function can be used with the aggregate clause to merge all points into a single point.
''',
    query: '''
from some.metric
aggregate all() as some.field
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "a"
      },
      {
        "value": "b"
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
        "some.field": true,
        "value": ["a", "b"]
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### interval function

The `interval` function can be used with the aggregate clause to place points into buckets of values. It can be useful to divide points into buckets of time.
''',
    query: '''
from some.metric
aggregate interval(time, 0, 10s) as time
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "a",
        "time": 1000000000
      },
      {
        "value": "b",
        "time": 1000000001
      },
      {
        "value": "c",
        "time": 2000000000
      },
      {
        "value": "d",
        "time": 2000000001
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
        "time": 1000000000,
        "value": ["a", "b"]
      },
      {
        "time": 2000000000,
        "value": ["c", "d"]
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Arithmetic Operators

The following arithmetic operators are supported:

| Operator | Meaning  |
| -------- | -------- |
| *        | Multiply |
| /        | Divide   |
| +        | Add      |
| -        | Subtract |
''',
    query: '''
from some.metric
point value * 2 as multiplication,
value / 2 as division,
value + 2 as addition,
value - 2 as subtraction
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": 1
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
        "division": 0.5,
        "subtraction": -1.0,
        "multiplication": 2.0,
        "addition": 3.0
      }
    ]
  }
]
'''
  ],
  [
    content: '''
### Constants

The following arithmetic operators are supported:

| Constant      | Example   |
| ------------- | --------- |
| Boolean       | true      |
| Integer       | 1         |
| String        | "text"    |
| Time period   | 1s        |
| Regex pattern | /pattern/ |

#### Boolean constants
''',
    query: '''
from some.metric
where value == true
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": true
      },
      {
        "value": false
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
        "value": true
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### Integer constants
''',
    query: '''
from some.metric
where value > 1
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": 1
      },
      {
        "value": 2
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
        "value": 2
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### String constants
''',
    query: '''
from some.metric
where value == "b"
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "a"
      },
      {
        "value": "b"
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
        "value": "b"
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### Time period constants

A time period can included in a query by adding one of the following letters to the end up a number:

| Letter | Unit         |
| ------ | ------------ |
| u      | Microseconds |
| s      | Seconds      |
| m      | Minutes      |
| h      | Hours        |
| d      | Days         |
| w      | Weeks        |

`10s` would represent 10 seconds. `1w` would represent 1 week.
''',
    query: '''
from some.metric
where time > 1u
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "time": 1
      },
      {
        "time": 2
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
        "time": 2
      }
    ]
  }
]
'''
  ],
  [
    content: '''
#### Regex patterns

A regex pattern can be included in a query using the syntax `/pattern/`.  Regex patterns have
the same syntax as [Java regex patterns](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

Regex options can be specified by placing one of of the following letters after the closing forward slash of a
pattern.  E.g. /pattern/i would make the regex pattern case-insensitive.

| Letter | Unit                    | More information                                                                               |
| ------ | ----------------------- | ---------------------------------------------------------------------------------------------- |
| d      | Unix lines              | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#UNIX_LINES              |
| i      | Case-insensitive        | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#CASE_INSENSITIVE        |
| x      | Comments                | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#COMMENTS                |
| m      | Multiline               | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#MULTILINE               |
| s      | Dotall                  | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#DOTALL                  |
| u      | Unicode case            | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#UNICODE_CASE            |
| U      | Unicode character class | https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#UNICODE_CHARACTER_CLASS |
''',
    query: '''
from some.metric
where value ~= /fic/
''',
    metrics: '''
[
  {
    "name": "some.metric",
    "points": [
      {
        "value": "Pacific"
      },
      {
        "value": "Atlantic"
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
        "value": "Pacific"
      }
    ]
  }
]
'''
  ]
]

  @Shared def expectedMarkdown = this.getClass().getResource('/io/tiler/unit/documentation/expected-query.md').text

  def normaliseJsonArrayString(String jsonArrayString) {
    new JsonArray(jsonArrayString).encode()
  }

  def evaluateQuery(queryString, metricsString) {
    def queryFactory = new QueryFactory()
    def query = queryFactory.parseQuery(queryString)

    def clock = Clock.fixed(ZonedDateTime.of(2010, 01, 01, 0, 0, 0, 0, ZoneId.of("Z")).toInstant(), ZoneId.of("Z"))
    def metrics = new JsonArray(metricsString)

    def actualOutput = query.applyToMetrics(clock, metrics)

    return actualOutput.encode()
  }

  def generateMarkdown(documentationEntries) {
    def builder = new StringBuilder()

    documentationEntries.each { entry ->
      builder
        .append(entry.content)
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
    }

    return builder.toString()
  }

  def "documentation examples should generate the expected output"() {
    given:
    def expectedOutput = normaliseJsonArrayString(entry.expectedOutput)

    when:
    def actualOutput = evaluateQuery(entry.query, entry.metrics)

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
