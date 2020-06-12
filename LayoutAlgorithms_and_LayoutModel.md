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
