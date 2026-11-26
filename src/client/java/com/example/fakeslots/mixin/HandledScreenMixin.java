package com.example.fakeslots.mixin;

import com.example.fakeslots.FakeSlotsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow @org.spongepowered.asm.mixin.Final protected ScreenHandler handler;

    protected HandledScreenMixin(Text t) { super(t); }

    @Inject(method = "render", at = @At("TAIL"))
    private void fakeslots$overlay(DrawContext ctx, int mx, int my, float delta, CallbackInfo ci) {
        if (!FakeSlotsClient.enabled) return;
        String t = this.title.getString();
        if (!FakeSlotsClient.titleMatch.isEmpty()
                && !t.toLowerCase().contains(FakeSlotsClient.titleMatch.toLowerCase())) return;

        for (Slot s : handler.slots) {
            if (s.inventory instanceof PlayerInventory) continue; // only the container's slots
            int sx = this.x + s.x, sy = this.y + s.y;
            ctx.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
            net.minecraft.item.ItemStack st = FakeSlotsClient.shown;
            if (FakeSlotsClient.layout != null) {
                int i = s.getIndex();
                if (i < FakeSlotsClient.layout.length) st = FakeSlotsClient.layout[i];
            }
            ctx.drawItem(st, sx, sy);
        }
    }
}
