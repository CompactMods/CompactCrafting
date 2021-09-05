package dev.compactmods.crafting.recipes.components;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SimpleRecipeComponentType<C extends IRecipeComponent>
        extends ForgeRegistryEntry<RecipeComponentType<?>>
        implements RecipeComponentType<C> {

    private final Codec<C> s;

    public SimpleRecipeComponentType(Codec<C> comp) {
        this.s = comp;
    }

    @Override
    public Codec<C> getCodec() {
        return this.s;
    }
}
