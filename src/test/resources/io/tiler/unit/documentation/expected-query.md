
## Query Language

Tiler includes its own custom query language.  The language is loosely based on SQL, though some of its syntax is more
similar to C based languages like Java than they are to SQL.

### From clause

A query starts with a from clause. In the example below, the `from` clause is followed by the name of a metric.

```
from some.metric
```

Example metrics:

``` json
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

``` json
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

#### From clause with regular expression

The from clause can also be used with a regular expression to match multiple metrics.

```
from /^some\./
```

Example metrics:

``` json
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

``` json
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

#### From clause with multiple expressions

A comma separated list of metric names and/or regular expressions can be used with the `from` clause.

```
from /^some\./,
third.metric
```

Example metrics:

``` json
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

``` json
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

### Point clause

The `point` clause can be used to select what fields to return for each point of the metric(s). Only field specified in the point clause will be returned in the metric(s) points.

```
from some.metric
point time, value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### Point clause with aliases

Aliases can be specified for fields in the `point` clause, which causes the fields to be returned with alternative names.

```
from some.metric
point time, value as another.name
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Metric clause

The `metric` clause can be used to select the fields on the metric(s) to return.

```
from some.metric
metric name, another.field
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Where clause

The `where` clause filters the points that are returned.

```
from some.metric
where value == 'B'
```

Example metrics:

``` json
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

``` json
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
```

### Sorting points

The `sort` clause sorts the points of each that is returned.  When sorting points, the sort clause must be used together with a point clause.

```
from some.metric
point value, time
sort time
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### Sort ascending and descending

A sort can be based on multiple fields and fields can be sorted in ascending and descending order.

```
from some.metric
point value, time
sort time desc, value asc
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Sorting metrics

Metrics can also be sorted. In this case, the `sort` clause needs to be used together with the metric clause.

```
from /.*/
metric name, another.field
sort another.field
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Group clause

The `group` clause can be used to merge the points from multiple metrics. The group clause is used with a list of point fields, which is used to group related pointed together under new metrics. For the point fields specified, points that have the same field values will be placed in the same new metric and points with different point values will be placed in different new metrics. Any fields used to group points will also be copied to the new metrics as metric fields - see the `ocean` field on the new metrics in the example output below.

```
from /.*/
group ocean
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Aggregate clause

The `aggregate` clause can be used to combine the points of a metric. The clause is typically used with an aggregate function like `interval`.
When the aggregation expression is evaluated, points that evaluate to the same value are combined into a single point.
In the example below, points are placed into 10 second buckets.

```
from some.metric
aggregate interval(time, 0, 10s) as time
point time, sum(value) as total
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

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

```
from some.metric
where value > 2
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### Regex find operator

The `regex find` operator Evaluates to true if the regular expression on the right can be found within the expression on the right.

```
from some.metric
where value ~= /World/
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Boolean operators

The following boolean operators are supported:

| Operator | Meaning       |
| -------- | ------------- |
| &&       | Logical `and` |
| ||       | Logical `or`  |
| !        | Logical `not` |

Boolean operators, like all operators and constants, can be used in the `where`, `point`, `metric`, `group` and `aggregate` clauses.

```
from some.metric
where value > 2 && !active
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

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

```
from some.metric
point concat(a, b) as c
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### first function

The `first` function is typically used with the aggregate clause, to select the first value of a field.

```
from some.metric
aggregate ocean
point ocean, first(value) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### last function

The `last` function is also used with the aggregate clause and selects the last value of a merged field.

```
from some.metric
aggregate ocean
point ocean, last(value) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### max function

The `max` function can be used with the aggregate clause to select the field value with the highest numerical value.

```
from some.metric
aggregate ocean
point ocean, max(value) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### min function

The `min` function can be used with the aggregate clause to select the field value with the lowest numerical value.

```
from some.metric
aggregate ocean
point ocean, min(value) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### mean function

The `mean` function can be used with the aggregate clause to calculate the numerical mean of field values being merged.

```
from some.metric
aggregate ocean
point ocean, mean(value) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### sum function

The `sum` function can be used with the aggregate clause to calculate the numerical sum of field values being merged.

```
from some.metric
aggregate ocean
point ocean, sum(value) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### now function

The `now` function returns the number of microseconds since 00:00 on 1 January 1970.
It is often used with the where clause to filter points to a certain time frame, relative to the current date time.
In the following example, the "current date time" is 2010-01-01T00:00Z.

```
from some.metric
where time >= now()
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### replace function

The `replace` function performs a regular expression based replacement on a string based value.
The function has the following parameters:

| Parameter   | Description                                                                                                                                                                                     |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| value       | field or other value                                                                                                                                                                            |
| regex       | Regular expression to search for in the `value`                                                                                                                                                 |
| replacement | The replacement string works the same as the argument in Java's Matcher.replaceAll function https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html#replaceAll-java.lang.String- |

```
from some.metric
point replace(value, /Hello ([a-z]+)/i, "Goodbye $1") as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### substring function

The `substring` function returns a substring of the specified value.
The function has the following parameters:

| Parameter  | Description                                   |
| ---------- | --------------------------------------------- |
| value      | field or other value                          |
| beginIndex | The starting index of the substring to return |
| endIndex   | The ending index of the substring to return   |

```
from some.metric
point substring(value, 6, 11) as value
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Aggregations

The following aggregations are supported:

| Function |
| -------- |
| all      |
| interval |

#### all function

The `all` function can be used with the aggregate clause to merge all points into a single point.

```
from some.metric
aggregate all() as some.field
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### interval function

The `interval` function can be used with the aggregate clause to place points into buckets of values. It can be useful to divide points into buckets of time.

```
from some.metric
aggregate interval(time, 0, 10s) as time
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

### Arithmetic Operators

The following arithmetic operators are supported:

| Operator | Meaning  |
| -------- | -------- |
| *        | Multiply |
| /        | Divide   |
| +        | Add      |
| -        | Subtract |

```
from some.metric
point value * 2 as multiplication,
value / 2 as division,
value + 2 as addition,
value - 2 as subtraction
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

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

```
from some.metric
where value == true
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### Integer constants

```
from some.metric
where value > 1
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

#### String constants

```
from some.metric
where value == "b"
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

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

```
from some.metric
where time > 1u
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```

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

```
from some.metric
where value ~= /fic/
```

Example metrics:

``` json
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
```

Output of query:

``` json
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
```
