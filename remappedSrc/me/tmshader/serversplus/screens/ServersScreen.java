package me.tmshader.serversplus.screens;

import java.util.List;

import me.tmshader.serversplus.ServersPlus;
import me.tmshader.serversplus.types.ServerList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ServersScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();
    private final Screen parent;
    protected ServersScreenWidget serverListWidget;
    private ServerList serverList;
    private ButtonWidget buttonEdit;
    private ButtonWidget buttonJoin;
    // private ButtonWidget buttonDelete;
    private List<Text> tooltipText;
    private me.tmshader.serversplus.types.ServerInfo selectedEntry;
    private boolean initialized;

    public ServersScreen(Screen parent) {
        super(Text.of(ServersPlus.config.node("button_text").getString()));
        this.parent = parent;
    }

    protected void init() {
        assert this.client != null;

        super.init();
        this.client.keyboard.setRepeatEvents(true);
        if (this.initialized) {
            this.serverListWidget.updateSize(this.width, this.height, 32, this.height - 64);
        } else {
            this.initialized = true;
            this.serverList = new ServerList(true /*this.client*/);
            // this.serverList.load();

            this.serverListWidget = new ServersScreenWidget(this, this.client, this.width, this.height, 32, this.height - 64, 36);
            this.serverListWidget.setServers(this.serverList);
        }

        this.addSelectableChild(this.serverListWidget);
        this.buttonJoin = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height - 52, 100, 20, new TranslatableTextContent("selectServer.select"), (button) -> {
            this.connect();
        }));
        this.buttonEdit = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, this.height - 52, 100, 20, new TranslatableTextContent("selectServer.edit"), (button) -> {
            this.client.setScreen(new EditServersScreen(this));
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height - 28, 100, 20, new TranslatableTextContent("selectServer.refresh"), (button) -> {
            this.refresh();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, this.height - 28, 100, 20, ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parent);
        }));
        this.updateButtonActivationStates();
    }

    public void tick() {
        super.tick();
        this.serverListPinger.tick();
    }

    public void removed() {
        assert this.client != null;
        this.client.keyboard.setRepeatEvents(false);
        this.serverListPinger.cancel();
    }

    private void refresh() {
        ServersPlus.loadConfig();
        this.serverList.load();
        this.client.setScreen(new ServersScreen(this.parent));
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_F5) {
            this.refresh();
            return true;
        } else if (this.serverListWidget.getSelectedOrNull() != null) {
            if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
                return this.serverListWidget.keyPressed(keyCode, scanCode, modifiers);
            } else {
                this.connect();
                return true;
            }
        } else {
            return false;
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.tooltipText = null;
        this.renderBackground(matrices);
        this.serverListWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
        if (this.tooltipText != null) {
            this.renderTooltip(matrices, this.tooltipText, mouseX, mouseY);
        }

    }

    public void connect() {
        ServersScreenWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        this.connect(((ServersScreenWidget.ServerEntry)entry).getServer());
    }

    private void connect(me.tmshader.serversplus.types.ServerInfo entry) {
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.getAddress()), new net.minecraft.client.network.ServerInfo(entry.getName(), entry.getAddress(), false));
    }

    public void select(ServersScreenWidget.Entry entry) {
        this.serverListWidget.setSelected(entry);
        this.updateButtonActivationStates();
    }

    protected void updateButtonActivationStates() {
        this.buttonJoin.active = false;
        this.buttonEdit.active = false;
        ServersScreenWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry != null && !(entry instanceof ServersScreenWidget.ScanningEntry)) {
            this.buttonJoin.active = true;
        }

    }

    public MultiplayerServerListPinger getServerListPinger() {
        return this.serverListPinger;
    }

    public void setTooltip(List<Text> tooltipText) {
        this.tooltipText = tooltipText;
    }

    public ServerList getServerList() {
        return this.serverList;
    }
}
