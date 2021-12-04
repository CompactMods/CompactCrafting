package dev.compactmods.crafting.recipes.catalyst;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.core.CCCatalystTypes;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemTagCatalystMatcher extends ForgeRegistryEntry<CatalystType<?>>
        implements ICatalystMatcher, CatalystType<ItemTagCatalystMatcher> {

    private static final Codec<ItemTagCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY))
                    .fieldOf("tag").forGetter((is) -> is.tag)
    ).apply(i, ItemTagCatalystMatcher::new));

    private final Tag<Item> tag;

    public ItemTagCatalystMatcher() {
        this.tag = null;
    }

    public ItemTagCatalystMatcher(Tag<Item> tag) {
        this.tag = tag;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem().getTags().contains(tag);
    }

    @Override
    public CatalystType<?> getType() {
        return CCCatalystTypes.TAGGED_ITEM_CATALYST.get();
    }

    @Override
    public Set<ItemStack> getPossible() {
        if(tag == null)
            return Collections.emptySet();

        return tag.getValues().stream()
                .map(ItemStack::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Codec<ItemTagCatalystMatcher> getCodec() {
        return CODEC;
    }
}
