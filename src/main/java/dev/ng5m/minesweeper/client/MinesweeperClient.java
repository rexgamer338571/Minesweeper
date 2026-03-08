package dev.ng5m.minesweeper.client;

import dev.ng5m.minesweeper.MinesweeperScreen;
import dev.ng5m.minesweeper.game.Match;
import dev.ng5m.minesweeper.game.Statistics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MinesweeperClient implements ClientModInitializer {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    private static final KeyBinding BIND_OPEN_MINESWEEPER = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Minesweeper", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, KeyBinding.Category.MISC
    ));

    public static final Identifier SOUND_CLICK = Identifier.of("minesweeper", "click");
    public static final Identifier SOUND_BOMB = Identifier.of("minesweeper", "bomb");
    public static final Identifier SOUND_FLAG = Identifier.of("minesweeper", "flag");

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

        SOUND_EVENT_CLICK = Registry.register(Registries.SOUND_EVENT, SOUND_CLICK, SoundEvent.of(SOUND_CLICK));
        SOUND_EVENT_BOMB = Registry.register(Registries.SOUND_EVENT, SOUND_BOMB, SoundEvent.of(SOUND_BOMB));
        SOUND_EVENT_FLAG = Registry.register(Registries.SOUND_EVENT, SOUND_FLAG, SoundEvent.of(SOUND_FLAG));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (BIND_OPEN_MINESWEEPER.wasPressed()) {
                MinesweeperScreen.INSTANCE.game.unpause();
                mc.setScreen(MinesweeperScreen.INSTANCE);
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
        if (MC.getCurrentServerEntry() == null) return false;
        String address = MC.getCurrentServerEntry().address.toLowerCase();
        return address.endsWith("pvplegacy.net")
                || address.endsWith("vanillalegacy.com")
                || address.endsWith("mcpwn.net");
    }
}
