package com.robotgryphon.compactcrafting.core;

import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.FilledLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.RecipeLayerSerializer;
import net.minecraftforge.fml.RegistryObject;

public class LayerSerializationRegistration {

    // ================================================================================================================
    //   CORE RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerSerializer<?>> FILLED_LAYER_SERIALIZER = Registration.RECIPE_LAYERS.register("filled", FilledLayerSerializer::new);

    public static void init() {}
}
