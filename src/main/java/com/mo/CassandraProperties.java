package com.mo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "datastax-java-driver.basic")
public class CassandraProperties {
    private String contactPoints;

    // Getters and setters

    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(String contactPoints) {
        this.contactPoints = contactPoints;
    }
}
