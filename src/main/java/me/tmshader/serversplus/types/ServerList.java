package me.tmshader.serversplus.types;

import java.util.ArrayList;
import java.util.List;

import me.tmshader.serversplus.ServersPlus;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

@ConfigSerializable
public class ServerList {
    //private final MinecraftClient client;
    @Setting(value = "servers")
    private List<ServerInfo> serverInfoList = new ArrayList<>();

    public ServerList() {}

    public ServerList(boolean load) {
        //this.client = client;
        if (load) this.load();
    }

    public List<ServerInfo> getServerInfoList() {
        return this.serverInfoList;
    }
    public void setServerInfoList(List<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
    };

    public void load() {
        try {
            this.serverInfoList.clear();
            this.setServerInfoList(ServersPlus.config.get(ServerList.class).getServerInfoList());
        } catch (SerializationException e) {
            ServersPlus.LOGGER.error("Couldn't load server list", e);
        }
    }

    public ServerInfo get(int index) {
        return this.serverInfoList.get(index);
    }
    public void remove(int index) {
        this.serverInfoList.remove(index);
    }
    public void add(ServerInfo serverInfo) {
        this.serverInfoList.add(serverInfo);
    }
    public int size() {
        return this.serverInfoList.size();
    }
    public void clear() {
        this.serverInfoList.clear();
    }
    public void set(int index, ServerInfo serverInfo) {
        this.serverInfoList.set(index, serverInfo);
    }

    public void swapEntries(int index1, int index2) {
        ServerInfo serverInfo = this.get(index1);
        this.serverInfoList.set(index1, this.get(index2));
        this.serverInfoList.set(index2, serverInfo);
    }
}