package com.routesearch.web;

/** Thrown when a requested city is not present in the graph. */
public class UnknownCityException extends RuntimeException {

    private final String city;

    public UnknownCityException(String city) {
        super("Unknown city: '" + city + "'");
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
