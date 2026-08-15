package dev.ng5m.minesweeper.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ng5m.minesweeper.MinesweeperScreen;
import dev.ng5m.minesweeper.game.Match;
import dev.ng5m.minesweeper.game.Statistics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MinesweeperClient implements ClientModInitializer {
    public static final String MOD_ID = "minesweeper";
    private static final Minecraft MC = Minecraft.getInstance();

    private static final KeyMapping.Category BIND_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID));

    private static final KeyMapping BIND_OPEN_MINESWEEPER = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "Minesweeper", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, BIND_CATEGORY
    ));

    public static final Identifier SOUND_CLICK = Identifier.fromNamespaceAndPath("minesweeper", "click");
    public static final Identifier SOUND_BOMB = Identifier.fromNamespaceAndPath("minesweeper", "bomb");
    public static final Identifier SOUND_FLAG = Identifier.fromNamespaceAndPath("minesweeper", "flag");

    public static SoundEvent SOUND_EVENT_CLICK;
    public static SoundEvent SOUND_EVENT_BOMB;
    public static SoundEvent SOUND_EVENT_FLAG;

    public static final Path STATS_PATH = FabricLoader.getInstance()
            .getGameDir()
            .resolve("minesweeper.mdb");

    public static Statistics statistics;

    @Override
    public void onInitializeClient() {
        if (!Files.exists(STATS_PATH))
            statistics = new Statistics.V1(new ArrayList<>());
        else
            try {
                statistics = Statistics.deserialize(ByteBuffer.wrap(Files.readAllBytes(STATS_PATH)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        SOUND_EVENT_CLICK = Registry.register(BuiltInRegistries.SOUND_EVENT, SOUND_CLICK, SoundEvent.createVariableRangeEvent(SOUND_CLICK));
        SOUND_EVENT_BOMB = Registry.register(BuiltInRegistries.SOUND_EVENT, SOUND_BOMB, SoundEvent.createVariableRangeEvent(SOUND_BOMB));
        SOUND_EVENT_FLAG = Registry.register(BuiltInRegistries.SOUND_EVENT, SOUND_FLAG, SoundEvent.createVariableRangeEvent(SOUND_FLAG));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (BIND_OPEN_MINESWEEPER.consumeClick()) {
                MinesweeperScreen.INSTANCE.game.unpause();
                mc.setScreenAndShow(MinesweeperScreen.INSTANCE);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            if (!(statistics instanceof Statistics.V1(List<Match> matches)))
                throw new UnsupportedOperationException("Unsupported statistics");

            ByteBuffer buffer = ByteBuffer.allocate(1 + 4 +
                    Match.SIZE * matches.size());

            Statistics.serialize(buffer, statistics);

            try {
                Files.write(STATS_PATH, buffer.array());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

//        HudElementRegistry.addLast(Identifier.of("minesweeper", "tooltip"),
//                (ctx, tickCounter) -> {
//                    if (MC.interactionManager == null) return;
//                    if (MC.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR
//                            && !FabricLoader.getInstance().isDevelopmentEnvironment()) return;
//
//                    ctx.drawText(MC.textRenderer,
//                            String.format("Press [%s] to play Minesweeper", InputUtil.fromTranslationKey(BIND_OPEN_MINESWEEPER.getBoundKeyTranslationKey())),
//                            ctx.getScaledWindowWidth() / 2 - 91, 3,
//                            0xffffffff, true
//                    );
//                }
//        );
    }

    public static boolean isVL() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return true;
        if (MC.getCurrentServer() == null) return false;
        String address = MC.getCurrentServer().ip.toLowerCase();
        return address.endsWith("pvplegacy.net")
                || address.endsWith("vanillalegacy.com")
                || address.endsWith("mcpwn.net");
    }
}
