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

                if ("stop".equals(cmd)) {
                    serverService.stopServer();
                    break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}