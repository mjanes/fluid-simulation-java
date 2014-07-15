fluid-simulation
================

Teaching myself how to do a basic fluid simulation. Also my introduction to JavaFX and Java8.

Currently the simulation is 2D only, though the camera is equipped to do 3D.

Bear in mind I've never taken a physics class.

###TODO:
-   Allow different types of fluids/gasses/etc such as air and water. At the moment we have some constants that define 
    fluid behavior, make them come from the fluid entity, and the proportion of the fluid entity that is whatever type
    of material.
-   Allow phase transitions (This will probably depend upon the above, but changes those variables based on temperature.)
-   Ability to model surface tension in liquids
-   Fix issue that is preventing Rayleigh-Taylor and Kelvin-Helmholtz instabilities from arising. Or at least learn how
    to make them more visible, if they are.

###References:
-   http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
-   https://software.intel.com/sites/default/files/m/e/b/b/e/f/FluidsForGames_Pt1.pdf
-   http://http.developer.nvidia.com/GPUGems/gpugems_ch38.html