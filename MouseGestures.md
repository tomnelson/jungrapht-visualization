# Mouse Gestures for jungrapht-visualization

#####MouseWheel:
* turn the mouse wheel to scale (zoom) the visualization at the mouse position
* CTRL+MouseWheel to scale (zoom) the visualization in the X-Axis only
* ALT+MouseWheel to scale (zoom) the visualization in the Y-Axis only

* In a Lens, the mouse wheel, when the cursor is inside the Lens, will change the Lens magnification
###DefaultGraphMouse:

#####Mouse Button One:
* press and drag to pan the graph visualization
* CTRL+MouseButtonOne press over a Vertex or Edge to select a vertex or edge
* SHIFT+CTRL+MouseButtonOne press to add another Vertex or Edge to the selection
    ###### Rectangular Selection Mode (normal):
* CTRL+MouseButtonOne press not over a Vertex or Edge to clear selection
* CTRL+MouseButtonOne press and drag to define a rectangle. Vertices inside are selected.
* SHIFT+CTRL+MouseButtonOne press and drag to define a rectangle. Vertices inside are added to the selection.
    ###### Arbitrary Selection Mode:
* CTRL+MouseButtonOne press and drag to define an arbitrary shape. Vertices inside are selected.
* SHIFT+CTRL+MouseButtonOne press and drag to define an arbitrary shape. Vertices inside are added to the selection.
* Arbitrary selection mode is selected with the property:

     `jungrapht.arbitraryShapeSelection=true`
     
          or by calling:

    `graphMouse.setMultiSelectionStrategy(MultiSelectionStrategy.arbitrary());`

###ModalGraphMouse:

######TransformingMode:
#####Mouse Button One:
* press and drag to pan the graph visualization
* SHIFT+MouseButtonOne and drag to rotate the visualization about the center
* CTRL+MouseButtonOne and drag to shear the visualization about the center

######PickingMode:
#####Mouse Button One:
* CTRL+MouseButtonOne press over a Vertex or Edge to select a vertex or edge
* SHIFT+CTRL+MouseButtonOne press to add another Vertex or Edge to the selection
    ###### Rectangular Selection Mode (normal):
* CTRL+MouseButtonOne press not over a Vertex or Edge to clear selection
* CTRL+MouseButtonOne press and drag to define a rectangle. Vertices inside are selected.
* SHIFT+CTRL+MouseButtonOne press and drag to define a rectangle. Vertices inside are added to the selection.
    ###### Arbitrary Selection Mode:
* CTRL+MouseButtonOne press and drag to define an arbitrary shape. Vertices inside are selected.
* SHIFT+CTRL+MouseButtonOne press and drag to define an arbitrary shape. Vertices inside are added to the selection.
* Arbitrary selection mode is selected with the property:



     `jungrapht.arbitraryShapeSelection=true`
     
          or by calling:

    `graphMouse.setMultiSelectionStrategy(MultiSelectionStrategy.arbitrary());`

