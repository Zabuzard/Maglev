# Maglev

[![codefactor](https://img.shields.io/codefactor/grade/github/Zabuzard/Maglev)](https://www.codefactor.io/repository/github/zabuzard/maglev)
[![maven-central](https://img.shields.io/maven-central/v/io.github.zabuzard.maglev/maglev)](https://search.maven.org/search?q=g:io.github.zabuzard.maglev)
[![javadoc](https://javadoc.io/badge2/io.github.zabuzard.maglev/maglev/javadoc.svg?style=flat&color=AA82FF)](https://javadoc.io/doc/io.github.zabuzard.maglev/maglev)
![Java](https://img.shields.io/badge/Java-11%2B-ff696c)
[![license](https://img.shields.io/github/license/Zabuzard/Maglev)](https://github.com/Zabuzard/Maglev/blob/master/LICENSE)

Maglev is a simple library that provides fast and generic solutions for shortest path problems (SPP). It is designed
generic and can easily be modified and extended.

It is able to compute shortest paths on a given graph structure. It offers:

* `1-1` - one source to one destination
* `n-1` - the closest source to one destination
* `1-*` - one source to all reachable nodes
* `n-*` - the closest sources to all reachable nodes

and the opposite directions by offering an implicit `Graph#reverse()`:

* `1-n` - one source to the closest destination
* `*-1` - all from destination reachable nodes to destination
* `*-n` - all from any destination reachable nodes to the closest destination

By utilizing efficient and well known algorithms, such as:

* Dijkstra
* A-Star
* ALT (A-Star with landmarks)

and providing a high degree of customizability by offering ways to manipulate the algorithm using extensions called _
Dijkstra modules_.

The main interface of the algorithms provide the following methods:

* `Collection<N> searchSpace(Collection<? extends N> sources, N destination)`
* `Collection<N> searchSpace(N source, N destination)`
* `Optional<Path<N, E>> shortestPath(Collection<? extends N> sources, N destination)`
* `Optional<Path<N, E>> shortestPath(N source, N destination)`
* `Optional<Double> shortestPathCost(Collection<? extends N> sources, N destination)`
* `Optional<Double> shortestPathCost(N source, N destination)`
* `Map<N, ? extends HasPathCost> shortestPathCostsReachable(Collection<? extends N> sources)`
* `Map<N, ? extends HasPathCost> shortestPathCostsReachable(N source)`
* `PathTree<N, E> shortestPathReachable(Collection<? extends N> sources)`
* `PathTree<N, E> shortestPathReachable(N source)`

# Requirements

* Requires at least **Java 11**

# Download

Maven:

```xml

<dependency>
    <groupId>io.github.zabuzard.maglev</groupId>
    <artifactId>maglev</artifactId>
    <version>1.2</version>
</dependency>
```

Jar downloads are available from the [release section](https://github.com/ZabuzaW/Maglev/releases).

# Documentation

* [API Javadoc](https://javadoc.io/doc/io.github.zabuzard.maglev/maglev)
  or alternatively from the [release section](https://github.com/ZabuzaW/Maglev/releases)

# Getting started

1. Integrate **Maglev** into your project. The API is contained in the module `io.github.zabuzard.maglev`.
2. Create an implementation of `Graph<N, E>` for your custom graphs or use `SimpleGraph<N, E>` for a quick start
3. Populate your graph
4. Create an algorithm using `ShortestPathComputationBuilder`
5. Execute shortest path computations using the methods offered by `ShortestPathComputation`

# Examples

Consider the following simple graph setup:

![Graph example](https://i.imgur.com/lumZoLj.png)

```java
SimpleGraph<Integer, SimpleEdge<Integer>> graph = new SimpleGraph<>();

graph.addNode(1);
graph.addNode(2);
graph.addNode(3);
graph.addNode(4);
graph.addNode(5);

graph.addEdge(new SimpleEdge<>(1, 2, 8));
graph.addEdge(new SimpleEdge<>(1, 3, 1));
graph.addEdge(new SimpleEdge<>(2, 5, 2));
graph.addEdge(new SimpleEdge<>(3, 4, 2));
graph.addEdge(new SimpleEdge<>(4, 2, 1));
graph.addEdge(new SimpleEdge<>(4, 5, 5));
```

Next, we create an algorithm by using the builder with default settings:

```java
var algo = new ShortestPathComputationBuilder<>(graph)
    .build();
var path = algo.shortestPath(1, 5);
System.out.println(path);
```

The algorithm correctly computes the shortest path from node `1` to `5` (as highlighted in the picture).

***

The next example demonstrates how to ignore node `4` in all computations:

```java
var algo = new ShortestPathComputationBuilder<>(graph)
    .addModuleIgnoreEdgeIf(edge -> edge.getDestination().equals(4))
    .build();
var path = algo.shortestPath(1, 5);
System.out.println(path);
```

***

The third example shows how to compute all reachable shortest path costs, starting from node `1`, but aborting as soon
as node `4` has been settled:

```java
var algo = new ShortestPathComputationBuilder<>(graph)
    .addModuleAbortAfterIf(dist -> dist.getNode().equals(4))
    .build();
var nodeToCost = algo.shortestPathCostsReachable(1);

nodeToCost.entrySet().stream()
    .map(entry -> entry.getKey() + "=" + entry.getValue().getPathCost())
    .forEach(System.out::println);
```

***

The last example uses ordinary Dijkstra without any modules or optimizations:

```java
var algo = new ShortestPathComputationBuilder<>(graph)
    .resetOrdinaryDijkstra()
    .build();
var path = algo.shortestPath(1, 5);
System.out.println(path);
```

***

## Advanced

Lastly, we show how to create an A-Star algorithm that uses
the [Euclidean distance](https://en.wikipedia.org/wiki/Euclidean_distance) (_as the crow flies_)
on a graph consisting of points in a 2-dimensional space.

Consider the following simple class for points in a 2-dimensional space

```java
class Point { 
    private final int x;
    private final int y;

    // constructor, getter, equals, hashCode and toString ommitted
}
```

Next, we define our heuristic metric

```java
class EuclideanDistance implements Metric<Point> {
    @Override
    public double distance(Point a, Point b) { 
        return Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }
}
```

Now a simple graph consisting of such points:

```java
SimpleGraph<Point, SimpleEdge<Point>> graph = new SimpleGraph<>();

var a = Point.of(1, 2);
var b = Point.of(5, 7);
var c = Point.of(-10, 4);

graph.add(a);
graph.add(b);
graph.add(c);

graph.addEdge(new SimpleEdge<>(a, b, 1));
graph.addEdge(new SimpleEdge<>(b, c, 1));
graph.addEdge(new SimpleEdge<>(a, c, 5));
```

and finally the algorithm operating on this graph by using A-Star with the Euclidean distance:

```java
var algo = new ShortestPathComputationBuilder<>(graph)
    .setMetric(new EuclideanDistance())
    .build();
var path = algo.shortestPath(a, c);
System.out.println(path);
```

***

# Graph

The algorithms operate on a generic `Graph` interface which must be implemented by a user.

To ease development, `AbstractGraph` can be used as base class. It implements almost all methods already, based on
abstract
`Map<N, Set<E>> getNodeToIncomingEdges()` and `Map<N, Set<E>> getNodeToOutgoingEdges`
methods which can easily be given by any implementing class.

Further, there is `SimpleGraph` which already provides a full implementation of the interface.

***

Graphs are defined on a generic node type `N`, no restrictions, and an `Edge<N>` interface. The class `SimpleEdge`
provides a full implementation of the interface.

# Builder

The algorithm builder `ShortestPathComputationBuilder` offers highly customizable algorithms based on _Dijkstra_,
called _Module-Dijkstra_. That is a regular Dijkstra algorithm which can be extended using extension modules that modify
its behavior. Offered modules are:

* `AbortBeforeIfModule` - Aborts further computation as soon as a node that matches a given predicate would be settled
* `AbortAfterIfModule` - Aborts further computation as soon as a node that matches a given predicate has been settled
* `AbortAfterRangeModule` - Only explores shortest paths up to the given range
* `IgnoreEdgeIfModule` - Ignores exploring edges that match the given predicate
* `AStarModule` - Optimization of the algorithm by utilizing a given heuristic metric

It is also possible to add custom modules by simply implementing the interface `DijkstraModule`. Modules can be added by
using `addModule(DijkstraModule)` and the other `addModuleXXX` methods.

***

If the `AStarModule` was chosen, a heuristic metric must be given. Offered metrics are:

* `LandmarkMetric` - Dynamic heuristic computed based on the underlying graph model (also known as _ALT_ algorithm)

It is also possible to use a custom metric by simply implementing the `Metric` interface, for example a metric based on
the _Euclidean distance_. A metric can be set by using `setMetric(Metric)` and the other `setMetricXXX` methods.

***

If the `LandmarkMetric` was chosen, the amount of landmarks must be set, which can be done
using `setAmountOfLandmarks(int)`. Additionally, a landmark provider must be given. Offered landmark providers are:

* `GreedyFarthestLandmarkProvider` - Chooses landmarks that are optimally spread across the graph by dynamically
  utilizing its structure
* `RandomLandmarkProvider` - Randomly selects nodes as landmarks

It is also possible to use a custom landmark provider by simply implementing the `LandmarkProvider` interface. A
landmark provider can be set by using `setLandmarkProvider(LandmarkProvider)`
and the other `setLandmarkProviderXXX` methods.

***

Finally, an algorithm using the selected properties can be created using `build()`. The initial construction might take
a while, depending on the graph size. Results are cached and further constructions will try to utilize the cache
whenever possible.

The **default configuration** of the builder is:

* `AStarModule`
* `LandmarkMetric`
* 20 landmarks
* `RandomLandmarkProvider`

The method `resetDefault()` can be used to restore the default settings. Likewise `resetOrdinaryDijkstra()` can be used
to get a configuration that just uses the ordinary Dijkstra algorithm without any modules.
