package com.distalgo.client;

import com.distalgo.client.gui.ClientGUI;
//import com.distalgo.client.service.ClientService;
import com.distalgo.client.service.ClientPublisherService;
import com.distalgo.client.service.ClientService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan
public class ClientApplication implements ApplicationRunner {
    private final static int IP_IDX = 0;
    private final static int PORT_IDX = 1;

    @Autowired
    ClientPublisherService publisherService;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false"); // to enable to show the GUI
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClientService client = new ClientService();
        String[] arguments = args.getSourceArgs();

        System.out.println("arguments provided: ");
        for (String s: arguments) {
            System.out.println(s);
        }

        ObjectMapper objectMapper = new YAMLMapper();
        checkNumArguments(arguments);
        checkPortValidity(arguments[PORT_IDX]);

        String sessionID = UUID.randomUUID().toString();
        String clientIP = arguments[IP_IDX];
        String clientPort = arguments[PORT_IDX];

        Map<String, Object> user = objectMapper.readValue(new File("/Users/moniquew/Downloads/ticketingsystem/client-new/src/main/resources/application.yml"),
                new TypeReference<Map<String, Object>>() { });

        Map<String, Object> server = (Map<String, Object>) user.get("server");

        System.out.println("old port:" + server.get("port"));
        server.put("port", clientPort);
        System.out.println("publisher:" + publisherService);
        client.launchGUI(sessionID, publisherService);
//        ClientGUI clientGUI = new ClientGUI(sessionID, clientIP, clientPort);
    }

    private void checkNumArguments(String[] args) {
        if (args.length != 2) {
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
