package com.blue.cacheserver.service;

import com.blue.cacheserver.cache.Cache;
import com.blue.cacheserver.client.Client;
import com.blue.cacheserver.message.ErrorMessage;
import com.blue.cacheserver.message.ServerException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.Vector;

public class ServerMain {
    public static void main(String[] args) {
        ServerService serverService = new ServerService();
        serverService.startServer();

    }
}
