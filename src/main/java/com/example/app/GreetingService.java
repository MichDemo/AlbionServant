
package com.example.app;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String getGreeting() {
        return "Hello from Spring @Service";
    }

    public String process(String name) {
        // Simulate some processing work
        return "Processed: " + name.toUpperCase();
    }
}
