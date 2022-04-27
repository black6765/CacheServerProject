package com.blue.cacheserver.service;

public class ServerMain {
    public static void main(String[] args) {
        ServerServiceImpl serverService = new ServerServiceImpl();
        serverService.startServer();
    }
}
