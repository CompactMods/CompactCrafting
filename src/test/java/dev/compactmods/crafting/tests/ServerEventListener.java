package dev.compactmods.crafting.tests;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
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

        hackyRecipeLoad(server);
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
