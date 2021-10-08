package dev.compactmods.crafting.tests;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.setup.RecipeBase;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class ServerEventListener {

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent evt) {

        final MinecraftServer server = evt.getServer();
        // final File root = server.getServerDirectory().getParentFile();
        // server.getPackRepository().addPackFinder(new FolderPackFinder());

        final List<RecipeBase> miniBefore = server.getRecipeManager().getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE);

        // Add "test/resources" as a resource pack to the pack repository
        final ResourcePackList packs = server.getPackRepository();
        final FolderPackFinder testPack = new FolderPackFinder(new File(System.getenv("TEST_RESOURCES")), IPackNameDecorator.DEFAULT);
        packs.addPackFinder(testPack);
        packs.reload();

        // add "file/resources" to selected pack list
        final ImmutableSet<String> toSelect = ImmutableSet.<String>builder()
                .addAll(packs.getSelectedIds())
                .add("file/test_data")
                .build();

        packs.setSelected(toSelect);
        try {
            server.reloadResources(packs.getSelectedIds()).get();
        } catch (InterruptedException | ExecutionException e) {
            CompactCrafting.LOGGER.error("Failed to reload test resource packs.", e);
        }

        final List<RecipeBase> miniAfter = server.getRecipeManager().getAllRecipesFor(Registration.MINIATURIZATION_RECIPE_TYPE);
        // hackyRecipeLoad(server);
        CompactCrafting.LOGGER.debug("Added test miniaturization recipes.");
    }

    private static void hackyRecipeLoad(MinecraftServer server) {
        final RecipeManager recipeManager = server.overworld().getRecipeManager();
        final ImmutableMap<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = ObfuscationReflectionHelper
                .getPrivateValue(RecipeManager.class, recipeManager, "recipes");
        final HashMap<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> moreRecipes = new HashMap<>();

        final HashMap<ResourceLocation, IRecipe<?>> miniRecipes = new HashMap<>();
        final Path pf = new File(System.getenv("TEST_RESOURCES")).toPath();
        final Path testRecipes = pf.resolve(Paths.get("data", "compactcrafting", "recipes"));

        final File recipeDir = testRecipes.toFile();
        final String dp = recipeDir.getAbsolutePath();
        if (!recipeDir.exists())
            return;

        final File[] files = recipeDir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            final MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile(f.getAbsolutePath());
            if (recipe != null)
                miniRecipes.put(recipe.getRecipeIdentifier(), recipe);
        }

        moreRecipes.put(Registration.MINIATURIZATION_RECIPE_TYPE, miniRecipes);
        moreRecipes.putAll(recipes);

        ObfuscationReflectionHelper.setPrivateValue(RecipeManager.class, recipeManager, moreRecipes, "recipes");
    }

    @SubscribeEvent
    static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent evt) {
        final PlayerEntity player = evt.getPlayer();
        final MinecraftServer server = player.getServer();
        final PlayerList players = server.getPlayerList();
        final boolean op = players.isOp(player.getGameProfile());
        if (!op)
            players.op(player.getGameProfile());
    }
}
