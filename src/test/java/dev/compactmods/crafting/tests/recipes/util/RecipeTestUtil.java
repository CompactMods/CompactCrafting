package dev.compactmods.crafting.tests.recipes.util;

import java.util.Map;
import java.util.Optional;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.junit.jupiter.api.Assertions;

public class RecipeTestUtil {
    public static MiniaturizationRecipe getRecipeFromFile(String filename) {
        JsonElement json = FileHelper.INSTANCE.getJsonFromFile(filename);

        Optional<MiniaturizationRecipe> loaded = MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(CompactCrafting.LOGGER::info);

        if (!loaded.isPresent()) {
            Assertions.fail("Recipe did not load from file.");
            return null;
        } else {
            final MiniaturizationRecipe rec = loaded.get();
            rec.setId(new ResourceLocation("compactcrafting", Files.getNameWithoutExtension(filename)));
            return rec;
        }
    }

    public static MiniaturizationRecipeComponents getComponentsFromRecipeFile(String filename) {
        final JsonElement data = FileHelper.INSTANCE.getJsonFromFile(filename);


        final Map<String, BlockComponent> blocks = Codec.unboundedMap(Codec.STRING, BlockComponent.CODEC)
                .fieldOf("components")
                .codec()
                .parse(JsonOps.INSTANCE, data)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        blocks.forEach(components::registerBlock);

        return components;
    }

    public static AxisAlignedBB getFieldBounds(MiniaturizationFieldSize fieldSize, IntegrationTestHelper helper) {
        return fieldSize.getBoundsAtOrigin().move(helper.relativePos(BlockPos.ZERO).orElse(BlockPos.ZERO));
    }

    public static AxisAlignedBB getFloorLayerBounds(MiniaturizationFieldSize fieldSize, IntegrationTestHelper helper) {
        return BlockSpaceUtil.getLayerBounds(getFieldBounds(fieldSize, helper), 0);
    }

    public static void loadStructureIntoTestArea(IntegrationTestHelper test, ResourceLocation structure, BlockPos location) {
        final TemplateManager structures = test.getWorld().getStructureManager();
        final Template ender = structures.get(structure);
        if(ender == null)
            return;

        ender.placeInWorld(test.getWorld(), test.getOrigin().offset(location), new PlacementSettings(), test.getWorld().getRandom());
    }
}
