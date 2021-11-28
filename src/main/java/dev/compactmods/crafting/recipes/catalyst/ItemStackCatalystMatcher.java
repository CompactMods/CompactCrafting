package dev.compactmods.crafting.recipes.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackCatalystMatcher extends ForgeRegistryEntry<CatalystType<?>>
        implements ICatalystMatcher, CatalystType<ItemStackCatalystMatcher> {

    public static final Codec<ItemStackCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(ItemStackCatalystMatcher::getItemId)
    ).apply(i, ItemStackCatalystMatcher::new));

    private final Item item;

    public ItemStackCatalystMatcher() {
        this.item = null;
    }

    public ItemStackCatalystMatcher(ResourceLocation item) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
    }

    public ResourceLocation getItemId() {
        return item.getRegistryName();
    }

    // TODO - Expand
    public boolean matches(ItemStack stack) {
        return stack.getItem() == item;
    }

    @Override
    public CatalystType<?> getType() {
        return Registration.ITEM_STACK_CATALYST.get();
    }

    @Override
    public Codec<ItemStackCatalystMatcher> getCodec() {
        return CODEC;
    }
}
