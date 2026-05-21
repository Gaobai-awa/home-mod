package net.homemod.config;

import net.homemod.HomeMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HomeConfig {
    private static final Path FILE = HomeMod.CONFIG_DIR.resolve("homes.json");

    // playerUuid -> (homeName -> HomeData)
    private Map<String, PlayerHomes> players = new HashMap<>();
    // playerUuid -> last teleport source position (for /back)
    private Map<String, HomeData> lastPositions = new HashMap<>();

    public static HomeConfig load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE);
                HomeConfig cfg = HomeMod.GSON.fromJson(json, HomeConfig.class);
                if (cfg != null && cfg.players != null) {
                    HomeMod.LOGGER.info("Loaded {} player home configs", cfg.players.size());
                    return cfg;
                }
            }
        } catch (IOException e) {
            HomeMod.LOGGER.error("Failed to load home config", e);
        }
        HomeMod.LOGGER.info("Creating new home config");
        return new HomeConfig();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            String json = HomeMod.GSON.toJson(this);
            Files.writeString(FILE, json);
        } catch (IOException e) {
            HomeMod.LOGGER.error("Failed to save home config", e);
        }
    }

    public HomeData getHome(String uuid, String name) {
        PlayerHomes ph = players.get(uuid);
        if (ph == null) return null;
        return ph.homes.get(name);
    }

    public void setHome(String uuid, String name, HomeData data) {
        players.computeIfAbsent(uuid, k -> new PlayerHomes()).homes.put(name, data);
        save();
    }

    public boolean deleteHome(String uuid, String name) {
        PlayerHomes ph = players.get(uuid);
        if (ph == null) return false;
        boolean removed = ph.homes.remove(name) != null;
        if (removed) save();
        return removed;
    }

    public Collection<String> listHomes(String uuid) {
        PlayerHomes ph = players.get(uuid);
        if (ph == null) return Collections.emptyList();
        return ph.homes.keySet();
    }

    public HomeData getLastPosition(String uuid) {
        return lastPositions.get(uuid);
    }

    public void setLastPosition(String uuid, HomeData data) {
        lastPositions.put(uuid, data);
    }

    public boolean hasHome(String uuid, String name) {
        return getHome(uuid, name) != null;
    }

    // ---- nested classes ----

    public static class PlayerHomes {
        public Map<String, HomeData> homes = new HashMap<>();
    }

    public static class HomeData {
        public double x, y, z;
        public float yaw, pitch;
        public String world;

        public HomeData() {}

        public HomeData(double x, double y, double z, float yaw, float pitch, String world) {
            this.x = x; this.y = y; this.z = z;
            this.yaw = yaw; this.pitch = pitch;
            this.world = world;
        }
    }
}