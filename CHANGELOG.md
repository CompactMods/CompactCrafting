## 1.0.0-beta.3 [pending 2021.06.11]
- Introduced a new component type, `compactcrafting:empty`. It matches all forms of air.
- Made recipe loading more defensive. If a layer defines a component key and said component isn't defined in the recipe itself, the recipe will register an `empty` component for the layer to use.
- Solved some issues with recipe layer matching.
- Fixed issue where projectors didn't fully respect player rotation, making them awkward to place in tight spaces. They now just use the player's horizontal direction, making them much easier to place.
- Made JEI rendering more defensive. Rendering now uses a fake world to apply tile entity data and model data to block rendering. Mods with issues are now caught and logged accordingly, and skip rendering these blocks in the preview.
- Fixed an issue with field block checking and deletion. Solves blocks SE of an active field getting deleted erroneously.

## 1.0.0-beta.2 & 1.0.0-beta.1 [2021.04.04]
- Added loot table generation, fixes issue where projectors wouldn't drop themselves when broken.
- Migrated data serialization to codecs.
- Separated main projector and dummy projector tile entities. Makes it so only one of the four projectors ticks, improving performance.
- Initial unit tests for data loading logic.
- Introduction of recipe component system. (`compactcrafting:block`)