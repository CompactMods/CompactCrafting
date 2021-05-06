package com.robotgryphon.compactcrafting.tests.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class FileHelper {

    public static final FileHelper INSTANCE = new FileHelper();

    private FileHelper() {}

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
