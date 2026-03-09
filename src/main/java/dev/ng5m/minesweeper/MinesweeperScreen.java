package dev.ng5m.minesweeper;

import dev.ng5m.minesweeper.client.MinesweeperFabricPlatform;
import dev.ng5m.minesweeper.game.Minesweeper;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.IntConsumer;

public class MinesweeperScreen extends Screen {
    public static final MinesweeperScreen INSTANCE = new MinesweeperScreen();

    private static final Identifier TEXTURE_ATLAS = Identifier.of("minesweeper", "atlas/atlas.png");
    private static final Identifier SEVEN_SEGMENT_ATLAS = Identifier.of("minesweeper", "atlas/7s.png");
    private static final Identifier OUTLINED_ATLAS = Identifier.of("minesweeper", "atlas/outlined.png");
    private static final Identifier DAVE = Identifier.of("minesweeper", "textures/dave.png");
    private static final Identifier DAVE_ATLAS = Identifier.of("minesweeper", "atlas/dave.png");
    private static final int ATLAS_ELEMENT_SIZE = 16;
    private static final int ATLAS_SIZE = 64;
    private static final int ATLAS_ROW_LENGTH = ATLAS_SIZE / ATLAS_ELEMENT_SIZE;

    private static final int OUTLINED_ATLAS_ELEMENT_WIDTH = 7;
    private static final int OUTLINED_ATLAS_ELEMENT_HEIGHT = 9;
    private static final int OUTLINED_ATLAS_WIDTH = OUTLINED_ATLAS_ELEMENT_WIDTH * 10;

    private static final int[] POWERS_OF_10 = {100, 10, 1};

    private static final int PADDING_W = 150;
    private static final int PADDING_H = 100;

    public final Minesweeper game = new Minesweeper(MinesweeperFabricPlatform.INSTANCE, 10, 10, 10);

    private int startX, startY;
    private int displayWidth, displayHeight;
    private int tileWidth, tileHeight;

    private TextFieldWidget inputMines;
    private TextFieldWidget inputWidth;
    private TextFieldWidget inputHeight;
    private CheckboxWidget checkboxEffects;
    private CheckboxWidget checkboxEvil;

    private MinesweeperScreen() {
        super(Text.of("Minesweeper"));
    }

    @Override
    protected void init() {
        initDimensions();

        this.inputMines = createIntInput(10, 999, newMines -> {
            if (newMines != game.mines) {
                game.rebuild(game.width, game.height, newMines);
                initDimensions();
            }
        });

        this.inputWidth = createIntInput(10, 100, nw -> {
            if (nw != game.width) {
                game.rebuild(nw, game.height, game.mines);
                initDimensions();
            }
        });

        this.inputHeight = createIntInput(10, 100, nh -> {
            if (nh != game.height) {
                game.rebuild(game.width, nh, game.mines);
                initDimensions();
            }
        });

        this.checkboxEffects = CheckboxWidget.builder(Text.of("SFX"), textRenderer).build();
        this.checkboxEvil = CheckboxWidget.builder(Text.of("Alt"), textRenderer).build();

        addDrawableChild(checkboxEffects);
        addDrawableChild(checkboxEvil);
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        initDimensions();
    }

    private TextFieldWidget createIntInput(int initial, int max, IntConsumer listener) {
        TextFieldWidget textFieldWidget = new TextFieldWidget(
                textRenderer, textRenderer.getWidth("000") * 2, ATLAS_ELEMENT_SIZE,
                Text.of(String.valueOf(initial))
        );
        textFieldWidget.setChangedListener(s -> {
            try {
                int i = Math.max(1, Math.min(Integer.parseInt(s), max));
                listener.accept(i);
            } catch (NumberFormatException ignored) {
            }
        });
        textFieldWidget.setText(String.valueOf(initial));

        addDrawableChild(textFieldWidget);

        return textFieldWidget;
    }

    private void initDimensions() {
        int windowWidth = this.width, windowHeight = this.height;
        int windowMidX = windowWidth / 2, windowMidY = windowHeight / 2;

        displayWidth = Math.min(game.width * ATLAS_ELEMENT_SIZE, windowWidth - PADDING_W);
        displayHeight = Math.min(game.height * ATLAS_ELEMENT_SIZE, windowHeight - PADDING_H);

        startX = windowMidX - (displayWidth / 2);
        startY = windowMidY - (displayHeight / 2);

        tileWidth = Math.ceilDiv(displayWidth, game.width);
        tileHeight = Math.ceilDiv(displayHeight, game.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float scale = (float) ATLAS_ELEMENT_SIZE / OUTLINED_ATLAS_ELEMENT_HEIGHT;
        int headerY = startY - 10 - ATLAS_ELEMENT_SIZE;

        if (checkboxEvil.isChecked()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, DAVE, width / 2 - ATLAS_ELEMENT_SIZE / 2,
                    headerY, 0, 0,
                    ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                    268, 268,
                    268, 268
            );
        } else {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, DAVE_ATLAS, width / 2 - ATLAS_ELEMENT_SIZE / 2, headerY,
                    (game.ended ? game.lost ? 3 : 2 : 0) * ATLAS_ELEMENT_SIZE, 0,
                    ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                    ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                    ATLAS_SIZE, ATLAS_SIZE
            );
        }


