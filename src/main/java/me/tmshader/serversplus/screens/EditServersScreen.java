package me.tmshader.serversplus.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
// import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
// import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class EditServersScreen extends Screen {
    private int tickCounter;
    private ButtonWidget buttonJoin;
    public static final Identifier BOOK_TEXTURE = new Identifier("serversplus:textures/gui/config.png");
    private Screen parent;

    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private ButtonWidget doneButton;
    private ButtonWidget signButton;
    private ButtonWidget finalizeButton;
    private ButtonWidget cancelButton;

    public EditServersScreen(Screen parent) {
        super(Text.of("Hola"));
        this.parent = parent;
    }

    public void tick() {
        super.tick();
        ++this.tickCounter;
    }

    protected void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);
        this.doneButton = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, 210, 98, 20, ScreenTexts.DONE, (button) -> {
            this.client.setScreen(this.parent);
            // this.finalizeBook(false);
        }));
        this.cancelButton = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 210, 98, 20, ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
            /*if (this.signing) {
                this.signing = false;
            }

            this.updateButtons();*/
        }));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
        int i = (this.width - 208) / 2;
        this.drawTexture(matrices, i, 2, 0, 0, 208, 208);

        Position position = absolutePositionToScreenPosition(new Position(0, 0));

        this.textRenderer.draw(matrices, Text.of("Hello"), (float) position.x, (float) position.y, -16777216);

        this.drawCursor(matrices, new Position(0, 0), false);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void drawCursor(MatrixStack matrices, Position position, boolean atEnd) {
        if (this.tickCounter / 6 % 2 == 0) {
            position = this.absolutePositionToScreenPosition(position);
            if (!atEnd) {
                int x1 = position.x;
                int y1 = position.y - 1;
                int x2 = position.x + 1;
                int y2 = position.y + 9;
                Objects.requireNonNull(this.textRenderer);
                DrawableHelper.fill(matrices, x1, y1, x2, y2, -16777216);
            } else {
                this.textRenderer.draw(matrices, (String)"_", (float)position.x, (float)position.y, 0);
            }
        }
    }

    private Position absolutePositionToScreenPosition(Position position) {
        return new Position(position.x + (this.width - 208) / 2 + 17, position.y + 20);
    }

    @Environment(EnvType.CLIENT)
    static class Position {
        public final int x;
        public final int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
