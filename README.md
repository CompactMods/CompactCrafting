Compact Crafting
================

This mod is based off of the Miniaturization Crafting mechanic from Compact Machines pre-1.13. It's still
a work in progress, but most of the major features are in place and operational. Here is a summary:

## Feature: Data-driven Recipes
All the recipes are loaded from the `data/<container>/miniaturization` folder. They are loaded as unique recipes;
see the Wiki for some examples, or click [here](recipes/diamond_block.json) for a full example file.

## Feature: Recipe layers
Previously, the crafting mechanic forced a designer to be incredibly explicit about how recipe layers were defined. Given
that this means a lot more work on a designer rather than letting the mod itself "figure it out," it meant that custom recipes
felt like a second-class citizen to the mod. In Compact Crafting, this system was overhauled to make layer definitions 
more streamlined.

Take a look at the wiki's Recipe Layer Specification to see more information.

## Feature: Proxy Blocks
To be added in a future release. Allows for better automation of the miniaturization field.