        int dx = startX;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_ATLAS, startX, headerY,
                2 * ATLAS_ELEMENT_SIZE, 2 * ATLAS_ELEMENT_SIZE,
                ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                ATLAS_SIZE, ATLAS_SIZE
        );
        dx += ATLAS_ELEMENT_SIZE + 5;

        drawCounter(context, dx, headerY, game.flags, scale);

        int endX = startX + displayWidth;
        dx = (int) (endX - (OUTLINED_ATLAS_ELEMENT_WIDTH * scale) * 3 + 3 - ATLAS_ELEMENT_SIZE);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_ATLAS,
                dx, headerY,
                3 * ATLAS_ELEMENT_SIZE, 3 * ATLAS_ELEMENT_SIZE,
                ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                ATLAS_SIZE, ATLAS_SIZE
        );
        dx += ATLAS_ELEMENT_SIZE + 5;

        long timeString = game.getTimeString();

        drawCounter(context, dx, headerY,
                (int) timeString, scale);

        dx = 5;
        int dy = headerY;
        context.drawText(textRenderer, "Mines", dx, dy, 0xffffffff, false);
        dy += textRenderer.fontHeight + 2;
        this.inputMines.setPosition(dx, dy);

        dy += ATLAS_ELEMENT_SIZE + 5;
        context.drawText(textRenderer, "Width", dx, dy, 0xffffffff, false);
        dy += textRenderer.fontHeight + 2;
        this.inputWidth.setPosition(dx, dy);

        dy += ATLAS_ELEMENT_SIZE + 5;
        context.drawText(textRenderer, "Height", dx, dy, 0xffffffff, false);
        dy += textRenderer.fontHeight + 2;
        this.inputHeight.setPosition(dx, dy);

        dy += ATLAS_ELEMENT_SIZE + 5;
        checkboxEffects.setPosition(dx, dy);
        dy += checkboxEffects.getHeight() + 10;
        checkboxEvil.setPosition(dx, dy);

        super.render(context, mouseX, mouseY, deltaTicks);

        context.fill(startX - 2, startY - 2, endX + 2, startY + (tileHeight * game.height) + 2, 0xff000000);

        for (int x = 0; x < game.width; x++) {
            for (int y = 0; y < game.height; y++) {
                byte tile = game.displayGrid[x][y];

                int u = tile % ATLAS_ROW_LENGTH;
                int v = tile / ATLAS_ROW_LENGTH;

                context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_ATLAS,
                        startX + (tileWidth * x), startY + (tileHeight * y),
                        u * ATLAS_ELEMENT_SIZE, v * ATLAS_ELEMENT_SIZE,
                        tileWidth, tileHeight,
                        ATLAS_ELEMENT_SIZE, ATLAS_ELEMENT_SIZE,
                        ATLAS_SIZE, ATLAS_SIZE
                );
            }
        }
    }

    private int drawCounter(DrawContext context, int x, int y, int n, float scale) {
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale);

        x = Math.round(x / scale);

        for (int i = 0; i < 3; i++) {
            int digit = Math.floorDiv(n, POWERS_OF_10[i]);

            context.drawTexture(RenderPipelines.GUI_TEXTURED, OUTLINED_ATLAS,
                    x, Math.round(y / scale),
                    digit * OUTLINED_ATLAS_ELEMENT_WIDTH, 0,
                    OUTLINED_ATLAS_ELEMENT_WIDTH, OUTLINED_ATLAS_ELEMENT_HEIGHT,
                    OUTLINED_ATLAS_ELEMENT_WIDTH, OUTLINED_ATLAS_ELEMENT_HEIGHT,
                    OUTLINED_ATLAS_WIDTH, OUTLINED_ATLAS_ELEMENT_HEIGHT
            );

            x += OUTLINED_ATLAS_ELEMENT_WIDTH + 1;
        }

        context.getMatrices().popMatrix();

        return x;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.x() < startX || click.x() >= startX + displayWidth
                || click.y() < startY || click.y() >= startY + displayHeight) {
            double midX = width / 2.0;
            double midOffset = ATLAS_ELEMENT_SIZE / 2.0;
            double headerY = startY - 10 - ATLAS_ELEMENT_SIZE;
            if (click.x() >= midX - midOffset && click.x() < midX + midOffset
                    && click.y() >= headerY && click.y() < headerY + ATLAS_ELEMENT_SIZE) {
                game.clickDave(checkboxEffects.isChecked());
                return true;
            }

            super.mouseClicked(click, doubled);
            return false;
        }

        int relativeX = (int) (click.x() - startX), relativeY = (int) (click.y() - startY);
        int cellX = relativeX / tileWidth, cellY = relativeY / tileHeight;

        game.clickCell(cellX, cellY, click.button() == 1, checkboxEffects.isChecked());

        return true;
    }

}
