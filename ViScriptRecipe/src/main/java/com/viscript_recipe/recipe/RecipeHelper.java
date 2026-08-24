package com.viscript_recipe.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

// 把重复使用的服务端侧的方法统一放到这里
public class RecipeHelper {

    public static Item itemFromRegistry(String id, Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return fallback;
        }
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }

    public static ItemStack registryItem(String id, Item fallback) {
        return new ItemStack(itemFromRegistry(id, fallback));
    }

    public static ItemStack itemFromAbility(String itemAbility) {
        return switch (itemAbility) {
            case "axe_dig", "axe_strip" -> new ItemStack(Items.IRON_AXE);
            case "shovel_dig" -> new ItemStack(Items.IRON_SHOVEL);
            case "pickaxe_dig" -> new ItemStack(Items.IRON_PICKAXE);
            case "sword_dig" -> new ItemStack(Items.IRON_SWORD);
            case "shears_dig" -> new ItemStack(Items.SHEARS);
            default -> new ItemStack(itemFromRegistry("farmersdelight:iron_knife", Items.IRON_SWORD));
        };
    }

    public static Fluid fluidFromRegistry(String id, Fluid fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return fallback;
        }
        var fluid = BuiltInRegistries.FLUID.get(location);
        return fluid == Fluids.EMPTY ? fallback : fluid;
    }

    public static ItemStack[] itemsFromTag(ResourceLocation tag) {
        if (tag == null) {
            return new ItemStack[0];
        }
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag))
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .filter(item -> item != Items.AIR)
                        .map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new))
                .orElseGet(() -> new ItemStack[0]);
    }

    public static FluidStack[] fluidsFromTag(ResourceLocation tag, int amount) {
        if (tag == null) return new FluidStack[0];
        return BuiltInRegistries.FLUID.getTag(TagKey.create(Registries.FLUID, tag))
                .map(holders -> displayFluidsFromHolders(holders.stream()
                        .map(Holder::value)
                        .toList(), Math.max(1, amount)))
                .orElseGet(() -> new FluidStack[0]);
    }

    private static FluidStack[] displayFluidsFromHolders(List<Fluid> fluids, int amount) {
        var sourceFluids = fluids.stream().filter(fluid -> fluid.defaultFluidState().isSource()).toList();
        var displayFluids = sourceFluids.isEmpty() ? fluids : sourceFluids;
        return displayFluids.stream()
                .map(fluid -> new FluidStack(fluid, amount))
                .filter(stack -> !stack.isEmpty())
                .toArray(FluidStack[]::new);
    }
}
