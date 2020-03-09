## JUNGRAPHT-VISUALIZATION: The [JUNG](http://jung.sourceforge.net) visualization and sample code modernized and ported to use [JGraphT](https://jgrapht.org) graphs and algorithms

[![Build Status](https://travis-ci.org/tomnelson/jungrapht-visualization.svg?branch=master)](https://travis-ci.org/tomnelson/jungrapht-visualization)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tomnelson/jungrapht-visualization/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tomnelson/jungrapht-visualization)
[![Snapshot](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.tomnelson/jungrapht-visualization.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/github/tomnelson/jungrapht-visualization/)
[![License](https://img.shields.io/badge/license-BSD3%202.1-blue.svg)](http://www.opensource.org/licenses/BSD-3-Clause)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)


[**JUNGRAPHT-VISUALIZATION Website**](http://tomnelson.github.io/jungrapht-visualization/)

JUNGRAPHT-VISUALIZATION can be used to render any of the following:
* org.jgrapht.Graph
* com.google.common.graph.Network
* com.google.common.graph.Graph

JUNGRAPHT-VISUALIZATION includes performance enhancements for visualization of large networks, including R*Tree for visualization, Barnes-Hut Quad Tree for force-directed layouts, and a lightweight rendering layer that can swap in while graphs are being animated or when they are zoomed out to a point where details are very small.
In the **ShowLayoutsWithJGraptIO** demonstration program, the lightweight rendering layer allows fast enough rendering that the performance impact of the Barnes Hut Quad Tree for the force directed layout becomes obvious.

Many rendering features may be set via java properties (see [sample.jungrapht.properties](https://github.com/tomnelson/jungrapht-visualization/blob/master/jungrapht-visualization/src/main/resources/sample.jungrapht.properties) for keys and default values). 

JUNGRAPHT-VISUALIZATION includes improved layout algorithms for directed graphs and Trees, including the 
TidierTreeLayoutAlgorithm and the SugiyamaLayoutAlgorithm. All TreeLayoutAlgorithms will make a best attempt to draw any directed graph 
(by ignoring cycles and reversing feedback edges) for which one or more Root vertices can be determined. 
There are Vertex/Edge Predicates and Vertex/Edge Comparators that are used to coerce out the desired tree structure based on a user-defined procedure to find roots and follow the desired path. 
All TreeLayoutAlgorithms, including the TidierTreeLayoutAlgorithm, will draw either single or multiple rooted 'forest' graphs. CircleLayout has been improved with an option to reduce edge crossing.

Graph using TidierTreeLayoutAlgorithm

![Image TidierTree](images/tidiertree.jpg)

Graph using SugiyamaLayoutAlgorithm

![Image SugiyamaLayout](images/sugiyama2.jpg)

Graph using Eiglsperger Optimization of SugiyamaLayoutAlgorithm

![Image SugiyamaLayout](images/eiglspergersugiyama.jpg)

Graph using CircleLayoutAlgorithm

![Image CircleLayout](images/CircleLayout.jpg)

Same graph using CircleLayoutAlgorithm with reduced edge crossing

![Image ReducedEdgeCrossing](images/ReducedEdgeCrossingCircleLayout.jpg)

### Latest Release


The most recent version of JUNGRAPHT-VISUALIZATION is [version 1.0-RC7](https://github.com/tomnelson/jungrapht-visualization/releases/tag/v1.0_RC7), released 10 March 2020.
*   [Javadoc](http://tomnelson.github.io/jungrapht-visualization/javadoc/index.html)
*   [Maven Search Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.tomnelson%22%20AND%20v%3A%221.0-RC7%22%20AND%20(a%3A%22jungrapht-visualization%22%20OR%20a%3A%22jungrapht-visualization-samples%22))
    *   `jungrapht-visualization`: [jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization/1.0-RC7/jungrapht-visualization-1.0-RC7.jar), [source jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization/1.0-RC7/jungrapht-visualization-1.0-RC7-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization/1.0-RC7/jungrapht-visualization-1.0-RC7-javadoc.jar)
    *   `jungrapht-visualization-samples`: [jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization-samples/1.0-RC7/jungrapht-visualization-samples-1.0-RC7.jar), [source jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization-samples/1.0-RC7/jungrapht-visualization-samples-1.0-RC7-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization-samples/1.0-RC7/jungrapht-visualization-samples-1.0-RC7-javadoc.jar)

To add a dependency on this release of JUNGRAPHT-VISUALIZATION using Maven, use the following:

```xml
<dependency>
  <groupId>com.github.tomnelson</groupId>
  <artifactId>jungrapht-visualization</artifactId>
  <version>1.0-RC7</version>
</dependency>
```

### Snapshots

Snapshots of JUNGRAPHT-VISUALIZATION built from the `master` branch are available through Maven using version `1.0-SNAPSHOT`.

### Links

* [GitHub project](https://github.com/tomnelson/jungrapht-visualization)
* [Issue tracker: report a defect or make a feature request](https://github.com/tomnelson/jungrapht-visualization/issues/new)
* [StackOverflow: Ask questions](https://stackoverflow.com/questions/ask?tags=jungrapht+java)

