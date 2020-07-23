LayoutAlgorithm computes vertex locations.

LayoutModel accepts a LayoutAlgorithm and contains a Map of vertex to Point locations

VisualizationModel contains a LayoutModel and adds awt classes and event support.

VisualizationServer contains a VisualizationModel and contains affine transforms and
renderers to draw a graph.

VisualizationViewer extends VisualizationServer and adds mouse-driven event handling

