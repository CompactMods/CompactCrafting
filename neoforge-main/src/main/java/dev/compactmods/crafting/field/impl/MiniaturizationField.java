package dev.compactmods.crafting.field.impl;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import dev.compactmods.crafting.field.MiniaturizationFieldChangeListener;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.world.level.Level;

public class MiniaturizationField implements IMiniaturizationField {

    private final Level level;
    private final FieldProjectorPlacements projectors;
    private final MiniaturizationFieldLocation location;
    private final MiniaturizationFieldAccess fieldAccess;

    final MiniaturizationFieldChangeListener changeListener;
    private boolean areaLoaded;

    private final MiniaturizationFieldCraftingManager craftingManager;

    public MiniaturizationField(Level level, MiniaturizationFieldLocation location) {
        this.level = level;
        this.location = location;
        this.craftingManager = new MiniaturizationFieldCraftingManager(this);
        this.fieldAccess = new MiniaturizationFieldAccess(location, level);
        this.projectors = location.projectors();

        this.changeListener = new MiniaturizationFieldChangeListener(this);
    }

    @Override
    public Level level() {
        return this.level;
    }

    public MiniaturizationFieldLocation location() {
        return this.location;
    }

    public MiniaturizationFieldAccess fieldAccess() {
        return this.fieldAccess;
    }

    @Override
    public FieldProjectorPlacements getProjectors() {
        return this.projectors;
    }

    public void tick() {
        if (!areaLoaded)
            return;

        this.craftingManager.tick();
    }

    @Override
    public boolean isAreaLoaded() {
        return areaLoaded || level.isClientSide();
    }

    public void checkLoaded() {
        this.areaLoaded = level.isAreaLoaded(MathUtil.toBlockPosition(location.center()), location.size().getProjectorDistance() + 3);

        if (areaLoaded) {
//            listeners.forEach(l -> l.ifPresent(fl -> fl.onFieldActivated(this)));
        }
    }

    @Override
    public void fieldContentsChanged() {
        // clear the recipe immediately so people can't dupe items or break the projectors
        this.craftingManager.clearRecipe();
        var scheduled = this.craftingManager.scheduleScan();
    }

    public CraftingState craftingState() {
        return craftingManager.state();
    }

    // TODO: Reimplement with new game rules
//    public void handleDestabilize() {
//        if (craftingManager.getCraftingState() != EnumCraftingState.CRAFTING || craftingManager.matchedBlocks == null)
//            return;
//
//        if (level.isClientSide()) return;
//
//        // TODO - Look at dumping items into an inventory if it's attached to a projector, helps automation (in the weird cases)
//        boolean restoreBlocks = false;
//        boolean restoreCatalyst = false;
//        switch (ServerConfig.DESTABILIZE_HANDLING) {
//            case RESTORE_ALL:
//                restoreBlocks = true;
//                restoreCatalyst = true;
//                break;
//
//            case RESTORE_BLOCKS:
//            case DESTROY_CATALYST:
//                restoreBlocks = true;
//                break;
//
//            case RESTORE_CATALYST:
//            case DESTROY_BLOCKS:
//                restoreCatalyst = true;
//                break;
//
//            case DESTROY_ALL:
//                break;
//        }
//
//        if (restoreBlocks) {
//            AABB bounds = location.bounds();
//            BlockPos placeAt = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
//            // TODO - Check the const here, 2 may be wrong
//            matchedBlocks.placeInWorld((ServerLevelAccessor) level, placeAt, placeAt,
//                    new StructurePlaceSettings(), level.getRandom(), 2);
//        }
//
//        if (craftingManager.currentRecipe != null) {
//            final ItemPredicate catalyst = craftingManager.currentRecipe.value().catalystTest();
//            if (restoreCatalyst) {
//                final var spawnLoc = new Vector3d(location.center())
//                        .add(0, location.size().getRadius() + 1.5f, 0);
//
//                for (Item cat : matchedCatalysts) {
//                    final ItemEntity ie = new ItemEntity(level,
//                            spawnLoc.x, spawnLoc.y, spawnLoc.z,
//                            new ItemStack(cat));
//
//                    // ie.setNoGravity(true);
//
//                    level.addFreshEntity(ie);
//                }
//            }
//        }
//    }
}