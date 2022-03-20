package dev.compactmods.crafting.tests.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.gametest.framework.GameTestHelper;

public class FileHelper {

    public static final FileHelper INSTANCE = new FileHelper();

    private FileHelper() {}

    public File getFile(String filename) {
        URL res = getClass().getClassLoader().getResource(filename);
        if(res == null) {
            CompactCrafting.LOGGER.error("Tried to access {} but file not found.", filename);
            return null;
        }

        return new File(res.getFile());
    }

    private InputStreamReader openFile(String filename) {
        URL res = getClass().getClassLoader().getResource(filename);
        if(res == null) {
            CompactCrafting.LOGGER.error("Tried to access {} but file not found.", filename);
            return null;
        }

        try {
            InputStream inputStream = res.openStream();
            return new InputStreamReader(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JsonElement getJsonFromFile(String filename) {
        Gson g = new Gson();
        InputStreamReader isr = INSTANCE.openFile(filename);
        return g.fromJson(isr, JsonElement.class);
    }
}
