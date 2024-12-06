package dev.compactmods.crafting.test.gametests;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

public class CMTestStructures {

    public static final String ONE_CUBED = "1x1x1";
    public static final String FIVE_CUBED = "5x5x5";
    public static final String FIFTEEN_CUBED = "15x15x15";

    public static final String MEDIUM_GLASS_FILLED = CompactCrafting.MOD_ID + ":medium_glass_filled";
    public static final String MEDIUM_GLASS_WALLS = CompactCrafting.MOD_ID + ":medium_glass_walls";
    public static final String MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER = CompactCrafting.MOD_ID + ":medium_glass_walls_obsidian_center";

    @RegisterStructureTemplate(CompactCrafting.MOD_ID + ":empty_1_cubed")
    public static final StructureTemplate empty_1 = StructureTemplateBuilder.empty(1, 1, 1);

    @RegisterStructureTemplate(CompactCrafting.MOD_ID + ":empty_5_cubed")
    public static final StructureTemplate empty_5 = StructureTemplateBuilder.empty(5, 5, 5);

    @RegisterStructureTemplate(CompactCrafting.MOD_ID + ":empty_15_cubed")
    public static final StructureTemplate empty_15 = StructureTemplateBuilder.empty(15, 15, 15);

    @OnInit
    public static void init() {

    }
}
