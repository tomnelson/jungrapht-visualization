## JUNGRAPHT-VISUALIZATION: The [JUNG](http://jung.sourceforge.net) visualization and sample code modernized and ported to use [JGraphT](https://jgrapht.org) graphs and algorithms

[![Build Status](https://travis-ci.org/tomnelson/jungrapht-visualization.svg?branch=master)](https://travis-ci.org/tomnelson/jungrapht-visualization)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tomnelson/jungrapht-visualization/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tomnelson/jungrapht-visualization)


[**JUNGRAPHT-VISUALIZATION Website**](http://tomnelson.github.io/jungrapht-visualization/)

JUNGRAPHT-VISUALIZATION includes performance enhancements for visualization of large networks, including R*Tree for visualization, Barnes-Hut Quad Tree for force-directed layouts, and a lightweight rendering layer that can swap in while graphs are being animated or when they are zoomed out to a point where details are very small.
Many values may be set via java properties (see [sample.jungrapht.properties](https://github.com/tomnelson/jungrapht-visualization/blob/master/jungrapht-visualization/src/main/resources/sample.jungrapht.properties) for keys and default values).

### Latest Release

The most recent version of JUNGRAPHT-VISUALIZATION is [version 1.0-RC1](https://github.com/tomnelson/jungrapht-visualization/releases/tag/v1.0_RC1), released 1 September 2019.
*   [Javadoc](http://tomnelson.github.io/jungrapht-visualization/javadoc/index.html)
*   [Maven Search Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.tomnelson%22%20AND%20v%3A%221.0-RC1%22%20AND%20(a%3A%22jungrapht-visualization%22%20OR%20a%3A%22jungrapht-visualization-samples%22))
    *   `jungrapht-visualization`: [jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization/1.0-RC1/jungrapht-visualization-1.0-RC1.jar), [source jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization/1.0-RC1/jungrapht-visualization-1.0-RC1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization/1.0-RC1/jungrapht-visualization-1.0-RC1-javadoc.jar)
    *   `jungrapht-visualization-samples`: [jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization-samples/1.0-RC1/jungrapht-visualization-samples-1.0-RC1.jar), [source jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization-samples/1.0-RC1/jungrapht-visualization-samples-1.0-RC1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=com/github/tomnelson/jungrapht-visualization-samples/1.0-RC1/jungrapht-visualization-samples-1.0-RC1-javadoc.jar)

To add a dependency on this release of JUNGRAPHT-VISUALIZATION using Maven, use the following:

```xml
<dependency>
  <groupId>com.github.tomnelson</groupId>
  <artifactId>jungrapht-visualization</artifactId>
  <version>1.0-RC1</version>
</dependency>
```

### Snapshots

Snapshots of JUNGRAPHT-VISUALIZATION built from the `master` branch are available through Maven using version `1.0-SNAPSHOT`.

### Links

* [GitHub project](https://github.com/tomnelson/jungrapht-visualization)
* [Issue tracker: report a defect or make a feature request](https://github.com/tomnelson/jungrapht-visualization/issues/new)
* [StackOverflow: Ask questions](https://stackoverflow.com/questions/ask?tags=jungrapht+java)

