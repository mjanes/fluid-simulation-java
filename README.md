fluid-simulation
================

Teaching myself how to do a basic fluid simulation. Also my introduction to JavaFX and Java8.

Currently the simulation is 2D only, though the camera is equipped to do 3D.

###TODO:
-   Allow different types of fluids/gasses/etc such as air and water. At the moment we have some constants that define 
    fluid behavior, make them come from the fluid entity, and the proportion of the fluid entity that is whatever type
    of material.
-   Allow phase transitions (This will probably depend upon the above, but changes those variables based on temperature.)
-   Improve handling of gravity, so that pressure is cumulative. Not important now as we're generally simulating small 
    areas, but if we want to be able to simulate atmospheres, or plate tectonics, we will need this.

###References:
-   http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
-   https://software.intel.com/sites/default/files/m/e/b/b/e/f/FluidsForGames_Pt1.pdf
-   http://http.developer.nvidia.com/GPUGems/gpugems_ch38.html