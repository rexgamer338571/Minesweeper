package dev.ng5m.minesweeper.client;

import dev.ng5m.minesweeper.game.MinesweeperPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

public class MinesweeperFabricPlatform implements MinesweeperPlatform {
    public static final MinesweeperFabricPlatform INSTANCE = new MinesweeperFabricPlatform();
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void playSound(Sound sound) {
        if (MC.player == null) return;

        SoundEvent soundEvent = switch (sound) {
            case CLICK -> MinesweeperClient.SOUND_EVENT_CLICK;
            case BOMB -> MinesweeperClient.SOUND_EVENT_BOMB;
            case FLAG -> MinesweeperClient.SOUND_EVENT_FLAG;
        };

        MC.player.playSound(soundEvent, 1.0f, 1.0f);
    }
}
