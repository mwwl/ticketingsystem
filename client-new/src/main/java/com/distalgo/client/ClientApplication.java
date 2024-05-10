package com.distalgo.client;

import com.distalgo.client.service.ClientPublisherService;
import com.distalgo.client.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import java.util.UUID;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan
public class ClientApplication implements ApplicationRunner {
//    private final static int IP_IDX = 0;
    private final static int PORT_IDX = 1;
    private static String sessionID = null;

    @Autowired
    private Environment environment;

    @Autowired
    private ClientPublisherService publisherService;

    @Autowired
    private ClientService client;


    /**
     * sets the clientPort and groupID of the client before running the application
     *
     * (each client has a different group ID to ensure each client listens to all messages)
     * https://www.infoworld.com/article/3694893/optimize-apache-kafka-by-understanding-consumer-groups.html#:~:text=Kafka%20consumers%20are%20generally%20configured,depends%20on%20your%20use%20case
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            ClientApplication clientApplication = new ClientApplication();
            System.setProperty("java.awt.headless", "false"); // to enable to show the GUI

            clientApplication.checkNumArguments(args);
            String clientPort = args[PORT_IDX];
            clientApplication.checkPortValidity(clientPort);

            sessionID = UUID.randomUUID().toString();
            System.setProperty("server.port", clientPort);

            String groupID = String.format("client-%s", sessionID);
            System.out.println("Group id: " + groupID);
            System.setProperty("spring.kafka.consumer.group-id", groupID);

            SpringApplication.run(ClientApplication.class, args);
        } catch (ApplicationContextException e) {
            System.err.println("Port is already in use, please use a different port.");
            System.exit(1);
        } catch (Exception exception) {
            System.err.println("Something went wrong");
            System.exit(1);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.setSessionID(sessionID);
        client.launchGUI(sessionID, publisherService);
    }

    // error handling if number of arguments input is wrong, and if the port is not valid
    // but have to handle the case where the same input is given though
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
