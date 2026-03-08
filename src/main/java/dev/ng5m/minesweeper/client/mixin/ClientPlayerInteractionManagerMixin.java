package dev.ng5m.minesweeper.client.mixin;

import dev.ng5m.minesweeper.MinesweeperScreen;
import dev.ng5m.minesweeper.client.GlobalState;
import dev.ng5m.minesweeper.client.MinesweeperClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(
            method = "setGameMode",
            at = @At("HEAD")
    )
    private void setGameMode(GameMode gameMode, CallbackInfo ci) {
        if (!MinesweeperClient.isVL() && !FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        if (gameMode != GameMode.SPECTATOR
                && MinecraftClient.getInstance().currentScreen == MinesweeperScreen.INSTANCE) {
            MinesweeperScreen.INSTANCE.game.pause();
            MinecraftClient.getInstance().setScreen(null);
        }
    }

}
