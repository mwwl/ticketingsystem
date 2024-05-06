package com.distalgo;

//import com.distalgo.client.gui.ClientGUI;
import jakarta.persistence.Id;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@SpringBootApplication
public class ClientApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] arguments = args.getSourceArgs();
        checkNumArguments(arguments);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(ApplicationArguments args) {
        return factory -> {
            String clientIP = "127.0.0.1";
            int clientPort = 3001;
            if (args.containsOption("ip")) {
                clientIP = args.getOptionValues("ip").get(0);
            }

            if (args.containsOption("port")) {
                clientPort = Integer.parseInt(args.getOptionValues("port").get(0));
            }

            // Set IP address and port for the server
            try {
                factory.setAddress(InetAddress.getByName(clientIP));
                factory.setPort(clientPort);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        };
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
        // port ranging from 0 to 1023 ae well-known ports, so won't be valid here, just for safety
        if ((portNum >= 1024) && (portNum <= 65535)) {
                // all is good
        } else {
            System.err.println("Invalid port number, please try a different port.");
            System.exit(1);
        }
    }

//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        System.out.println("TRY");
//        System.out.println("BLAHBLAH: " + name);
//        System.out.println(Arrays.toString(args.getSourceArgs()));
//
////         ClientGUI clientGUI = new ClientGUI(sessionID, clientIP, clientPort);
//    }
}