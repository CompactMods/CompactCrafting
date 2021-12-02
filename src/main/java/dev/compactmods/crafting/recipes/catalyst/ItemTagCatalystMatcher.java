package dev.compactmods.crafting.recipes.catalyst;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemTagCatalystMatcher extends ForgeRegistryEntry<CatalystType<?>>
        implements ICatalystMatcher, CatalystType<ItemTagCatalystMatcher> {

    private static final Codec<ItemTagCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            ITag.codec(() -> TagCollectionManager.getInstance().getItems())
                    .fieldOf("tag").forGetter((is) -> is.tag)
    ).apply(i, ItemTagCatalystMatcher::new));

    private final ITag<Item> tag;

    public ItemTagCatalystMatcher() {
        this.tag = null;
    }

    public ItemTagCatalystMatcher(ITag<Item> tag) {
        this.tag = tag;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem().is(tag);
    }

    @Override
    public CatalystType<?> getType() {
        return Registration.TAGGED_ITEM_CATALYST.get();
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
