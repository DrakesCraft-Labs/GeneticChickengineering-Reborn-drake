package com.github.drakescraft_labs.gcereborn.core.adapters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import org.bukkit.entity.Animals;
import org.junit.jupiter.api.Test;

class AnimalsAdapterTest {

    /**
     * Los ítems de prueba no serializan un animal, por lo que su adaptador es nulo.
     */
    @Test
    void applyLeavesDefaultEntityUntouchedWhenDataIsMissing() {
        AnimalsAdapter<Animals> adapter = new AnimalsAdapter<>(Animals.class);

        assertDoesNotThrow(() -> adapter.apply(null, null));
    }

    /**
     * Los datos de ejemplares antiguos pueden no incluir nombre personalizado.
     * Esa ausencia no debe intentar desreferenciar un JsonElement nulo.
     */
    @Test
    void loreAcceptsMissingCustomName() {
        JsonObject missingName = new JsonObject();
        JsonObject nullName = new JsonObject();
        nullName.add("_customName", JsonNull.INSTANCE);
        JsonObject named = new JsonObject();
        named.addProperty("_customName", "clucky");

        assertFalse(MobAdapter.hasCustomName(missingName));
        assertFalse(MobAdapter.hasCustomName(nullName));
        assertTrue(MobAdapter.hasCustomName(named));
    }

}
