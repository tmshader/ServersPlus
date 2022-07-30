package me.tmshader.serversplus.types;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ServerInfo {
    private String name;
    private String address;
    private String icon;
    @Setting(value = "resourcePack")
    private ResourcePackPolicy resourcePack;
    private int ping;
    private Description description;
    @Setting(value = "playerCount")
    private PlayerCount playerCount;

    public String getName() { return this.name; }
    public String getAddress() { return this.address; }
    public String getIcon() { return this.icon; }
    public Text getResourcePackText() { return this.resourcePack.getText(); }
    public ResourcePackPolicy getResourcePack() { return this.resourcePack; }
    public int getPing() { return this.ping; }
    public Description getDescription() { return this.description; }
    public PlayerCount getPlayerCount() { return this.playerCount; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setResourcePack(String resourcePack) { this.resourcePack = ResourcePackPolicy.valueOf(resourcePack); }
    public void setResourcePack(ResourcePackPolicy resourcePack) { this.resourcePack = resourcePack; }
    public void setPing(int ping) { this.ping = ping; }
    public void setDescription(Description description) { this.description = description; }
    public void setPlayerCount(PlayerCount playerCount) { this.playerCount = playerCount; }

    @Override
    public String toString() {
        return "ServerInfo { " +
                "name = '" + name + '\'' +
                ", address = '" + address + '\'' +
                ", icon = '" + icon + '\'' +
                ", resourcePack = " + resourcePack +
                ", ping = " + ping +
                ", description = " + description +
                ", playerCount = " + playerCount +
                " }";
    }

    @Environment(EnvType.CLIENT)
    public static enum ResourcePackPolicy {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final String name;
        private final Text text;

        private ResourcePackPolicy(String name) {
            this.name = name;
            this.text = new TranslatableTextContent("addServer.resourcePack." + name);
        }

        public Text getText() {
            return this.text;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @ConfigSerializable
    public static class Description {
        private boolean hidden;
        private String text;

        public boolean getHidden() { return this.hidden; }
        public String getText() { return this.text; }

        public void setHidden(boolean hidden) { this.hidden = hidden; }
        public void setText(String text) { this.text = text; }

        @Override
        public String toString() {
            return "Description { " +
                    "hidden = " + hidden +
                    ", text = '" + text + '\'' +
                    " }";
        }
    }

    @ConfigSerializable
    public static class PlayerCount {
        private Style style;
        private int current;
        private int max;

        public Style getStyle() { return this.style; }
        public String getCurrent() { return String.valueOf(this.current); }
        public String getMax() { return String.valueOf(this.max); }

        public void setStyle(Style style) { this.style = style; }
        public void setCurrent(int current) { this.current = current; }
        public void setMax(int max) { this.max = max; }

        @Override
        public String toString() {
            return "PlayerCount { " +
                    "style = " + style +
                    ", current = " + current +
                    ", max = " + max +
                    " }";
        }

        public enum Style {
            INVISIBLE("invisible"),
            HIDDEN("hidden"),
            MAX("max"),
            CURRENT("current"),
            VANILLA("vanilla");

            private final String name;

            Style(String name) { this.name = name; }

            @Override
            public String toString() {
                return this.name;
            }
        }
    }
}
