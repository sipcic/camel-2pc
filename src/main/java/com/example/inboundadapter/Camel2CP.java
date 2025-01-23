package com.example.inboundadapter;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Camel2CP {

    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;

    public Camel2CP(CamelContext camelContext) throws Exception {
        this.camelContext = camelContext;
        this.producerTemplate = camelContext.createProducerTemplate();

        // Define the route
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        
                        .log("Publishing message: ${body} with msgid: ${header.msgid}")
                        .to("activemq:queue:msgQueue") // Send the original body to msgQueue
                        .process(exchange -> {
                            // Update the body to msgid for idQueue
                            int msgid = exchange.getIn().getHeader("msgid", Integer.class);
                            exchange.getIn().setBody(msgid);

                            // Add a delay to simulate processing time
                            // Uncomment the next line to enable delay for 2PC testing
                            Thread.sleep(10000); // Wait for 10 seconds

                            // Simulate an exception to test rollback
                            // Uncomment the next line to enable rollback testing
                            // throw new RuntimeException("Simulated exception after msgQueue update");
                        })
                        .to("activemq:queue:idQueue"); // Send the updated body (msgid) to idQueue
            }
        });

        camelContext.start();
    }

    public void processMessage(String message, int msgid) {
        try {
            producerTemplate.sendBodyAndHeader("direct:start", message, "msgid", msgid);
            System.out.println("Message successfully processed.");
        } catch (Exception e) {
            System.err.println("Transaction rolled back due to exception: " + e.getMessage());
        }
    }

    public void stop() throws Exception {
        if (camelContext != null) {
            camelContext.stop();
        }
    }
}