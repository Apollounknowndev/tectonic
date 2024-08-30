package dev.worldgen.tectonic.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import dev.worldgen.tectonic.Tectonic;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigHandler {
    private static ConfigCodec LOADED_CONFIG = new ConfigCodec(
        true,
        ConfigCodec.Legacy.DEFAULT,
        ConfigCodec.Features.DEFAULT,
        ConfigCodec.Experimental.DEFAULT
    );

    public static ConfigCodec getConfig() {
        return LOADED_CONFIG;
    }

    public static void load(Path path) {
        if (!Files.isRegularFile(path)) {
            write(path);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonElement json = JsonParser.parseReader(reader);
            Optional<ConfigCodec> result = ConfigCodec.CODEC.parse(JsonOps.INSTANCE, json).result();
            if (result.isPresent()) {
                LOADED_CONFIG = result.get();
            } else {
                throw new JsonParseException("Invalid codec");
            }
        } catch (JsonParseException e) {
            Tectonic.LOGGER.error("Couldn't parse config file, resetting to default config");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        write(path);
    }

    private static void write(Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            JsonElement element = ConfigCodec.CODEC.encodeStart(JsonOps.INSTANCE, LOADED_CONFIG).result().get();
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setIndent("  ");
            GsonHelper.writeValue(jsonWriter, element, Comparator.naturalOrder());
            writer.write(commentHack(stringWriter.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Yeah, we don't talk about this
     * The alternative is to try and JiJ Jankson
     * That sounds like too much work
     */
    private static String commentHack(String json) {
        return json.replaceAll("\"__.\": \"", "// ").replaceAll("\"...__\": \"", "// ").replace("\",", "");
    }
}