package com.ftgo.common.domain;

import jakarta.persistence.Embeddable;
import lombok.Value;

@Embeddable
@Value
public class Address {
    String street1;
    String street2;
    String city;
    String state;
    String zip;
    String country;

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

