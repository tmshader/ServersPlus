package me.tmshader.serversplus.screens;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import me.tmshader.serversplus.ServersPlus;
import me.tmshader.serversplus.types.ServerInfo;
import me.tmshader.serversplus.types.ServerList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ServersScreenWidget extends AlwaysSelectedEntryListWidget<ServersScreenWidget.Entry> {
    static final Logger LOGGER = LogManager.getLogger();
    static final ThreadPoolExecutor SERVER_PINGER_THREAD_POOL;
    static final Identifier UNKNOWN_SERVER_TEXTURE;
    static final Identifier SERVER_SELECTION_TEXTURE;
    static final Text LAN_SCANNING_TEXT;
    static final Text CANNOT_RESOLVE_TEXT;
    static final Text CANNOT_CONNECT_TEXT;
    static final Text INCOMPATIBLE_TEXT;
    static final Text NO_CONNECTION_TEXT;
    static final Text PINGING_TEXT;
    private final ServersScreen screen;
    private final List<ServersScreenWidget.ServerEntry> servers = Lists.newArrayList();

    public ServersScreenWidget(ServersScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
        super(client, width, height, top, bottom, entryHeight);
        this.screen = screen;
    }

    private void updateEntries() {
        this.clearEntries();
        this.servers.forEach(this::addEntry);
    }

    public void setSelected(@Nullable ServersScreenWidget.Entry entry) {
        super.setSelected(entry);
        this.screen.updateButtonActivationStates();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ServersScreenWidget.Entry entry = (ServersScreenWidget.Entry)this.getSelectedOrNull();
        return entry != null ? entry.keyPressed(keyCode, scanCode, modifiers) : super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void moveSelection(EntryListWidget.MoveDirection direction) {
        this.moveSelectionIf(direction, (entry) -> {
            return !(entry instanceof ServersScreenWidget.ScanningEntry);
        });
    }

    public void setServers(ServerList servers) {
        this.servers.clear();

        for(int i = 0; i < servers.size(); ++i) {
            this.servers.add(new ServersScreenWidget.ServerEntry(this.screen, servers.get(i)));
        }

        this.updateEntries();
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 30;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    static {
        SERVER_PINGER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER)).build());
        UNKNOWN_SERVER_TEXTURE = new Identifier("textures/misc/unknown_server.png");
        SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
        LAN_SCANNING_TEXT = new TranslatableTextContent("lanServer.scanning");
        CANNOT_RESOLVE_TEXT = (new TranslatableTextContent("multiplayer.status.cannot_resolve")).formatted(Formatting.DARK_RED);
        CANNOT_CONNECT_TEXT = (new TranslatableTextContent("multiplayer.status.cannot_connect")).formatted(Formatting.DARK_RED);
        INCOMPATIBLE_TEXT = new TranslatableTextContent("multiplayer.status.incompatible");
        NO_CONNECTION_TEXT = new TranslatableTextContent("multiplayer.status.no_connection");
        PINGING_TEXT = new TranslatableTextContent("multiplayer.status.pinging");
    }

    @Environment(EnvType.CLIENT)
    public static class ScanningEntry extends ServersScreenWidget.Entry {
        private final MinecraftClient client = MinecraftClient.getInstance();

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            assert this.client.currentScreen != null;

            int var10000 = y + entryHeight / 2;
            Objects.requireNonNull(this.client.textRenderer);
            int i = var10000 - 9 / 2;
            this.client.textRenderer.draw(matrices, ServersScreenWidget.LAN_SCANNING_TEXT, (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth((StringVisitable)ServersScreenWidget.LAN_SCANNING_TEXT) / 2), (float)i, 16777215);
            String string;
            switch((int)(Util.getMeasuringTimeMs() / 300L % 4L)) {
                case 0:
                default:
                    string = "O o o";
                    break;
                case 1:
                case 3:
                    string = "o O o";
                    break;
                case 2:
                    string = "o o O";
            }

            TextRenderer var13 = this.client.textRenderer;
            float var10003 = (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(string) / 2);
            Objects.requireNonNull(this.client.textRenderer);
            var13.draw(matrices, string, var10003, (float)(i + 9), 8421504);
        }

        public Text getNarration() {
            return LiteralTextContent.EMPTY;
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<ServersScreenWidget.Entry> {
    }

    @Environment(EnvType.CLIENT)
    public class ServerEntry extends ServersScreenWidget.Entry {
        private static final int field_32387 = 32;
        private static final int field_32388 = 32;
        private static final int field_32389 = 0;
        private static final int field_32390 = 32;
        private static final int field_32391 = 64;
        private static final int field_32392 = 96;
        private static final int field_32393 = 0;
        private static final int field_32394 = 32;
        private final ServersScreen screen;
        private final MinecraftClient client;
        private final ServerInfo server;
        private final Identifier iconTextureId;
        private String iconUri;
        @Nullable
        private NativeImageBackedTexture icon;
        private long time;

        protected ServerEntry(ServersScreen screen, ServerInfo server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
            this.iconTextureId = new Identifier("servers/" + Hashing.sha1().hashUnencodedChars(server.getAddress()) + "/icon");
            AbstractTexture abstractTexture = this.client.getTextureManager().getOrDefault(this.iconTextureId, MissingSprite.getMissingSpriteTexture());
            if (abstractTexture != MissingSprite.getMissingSpriteTexture() && abstractTexture instanceof NativeImageBackedTexture) {
                this.icon = (NativeImageBackedTexture)abstractTexture;
            }

        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            /*if (!this.server.online) {
                this.server.online = true;
                this.server.ping = -2L;
                this.server.label = LiteralText.EMPTY;
                this.server.playerCountLabel = LiteralText.EMPTY;
                ServersScreenWidget.SERVER_PINGER_THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getServerListPinger().add(this.server, () -> {
                            this.client.execute(this::saveFile);
                        });
                    } catch (UnknownHostException var2) {
                        this.server.ping = -1L;
                        this.server.label = ServersScreenWidget.CANNOT_RESOLVE_TEXT;
                    } catch (Exception var3) {
                        this.server.ping = -1L;
                        this.server.label = ServersScreenWidget.CANNOT_CONNECT_TEXT;
                    }

                });
            }*/

            // boolean bl = this.server.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion();
            boolean bl = false;
            List<OrderedText> list;

            if (this.server.getDescription().getHidden()) {
                this.client.textRenderer.draw(matrices, "", (float) (x + 32 + 3), (float) (y + 1), 16777215);
                list = this.client.textRenderer.wrapLines(StringVisitable.plain(this.server.getName()), entryWidth - 32 - 2);
            } else {
                this.client.textRenderer.draw(matrices, this.server.getName(), (float) (x + 32 + 3), (float) (y + 1), 16777215);
                list = this.client.textRenderer.wrapLines(StringVisitable.plain(this.server.getDescription().getText()), entryWidth - 32 - 2);
            }

            for(int ix = 0; ix < Math.min(list.size(), 2); ++ix) {
                TextRenderer var10000 = this.client.textRenderer;
                OrderedText var10002 = (OrderedText)list.get(ix);
                float var10003 = (float)(x + 32 + 3);
                int var10004 = y + 12;
                Objects.requireNonNull(this.client.textRenderer);
                var10000.draw(matrices, var10002, var10003, (float)(var10004 + 9 * ix), 8421504);
            }

            // Text i = bl ? this.server.version.shallowCopy().formatted(Formatting.RED) : this.server.getPlayerCount().getCurrent();
            Text i;
            switch (this.server.getPlayerCount().getStyle()) {
                case INVISIBLE -> i = Text.of("");
                case VANILLA -> i = Text.of(this.server.getPlayerCount().getCurrent()).copy().append(Text.of("/").copy().formatted(Formatting.DARK_GRAY)).append(Text.of(this.server.getPlayerCount().getMax()));
                case CURRENT -> i = Text.of("Players: " + this.server.getPlayerCount().getCurrent());
                case HIDDEN -> i = Text.of("???");
                case MAX -> i = Text.of("Max Players: " + this.server.getPlayerCount().getMax());
                default -> throw new IllegalStateException("Unexpected value: " + this.server.getPlayerCount().getStyle());
            }

            int j = this.client.textRenderer.getWidth((StringVisitable)i);
            this.client.textRenderer.draw(matrices, (Text)i, (float)(x + entryWidth - j - 15 - 2), (float)(y + 1), 8421504);
            int k = 0;
            int l;
            List list2;
            Text text;
            /*if (bl) {
                l = 5;
                text = ServersScreenWidget.INCOMPATIBLE_TEXT;
                list2 = this.server.playerListSummary;
            } else if (this.server.online && this.server.ping != -2L) {
                if (this.server.ping < 0L) {
                    l = 5;
                } else if (this.server.ping < 150L) {
                    l = 0;
                } else if (this.server.ping < 300L) {
                    l = 1;
                } else if (this.server.ping < 600L) {
                    l = 2;
                } else if (this.server.ping < 1000L) {
                    l = 3;
                } else {
                    l = 4;
                }

                if (this.server.ping < 0L) {
                    text = ServersScreenWidget.NO_CONNECTION_TEXT;
                    list2 = Collections.emptyList();
                } else {
                    text = new TranslatableText("multiplayer.status.ping", this.server.ping);
                    list2 = this.server.playerListSummary;
                }
            } else {
                k = 1;
                l = (int)(Util.getMeasuringTimeMs() / 100L + (long)(index * 2L) & 7L);
                if (l > 4) {
                    l = 8 - l;
                }

                text = ServersScreenWidget.PINGING_TEXT;
                list2 = Collections.emptyList();
            }*/

            l = server.getPing();
            text = new TranslatableTextContent("multiplayer.status.ping", this.server.getPing());
            list2 = Collections.emptyList();

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
            DrawableHelper.drawTexture(matrices, x + entryWidth - 15, y, (float)(k * 10), (float)(176 + l * 8), 10, 8, 256, 256);
            String string = this.server.getIcon();
            if (!Objects.equals(string, this.iconUri)) {
                if (this.isNewIconValid(string)) {
                    this.iconUri = string;
                } else {
                    // this.server.setIcon((String)null);
                    this.saveFile();
                }
            }

            if (this.icon == null) {
                this.draw(matrices, x, y, ServersScreenWidget.UNKNOWN_SERVER_TEXTURE);
            } else {
                this.draw(matrices, x, y, this.iconTextureId);
            }

            int m = mouseX - x;
            int n = mouseY - y;
            if (m >= entryWidth - 15 && m <= entryWidth - 5 && n >= 0 && n <= 8) {
                this.screen.setTooltip(Collections.singletonList(text));
            } else if (m >= entryWidth - j - 15 - 2 && m <= entryWidth - 15 - 2 && n >= 0 && n <= 8) {
                this.screen.setTooltip(list2);
            }

            if (this.client.options.touchscreen || hovered) {
                RenderSystem.setShaderTexture(0, ServersScreenWidget.SERVER_SELECTION_TEXTURE);
                DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int o = mouseX - x;
                int p = mouseY - y;
                if (this.canConnect()) {
                    if (o < 32 && o > 16) {
                        DrawableHelper.drawTexture(matrices, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (index > 0) {
                    if (o < 16 && p < 16) {
                        DrawableHelper.drawTexture(matrices, x, y, 96.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        DrawableHelper.drawTexture(matrices, x, y, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (index < this.screen.getServerList().size() - 1) {
                    if (o < 16 && p > 16) {
                        DrawableHelper.drawTexture(matrices, x, y, 64.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        DrawableHelper.drawTexture(matrices, x, y, 64.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
            }

        }

        public void saveFile() {
            ServersPlus.LOGGER.info("Saving not implemented yet");
            // this.screen.getServerList().saveFile();
        }

        protected void draw(MatrixStack matrices, int x, int y, Identifier textureId) {
            RenderSystem.setShaderTexture(0, textureId);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean canConnect() {
            return true;
        }

        private boolean isNewIconValid(@Nullable String newIconUri) {
            if (newIconUri == null) {
                this.client.getTextureManager().destroyTexture(this.iconTextureId);
                if (this.icon != null && this.icon.getImage() != null) {
                    this.icon.getImage().close();
                }

                this.icon = null;
            } else {
                try {
                    NativeImage nativeImage = NativeImage.read(newIconUri);
                    Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
                    if (this.icon == null) {
                        this.icon = new NativeImageBackedTexture(nativeImage);
                    } else {
                        this.icon.setImage(nativeImage);
                        this.icon.upload();
                    }

                    this.client.getTextureManager().registerTexture(this.iconTextureId, this.icon);
                } catch (Throwable var3) {
                    ServersScreenWidget.LOGGER.error((String)"Invalid icon for server {} ({})", (Object)this.server.getName(), this.server.getAddress(), var3);
                    return false;
                }
            }

            return true;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (Screen.hasShiftDown()) {
                ServersScreenWidget ServersScreenWidget = this.screen.serverListWidget;
                int i = ServersScreenWidget.children().indexOf(this);
                if (keyCode == GLFW.GLFW_KEY_DOWN && i < this.screen.getServerList().size() - 1 || keyCode == GLFW.GLFW_KEY_UP && i > 0) {
                    this.swapEntries(i, keyCode == GLFW.GLFW_KEY_DOWN ? i + 1 : i - 1);
                    return true;
                }
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void swapEntries(int i, int j) {
            this.screen.getServerList().swapEntries(i, j);
            this.screen.serverListWidget.setServers(this.screen.getServerList());
            ServersScreenWidget.Entry entry = (ServersScreenWidget.Entry)this.screen.serverListWidget.children().get(j);
            this.screen.serverListWidget.setSelected(entry);
            ServersScreenWidget.this.ensureVisible(entry);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double d = mouseX - (double)ServersScreenWidget.this.getRowLeft();
            double e = mouseY - (double)ServersScreenWidget.this.getRowTop(ServersScreenWidget.this.children().indexOf(this));
            if (d <= 32.0D) {
                if (d < 32.0D && d > 16.0D && this.canConnect()) {
                    this.screen.select(this);
                    this.screen.connect();
                    return true;
                }

                int i = this.screen.serverListWidget.children().indexOf(this);
                if (d < 16.0D && e < 16.0D && i > 0) {
                    this.swapEntries(i, i - 1);
                    return true;
                }

                if (d < 16.0D && e > 16.0D && i < this.screen.getServerList().size() - 1) {
                    this.swapEntries(i, i + 1);
                    return true;
                }
            }

            this.screen.select(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.screen.connect();
            }

            this.time = Util.getMeasuringTimeMs();
            return false;
        }

        public ServerInfo getServer() {
            return this.server;
        }

        public Text getNarration() {
            return new TranslatableTextContent("narrator.select", new Object[]{this.server.getName()});
        }
    }

    @Environment(EnvType.CLIENT)
    public static class LanServerEntry extends ServersScreenWidget.Entry {
        private static final int field_32386 = 32;
        private static final Text TITLE_TEXT = new TranslatableTextContent("lanServer.title");
        private static final Text HIDDEN_ADDRESS_TEXT = new TranslatableTextContent("selectServer.hiddenAddress");
        private final ServersScreen screen;
        protected final MinecraftClient client;
        protected final LanServerInfo server;
        private long time;

        protected LanServerEntry(ServersScreen screen, LanServerInfo server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.client.textRenderer.draw(matrices, TITLE_TEXT, (float)(x + 32 + 3), (float)(y + 1), 16777215);
            this.client.textRenderer.draw(matrices, this.server.getMotd(), (float)(x + 32 + 3), (float)(y + 12), 8421504);
            if (this.client.options.hideServerAddress) {
                this.client.textRenderer.draw(matrices, HIDDEN_ADDRESS_TEXT, (float)(x + 32 + 3), (float)(y + 12 + 11), 3158064);
            } else {
                this.client.textRenderer.draw(matrices, this.server.getAddressPort(), (float)(x + 32 + 3), (float)(y + 12 + 11), 3158064);
            }

        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.screen.select(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.screen.connect();
            }

            this.time = Util.getMeasuringTimeMs();
            return false;
        }

        public LanServerInfo getLanServerEntry() {
            return this.server;
        }

        public Text getNarration() {
            return new TranslatableTextContent("narrator.select", new Object[]{(new LiteralTextContent("")).append(TITLE_TEXT).append(" ").append(this.server.getMotd())});
        }
    }
}