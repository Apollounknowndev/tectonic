package dev.worldgen.tectonic.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import dev.worldgen.tectonic.Tectonic;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            writeSortedKeys(jsonWriter, element);
            writer.write(commentHack(stringWriter.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copied and modified from GsonHelper.writeValue()
     * Method must be copied because it doesn't exist in 1.18.2
     * This multi-version stuff sucks
     */
    public static void writeSortedKeys(JsonWriter writer, @Nullable JsonElement json) throws IOException {
        if (json != null && !json.isJsonNull()) {
            if (json instanceof JsonPrimitive jsonPrimitive) {
                if (jsonPrimitive.isNumber()) {
                    writer.value(jsonPrimitive.getAsNumber());
                } else if (jsonPrimitive.isBoolean()) {
                    writer.value(jsonPrimitive.getAsBoolean());
                } else {
                    writer.value(jsonPrimitive.getAsString());
                }
            } else {
                if (json.isJsonArray()) {
                    writer.beginArray();

                    for (JsonElement element : json.getAsJsonArray()) {
                        writeSortedKeys(writer, element);
                    }

                    writer.endArray();
                } else {
                    if (!json.isJsonObject()) {
                        throw new IllegalArgumentException("Couldn't write " + json.getClass());
                    }

                    writer.beginObject();
                    for (Map.Entry<String, JsonElement> elementEntry : sort(json.getAsJsonObject().entrySet())) {
                        writer.name(elementEntry.getKey());
                        writeSortedKeys(writer, elementEntry.getValue());
                    }

                    writer.endObject();
                }
            }
        } else {
            writer.nullValue();
        }

    }


    /**
     * Copied and modified from GsonHelper.sortByKeyIfNeeded()
     * Method must be copied because it doesn't exist in 1.18.2
     * This multi-version stuff sucks
     */
    private static Collection<Map.Entry<String, JsonElement>> sort(Collection<Map.Entry<String, JsonElement>> entries) {
        List<Map.Entry<String, JsonElement>> list = new ArrayList<>(entries);
        list.sort(Map.Entry.comparingByKey(String::compareTo));
        return list;
    }

    /**
     * Yeah, we don't talk about this
     * The alternative is to try and JiJ Jankson across all mod versions to have real comments
     * That sounds like too much work
     */
    private static String commentHack(String json) {
        return json.replaceAll("\"__.\": \"", "// ").replaceAll("\"...__\": \"", "// ").replace("\",", "");
    }
}