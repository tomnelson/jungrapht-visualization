## LayoutAlgorithm and LayoutModel

DefaultLayoutModel extends AbstractLayoutModel implements LayoutModel

The LayoutModel holds a mapping of the Graph vertices to layout area Points.
The LayoutModel has a horizontal and vertical size to form a rectangle.

The LayoutAlgorithm implementations contain an algorithm to compute the Graph vertex locations.

The superclass of the DefaultLayoutModel (AbstractLayoutModel) fires events at various stages.

* Layout model accepts layout algorithm
* If there is a previous layout algorithm thread running, stop it
* Layout model sets its size back to its ‘original’ preferred size
* Fire an event to say that the layout model is now ‘busy’ (this allows the RTree spatial data structures to pause rebuilding)
* Enable the LayoutVertexPositionChange service (spatial RTrees are currently inactive)
* Fire an event to say that the model has changed (will cause repaint)
* Tell the LayoutAlgorithm to visit this LayoutModel so it can start setting points
* If the LayoutAlgorithm is an iterative one, set up the VisRunnable on a thread to call the step methods. 
   * This stage is called ‘relaxing’ the layout algorithm
   * The LayoutAlgorithm 'step' method is called repeatedly
* If the LayoutAlgorithm is not threaded fire an event to say that the LayoutModel is not busy and is finished.

VisRunnable in new Thread will do the following:
* Stop any active VisRunnable threads
* Fire an event to say the layout model is busy (no spatial RTree updates)
* pause visual updates to 'pre-relax' layout algorithm (to get it to a better starting state visually):
   * Fire an event to turn off model changes (no repaints)
   * Prerelax the layout algorithm (makes a series of 'step' calls that are not animated in the view
   * Fire an event to turn on model changes
* Run the VisRunnable in a new thread
* When the VisRUnnable thread completes, fire an event to cause a repaint
* Fire an event to say that the LayoutModel is no longer busy (so RTrees can update)


jungrapht-layout is designed so that it can be used independently of the jungrapht-visualization system. It uses no java.awt classses so it is not tied to any particular visualization framework.
Here is an example of how to use classes in the jungrapht-layout jar to apply a layout algorithm to a graph, then access the layout points. 

    Graph<String, Integer> graph =     // make your graph

    TidierTreeLayoutAlgorithm<String, Integer> layoutAlgorithm =
            TidierTreeLayoutAlgorithm.<String, Integer>edgeAwareBuilder().expandLayout(false).build();
    LayoutModel<String> layoutModel = LayoutModel.<String>builder().size(100, 100).graph(graph).build();
    layoutAlgorithm.visit(layoutModel);

    System.out.println("points are "+layoutModel.getLocations());
    
If you need the edge articulations (where they bend) for a layout like Sugiyama, you can get the articulation points for each edge using the layout's edgeArticulationFunction

If you choose a layout algorithm that is threaded, you will not get your layout points immediately. Either make the layout not-threaded or wait for it to finish.
