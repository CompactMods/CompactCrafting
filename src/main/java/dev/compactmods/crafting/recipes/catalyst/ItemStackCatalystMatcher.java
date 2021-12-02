package dev.compactmods.crafting.recipes.catalyst;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackCatalystMatcher extends ForgeRegistryEntry<CatalystType<?>>
        implements ICatalystMatcher, CatalystType<ItemStackCatalystMatcher> {

    public static final Codec<ItemStackCatalystMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(ItemStackCatalystMatcher::getItemId),
            CompoundNBT.CODEC.optionalFieldOf("nbt").forGetter(ItemStackCatalystMatcher::getNbtTag)
    ).apply(i, ItemStackCatalystMatcher::new));

    private final Predicate<ItemStack> nbtMatcher;

    private Optional<CompoundNBT> getNbtTag() {
        return Optional.of(new CompoundNBT());
    }

    private final Item item;

    public ItemStackCatalystMatcher() {
        this.item = null;
        this.nbtMatcher = (stack) -> true;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ItemStackCatalystMatcher(ResourceLocation item, Optional<CompoundNBT> nbt) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.nbtMatcher = buildMatcher(nbt.orElse(null));
    }

    public ItemStackCatalystMatcher(ItemStack stack) {
        this.item = stack.getItem();
        this.nbtMatcher = (s) -> true;
    }

    private Predicate<ItemStack> buildMatcher(CompoundNBT filter) {
        if(filter == null)
            return (stack) -> true;

        return (stack) -> {
            // filters defined but item has no nbt
            if(!stack.hasTag() && !filter.isEmpty()) return false;
            return tagMatched(stack.getTag(), filter);
        };
    }

    private boolean tagMatched(CompoundNBT node, CompoundNBT filter) {
        // filter: "mycompound: {}"
        if(filter.isEmpty()) return true;

        return filter.getAllKeys().stream().allMatch(key -> {
            if (!node.contains(key))
                return false;

            final byte tagType = filter.getTagType(key);
            if (tagType == Constants.NBT.TAG_COMPOUND) {
                // nested properties, recurse deeper
                return tagMatched(node.getCompound(key), filter.getCompound(key));
            } else {
                // all other key types are "primitives" - direct match time
                INBT primitive = node.get(key);
                if(primitive == null) return false;

                return primitive.equals(filter.get(key));
            }
        });
    }


    public ResourceLocation getItemId() {
        return item.getRegistryName();
    }

    public boolean matches(ItemStack stack) {
        return stack.getItem() == item && this.nbtMatcher.test(stack);
    }

    @Override
    public CatalystType<?> getType() {
        return Registration.ITEM_STACK_CATALYST.get();
    }

    @Override
    public Set<ItemStack> getPossible() {
        final HashSet<ItemStack> set = new HashSet<>();
        set.add(new ItemStack(item));
        return set;
    }

    @Override
    public Codec<ItemStackCatalystMatcher> getCodec() {
        return CODEC;
    }
}
