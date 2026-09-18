package com.github.drakescraft_labs.gcereborn.items.machines;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.libraries.dough.inventory.InvUtils;
import com.github.drakescraft_labs.slimefun4.libraries.dough.items.ItemUtils;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.SlimefunItem.abstractItems.MachineRecipe;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenu;

import com.github.drakescraft_labs.gcereborn.GeneticChickengineering;
import com.github.drakescraft_labs.gcereborn.core.genetics.DNA;
import com.github.drakescraft_labs.gcereborn.items.GCEItems;
import com.github.drakescraft_labs.gcereborn.items.chicken.ExpandedChickenSpecies;
import com.github.drakescraft_labs.gcereborn.utils.ChickenUtils;
import com.github.drakescraft_labs.gcereborn.utils.PocketChickenData;

public class MutagenicSplicer extends AbstractMachine {

    private static final int[] BORDER = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 31, 36, 37, 38, 39, 40, 41, 42, 43, 44 };
    private static final int[] BORDER_IN = { 9, 10, 11, 12, 18, 27, 28, 29, 30 };
    private static final int[] BORDER_OUT = { 14, 15, 16, 17, 23, 26, 32, 33, 34, 35 };
    private static final int[] INPUT_SLOTS = { 19, 20, 21 };
    private static final int[] OUTPUT_SLOTS = { 24, 25 };

    public MutagenicSplicer(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    @Nonnull
    public ItemStack getProgressBar() {
        return new ItemStack(Material.ENCHANTING_TABLE);
    }

    @Override
    public int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    protected void constructMenu(@Nonnull com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenuPreset preset) {
        preset.setSize(45);
        for (int i : BORDER) {
            preset.addItem(i, com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getBackground(), com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : BORDER_IN) {
            preset.addItem(i, com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getInputSlotTexture(), com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : BORDER_OUT) {
            preset.addItem(i, com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getOutputSlotTexture(), com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(INFO_SLOT, new com.github.drakescraft_labs.slimefun4.libraries.dough.items.CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "), com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getEmptyClickHandler());

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, com.github.drakescraft_labs.slimefun4.utils.ChestMenuUtils.getDefaultOutputHandler());
        }
    }

    @Override
    protected void tick(@Nonnull Block b) {
        super.tick(b);
        if (GeneticChickengineering.getConfigService().isParticlesEnabled() && Math.random() < 0.25) {
            b.getWorld().spawnParticle(Particle.PORTAL, b.getLocation().add(0.5, 0.9, 0.5), 6, 0.2, 0.2, 0.2, 0.1);
        }
    }

    @Override
    @Nullable
    protected MachineRecipe findNextRecipe(@Nonnull BlockMenu menu) {
        ItemStack chickenItem = null;
        ItemStack serumItem = null;
        ItemStack catalystItem = null;
        ExpandedChickenSpecies matchedSpecies = null;
        int chickenSlot = -1;
        int serumSlot = -1;
        int catalystSlot = -1;

        for (int slot : getInputSlots()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (ChickenUtils.isPocketChicken(item) && ChickenUtils.isAdult(item) && chickenItem == null) {
                chickenItem = item;
                chickenSlot = slot;
            } else if (item.isSimilar(GCEItems.MUTAGENIC_SERUM) && serumItem == null) {
                serumItem = item;
                serumSlot = slot;
            } else if (catalystItem == null) {
                ExpandedChickenSpecies s = ExpandedChickenSpecies.matchCatalyst(item);
                if (s != null) {
                    catalystItem = item;
                    catalystSlot = slot;
                    matchedSpecies = s;
                }
            }
        }

        if (chickenItem == null || serumItem == null || catalystItem == null || matchedSpecies == null) {
            return null;
        }

        PocketChickenData data = PocketChickenData.fromItem(chickenItem);
        if (data == null) {
            return null;
        }

        // Mutate the chicken into the new expanded species
        ItemStack mutated = chickenItem.clone();
        mutated.setAmount(1);
        DNA originalDna = data.getDNA();
        JsonObject json = data.getAdapter() != null ? data.getAdapter().deepCopy() : new JsonObject();
        json.addProperty("baby", false);
        json.addProperty("_health", 4d);

        ChickenUtils.setExpandedPocketChicken(mutated, json, originalDna, matchedSpecies);

        MachineRecipe recipe = new MachineRecipe(
            GeneticChickengineering.getConfigService().isTest() ? 1 : 30,
            new ItemStack[] {chickenItem, serumItem, catalystItem},
            new ItemStack[] {mutated}
        );

        if (!InvUtils.fitAll(menu.toInventory(), recipe.getOutput(), getOutputSlots())) {
            return null;
        }

        menu.consumeItem(chickenSlot, 1);
        menu.consumeItem(serumSlot, 1);
        menu.consumeItem(catalystSlot, 1);

        if (GeneticChickengineering.getConfigService().isSoundsEnabled()) {
            GeneticChickengineering.getScheduler().run(() ->
                menu.getBlock().getWorld().playSound(menu.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.8f)
            );
        }

        return recipe;
    }
}
