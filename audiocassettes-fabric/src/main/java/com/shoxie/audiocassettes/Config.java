package com.shoxie.audiocassettes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON config (replaces the Forge TOML config).
 * File: config/audiocassettes-common.json
 */
public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class Data {
        // "Announce the name of the song currently playing to everyone"
        boolean NowPlayingAnnounce = true;
        // "If enabled, Walkman and Boombox will ignore empty slots of audio cassettes"
        boolean SkipEmptySlots = true;
    }

    private static Data data = new Data();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("audiocassettes-common.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                Data loaded = GSON.fromJson(r, Data.class);
                if (loaded != null) data = loaded;
            } catch (Exception e) {
                audiocassettes.logger.warn("Could not read {}, using defaults: {}", p, e.getMessage());
                data = new Data();
            }
        }
        // (re)write so that new/missing keys show up in the file
        save();
    }

    public static void save() {
        Path p = path();
        try {
            Files.createDirectories(p.getParent());
            try (Writer w = Files.newBufferedWriter(p)) {
                GSON.toJson(data, w);
            }
        } catch (IOException e) {
            audiocassettes.logger.warn("Could not write {}: {}", p, e.getMessage());
        }
    }

    public static boolean isNowPlayingAnnounce() {
        return data.NowPlayingAnnounce;
    }

    public static void setNowPlayingAnnounce(boolean val) {
        data.NowPlayingAnnounce = val;
        save();
    }

    public static boolean SkipEmptySlots() {
        return data.SkipEmptySlots;
    }

    public static void setSkipEmptySlots(boolean val) {
        data.SkipEmptySlots = val;
        save();
    }
}
