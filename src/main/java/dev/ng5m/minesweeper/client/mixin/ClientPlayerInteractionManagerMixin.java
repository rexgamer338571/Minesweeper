package dev.ng5m.minesweeper.client.mixin;

import dev.ng5m.minesweeper.MinesweeperScreen;
import dev.ng5m.minesweeper.client.MinesweeperClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(
            method = "setLocalMode(Lnet/minecraft/world/level/GameType;)V",
            at = @At("HEAD")
    )
    private void setGameMode(GameType mode, CallbackInfo ci) {
        if (!MinesweeperClient.isVL() && !FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        if (mode != GameType.SPECTATOR
                && Minecraft.getInstance().gui.screen() == MinesweeperScreen.INSTANCE) {
            MinesweeperScreen.INSTANCE.game.pause();
            Minecraft.getInstance().gui.setScreen(null);
        }
    }

}
