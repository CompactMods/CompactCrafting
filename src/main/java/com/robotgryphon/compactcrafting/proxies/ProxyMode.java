package com.robotgryphon.compactcrafting.proxies;

public enum ProxyMode {
    RESCAN("rescan"),
    PROGRESS("progress"),
    MATCH("match");

    private final String name;
    ProxyMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
