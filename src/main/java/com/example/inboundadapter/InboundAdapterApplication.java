package com.example.inboundadapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InboundAdapterApplication {

    @Autowired
    private Camel2CP camel2CP;

    public static void main(String[] args) {
        // Start the Spring application context
        var context = SpringApplication.run(InboundAdapterApplication.class, args);

        try {
            // Retrieve the Camel2CP bean
            Camel2CP camel2CP = context.getBean(Camel2CP.class);

            // Process the message
            camel2CP.processMessage("Hello, Camel!", 42);

            // Stop the Camel2CP context
            camel2CP.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Stop the Spring application context
            SpringApplication.exit(context);
        }
    }
}