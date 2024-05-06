package com.distalgo;

import com.distalgo.client.gui.ClientGUI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
//import com.distalgo.saga.callback.CallbackEntity;
//import com.distalgo.saga.callback.CallbackRepo;
//import com.distalgo.saga.callback.CallbackService;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.SpringApplication;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

//@Component
public class Client {
    private final static int IP_IDX = 0;
    private final static int PORT_IDX = 1;

//    @Autowired
//    static CallbackRepo callbackRepo;


//    private CallbackService callbackServicel = new CallbackService();

//    @PostConstruct
    public static void main (String[] args) throws IOException {
        ObjectMapper objectMapper = new YAMLMapper();
        Client client = new Client();

        client.checkNumArguments(args);
        client.checkPortValidity(args[PORT_IDX]);

//        System.setProperty("server.address", String.valueOf(args[IP_IDX]));
//        System.setProperty("server.port", String.valueOf(args[PORT_IDX]));

        Map<String, Object> user = objectMapper.readValue(new File("/Users/moniquew/Downloads/ticketingsystem/client-old/src/main/resources/application.yml"),
                new TypeReference<Map<String, Object>>() { });

        Map<String, Object> server = (Map<String, Object>) user.get("server");

        System.out.println("old port:" + server.get("port"));

        String sessionID = UUID.randomUUID().toString();
        String clientIP = args[IP_IDX];
        String clientPort = args[PORT_IDX];

        server.put("port", args[PORT_IDX]);

        System.out.println("new port: " + server.get("port"));
        System.out.println("storing callback url");
        ClientGUI clientGUI = new ClientGUI(sessionID, clientIP, clientPort);

    }

    private void checkNumArguments(String[] args) {
        if (args.length != 2) {
            System.out.println("args: " + args);
            System.err.println("Please enter the correct number of arguments.");
            System.err.println("Format: <ip_address> <port>");
            System.exit(1);
        }
    }

    private void checkPortValidity(String port) {
        int portNum = Integer.parseInt(port);

        // port ranging from 0 to 1023 are well-known ports, so won't be valid here, just for safety
        if ((portNum >= 1024) && (portNum <= 65535)) {
            // all is good
        } else {
            System.err.println("Invalid port number, please try a different port.");
            System.exit(1);
        }
    }
}