package com.blue.cacheserver.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerMain {
    public static void main(String[] args) {
        ServerService serverService = new ServerService();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String cmd = br.readLine();

                if ("1".equals(cmd)) {
                    serverService.stopServer();
                    break;
                } else if ("2".equals(cmd)) {
                    int curCacheMemorySize = serverService.getCache().removeAllExpiredEntry();
                    System.out.println("Current cacheMemorySize is " + curCacheMemorySize);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
