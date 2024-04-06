package dev.worldgen.tectonic;

import com.google.common.base.Charsets;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.GroupResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class TectonicBuiltInResourcePack extends GroupResourcePack {
    private final String name;
    private final String description;
    private final String path;

    public TectonicBuiltInResourcePack(PackType type, List<ModResourcePack> packs, String name, String description, String path) {
        super(type, packs);
        this.name = name;
        this.description = description;
        this.path = path;
    }

    @Nullable
    @Override
    public InputStream getRootResource(@NotNull String fileName) throws IOException {
        if ("pack.mcmeta".equals(fileName)) {
            String pack = String.format("{\"pack\":{\"pack_format\":9,\"description\":\"%s\"}}", this.description);
            return IOUtils.toInputStream(pack, Charsets.UTF_8);
        } else if ("pack.png".equals(fileName)) {
            InputStream stream = FabricLoader.getInstance().getModContainer("tectonic")
                .flatMap(container -> Optional.of(this.path).map(container::getPath))
                .filter(Files::exists)
                .map(iconPath -> {
                    try {
                        return Files.newInputStream(iconPath);
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(null);

            if (stream != null) {
                return stream;
            }
        }

        throw new FileNotFoundException("\"" + fileName + "\" in Tectonic built-in resource pack");
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> reader) throws IOException {
        try {
            InputStream inputStream = this.getRootResource("pack.mcmeta");
            Throwable error = null;
            T metadata;

            try {
                metadata = AbstractPackResources.getMetadataFromStream(reader, inputStream);
            } catch (Throwable e) {
                error = e;
                throw e;
            } finally {
                if (inputStream != null) {
                    if (error != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e) {
                            error.addSuppressed(e);
                        }
                    } else {
                        inputStream.close();
                    }
                }
            }

            return metadata;
        } catch (FileNotFoundException | RuntimeException e) {
            return null;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }
}
