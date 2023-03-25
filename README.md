Compact Crafting
================

<p style="text-align: center;">
  <a href="https://discord.gg/abca3pDPvu">
    <img src="https://img.shields.io/discord/765363477186740234?label=Discord&amp;logo=discord&amp;logoColor=white&amp;style=for-the-badge" alt="Discord" />
  </a>
</p>

This mod is based off of the Miniaturization Crafting mechanic from Compact 
Machines pre-1.13. Here is a summary of features:

## Feature: Data-driven Recipes
All the recipes are loaded from the `data/<container>/recipes` folder, the 
same as any other datapack-driven recipe. [See the wiki][RecipeSpec] for 
some examples, or click [here](recipes/diamond_block.json) for a full example 
file.

## Feature: Recipe layers
Previously, the crafting mechanic forced a designer to be incredibly explicit 
about how recipe layers were defined. Given that this means a lot more work 
on a designer rather than letting the mod itself "figure it out," it meant 
that custom recipes felt like a second-class citizen to the mod. In Compact 
Crafting, this system was overhauled to make layer definitions more streamlined.

Take a look at the wiki's [Recipe Layer Specification][RecipeLayerSpec] to see 
more information.

## Feature: Proxy Blocks
Allows for better automation of the miniaturization field.

[RecipeSpec]: https://github.com/CompactMods/CompactCrafting/wiki/Recipe-Specification
[RecipeLayerSpec]: https://github.com/CompactMods/CompactCrafting/wiki/Recipe-Layer-Specification
