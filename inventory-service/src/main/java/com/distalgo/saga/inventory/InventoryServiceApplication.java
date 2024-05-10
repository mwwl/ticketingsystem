package com.distalgo.saga.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(InventoryServiceApplication.class);
        } catch (Exception e) {
            System.out.println("Error with setting up the service");
            System.exit(1);
        }
    }
}
