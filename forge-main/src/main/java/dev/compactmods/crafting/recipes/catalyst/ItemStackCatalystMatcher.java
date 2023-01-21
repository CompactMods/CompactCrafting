package dev.compactmods.crafting.recipes.catalyst;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.core.CCCatalystTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemStackCatalystMatcher implements ICatalystMatcher, CatalystType<ItemStackCatalystMatcher> {

    public static final Codec<ItemStackCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter((x) -> ForgeRegistries.ITEMS.getKey(x.item)),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ItemStackCatalystMatcher::getNbtTag)
    ).apply(i, ItemStackCatalystMatcher::new));

    private final Predicate<ItemStack> nbtMatcher;

    private final Item item;
    private final CompoundTag nbt;

    public ItemStackCatalystMatcher() {
        this.item = null;
        this.nbtMatcher = (stack) -> true;
        this.nbt = null;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ItemStackCatalystMatcher(ResourceLocation item, Optional<CompoundTag> nbt) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.nbtMatcher = buildMatcher(nbt.orElse(null));
        this.nbt = nbt.orElse(null);
    }

    public ItemStackCatalystMatcher(ItemStack stack) {
        this.item = stack.getItem();
        this.nbtMatcher = buildMatcher(stack.getOrCreateTag());
        this.nbt = stack.getTag();
    }

    private Predicate<ItemStack> buildMatcher(CompoundTag filter) {
        if(filter == null)
            return (stack) -> true;

        return (stack) -> {
            // filters defined but item has no nbt
            if(!stack.hasTag() && !filter.isEmpty()) return false;
            final var tag = stack.getTag();
            Objects.requireNonNull(tag);
            return NbtUtils.compareNbt(tag, filter, true);
        };
    }

    public boolean matches(ItemStack stack) {
        return stack.getItem().equals(item) && this.nbtMatcher.test(stack);
    }

    private Optional<CompoundTag> getNbtTag() {
        return Optional.ofNullable(nbt);
    }

    @Override
    public CatalystType<?> getType() {
        return CCCatalystTypes.ITEM_STACK_CATALYST.get();
    }

    @Override
    public Set<ItemStack> getPossible() {
        if(nbt == null)
            return Set.of(new ItemStack(item));

        final var withNbt = new ItemStack(item, 1);
        withNbt.setTag(nbt);
        return Set.of(withNbt);
    }

    @Override
    public Codec<ItemStackCatalystMatcher> getCodec() {
        return CODEC;
    }
}