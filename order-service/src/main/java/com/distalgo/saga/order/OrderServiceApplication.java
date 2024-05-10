package com.distalgo.saga.order;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(OrderServiceApplication.class);
        } catch (Exception e) {
            System.out.println("Error with setting up the service");
            System.exit(1);
        }
    }
}
