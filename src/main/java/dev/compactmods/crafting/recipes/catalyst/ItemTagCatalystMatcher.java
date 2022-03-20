package dev.compactmods.crafting.recipes.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.core.CCCatalystTypes;
import net.minecraft.core.Registry;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemTagCatalystMatcher extends ForgeRegistryEntry<CatalystType<?>>
        implements ICatalystMatcher, CatalystType<ItemTagCatalystMatcher> {

    private static final Codec<ItemTagCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            TagKey.codec(Registry.ITEM_REGISTRY).fieldOf("tag").forGetter((is) -> is.tag)
    ).apply(i, ItemTagCatalystMatcher::new));

    private final TagKey<Item> tag;
    private final ITag<Item> tag2;

    public ItemTagCatalystMatcher() {
        this.tag = null;
        this.tag2 = null;
    }

    public ItemTagCatalystMatcher(TagKey<Item> tag) {
        this.tag = tag;
        final var it = ForgeRegistries.ITEMS.tags();
        this.tag2 = it.getTag(tag);
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (tag == null) return true;
        return tag2.contains(stack.getItem());
    }

    @Override
    public CatalystType<?> getType() {
        return CCCatalystTypes.TAGGED_ITEM_CATALYST.get();
    }

    @Override
    public Set<ItemStack> getPossible() {
        if (tag == null)
            return Collections.emptySet();

        return tag2.stream()
                .map(ItemStack::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Codec<ItemTagCatalystMatcher> getCodec() {
        return CODEC;
    }
}
