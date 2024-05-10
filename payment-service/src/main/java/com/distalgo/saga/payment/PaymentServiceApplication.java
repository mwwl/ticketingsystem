package com.distalgo.saga.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(PaymentServiceApplication.class);
        } catch (Exception e) {
            System.out.println("Error with setting up the service");
            System.exit(1);
        }
    }
}
