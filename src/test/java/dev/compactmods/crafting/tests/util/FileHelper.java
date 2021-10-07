package dev.compactmods.crafting.tests.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class FileHelper {

    public static final FileHelper INSTANCE = new FileHelper();

    private FileHelper() {}

    public File getFile(String filename) {
        URL res = getClass().getClassLoader().getResource(filename);
        return new File(res.getFile());
    }

    public InputStreamReader openFile(String filename) {
        URL res = getClass().getClassLoader().getResource(filename);
        try {
            InputStream inputStream = res.openStream();
            return new InputStreamReader(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public JsonElement getJsonFromFile(String filename) {
        Gson g = new Gson();
        InputStreamReader isr = openFile(filename);
        return g.fromJson(isr, JsonElement.class);
    }
}
