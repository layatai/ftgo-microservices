package com.ftgo.common.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Address {
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;

    // Public no-arg constructor for JPA and Jackson JSON deserialization
    public Address() {
        // JPA requires a no-arg constructor for embeddables
        // Jackson also needs it for JSON deserialization
    }

    public Address(String street1, String street2, String city, String state, String zip, String country) {
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country != null ? country : "USA";
    }

    public Address(String street1, String city, String state, String zip) {
        this(street1, null, city, state, zip, "USA");
    }
}

