package org.saadMeddiche;

import org.saadMeddiche.entities.SquareCityAirport;

public class SquareCity {

    private final static SquareCityAirport airport = new SquareCityAirport("The Square Airport", 7070);

    public static void main(String[] args) {
        airport.run();
    }

}