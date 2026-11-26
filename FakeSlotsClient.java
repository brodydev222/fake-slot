package com.example.fakeslots;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Client-only cosmetic overlay. Nothing is sent to the server and no inventory data changes. */
public class FakeSlotsClient implements ClientModInitializer {
    public static boolean enabled = false;
    public static String titleMatch = "";          // part of the container title to match
    public static ItemStack shown = new ItemStack(Items.DIAMOND);
    public static ItemStack[] layout = null;       // per-slot override (container slot index 0..n)

    static final ItemStack[] DROPPER = {
        new ItemStack(Items.TRIDENT), new ItemStack(Items.WITHER_SKELETON_SKULL),
        new ItemStack(Items.DIAMOND_BLOCK), new ItemStack(Items.EMERALD_BLOCK),
        new ItemStack(Items.GOLD_BLOCK), new ItemStack(Items.IRON_BLOCK),
        new ItemStack(Items.LAPIS_BLOCK), new ItemStack(Items.REDSTONE), new ItemStack(Items.DIRT)
    };

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("fakeslots")
                .then(ClientCommandManager.literal("toggle").executes(c -> {
                    enabled = !enabled;
                    c.getSource().sendFeedback(Text.literal("FakeSlots " + (enabled ? "ON" : "OFF")));
                    return 1;
                }))
                .then(ClientCommandManager.literal("layout")
                    .then(ClientCommandManager.literal("dropper").executes(c -> {
                        layout = DROPPER; enabled = true;
                        c.getSource().sendFeedback(Text.literal("Dropper layout on"));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("off").executes(c -> {
                        layout = null;
                        c.getSource().sendFeedback(Text.literal("Layout off"));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("title")
                    .then(ClientCommandManager.argument("text", StringArgumentType.greedyString()).executes(c -> {
                        titleMatch = StringArgumentType.getString(c, "text");
                        c.getSource().sendFeedback(Text.literal("Matching titles containing: " + titleMatch));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("item")
                    .then(ClientCommandManager.argument("id", StringArgumentType.word()).executes(c -> {
                        Identifier id = Identifier.tryParse(StringArgumentType.getString(c, "id"));
                        Item item = id == null ? Items.AIR : Registries.ITEM.get(id);
                        if (item == Items.AIR) {
                            c.getSource().sendFeedback(Text.literal("Unknown item"));
                            return 0;
                        }
                        shown = new ItemStack(item);
                        c.getSource().sendFeedback(Text.literal("Showing " + id));
                        return 1;
                    })))));
    }
}
