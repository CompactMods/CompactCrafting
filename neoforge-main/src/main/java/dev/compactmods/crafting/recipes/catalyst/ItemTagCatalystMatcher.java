package dev.compactmods.crafting.recipes.catalyst;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.core.CCCatalystTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class ItemTagCatalystMatcher implements ICatalystMatcher, CatalystType<ItemTagCatalystMatcher> {

    private static final Codec<ItemTagCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter((is) -> is.tag)
    ).apply(i, ItemTagCatalystMatcher::new));

    private final TagKey<Item> tag;

    public ItemTagCatalystMatcher() {
        this.tag = null;
    }

    public ItemTagCatalystMatcher(TagKey<Item> tag) {
        this.tag = tag;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (tag == null) return true;
        return stack.is(tag);
    }

    @Override
    public CatalystType<?> getType() {
        return CCCatalystTypes.TAGGED_ITEM_CATALYST.get();
    }

    @Override
    public Set<ItemStack> getPossible() {
        if (tag == null)
            return Collections.emptySet();

        var list = BuiltInRegistries.ITEM.getTag(tag)
                .map(holders -> holders.stream().map(Holder::value).map(ItemStack::new).toList())
                .orElseGet(ArrayList::new);

        return ImmutableSet.copyOf(list);
    }

    @Override
    public Codec<ItemTagCatalystMatcher> getCodec() {
        return CODEC;
    }
}
