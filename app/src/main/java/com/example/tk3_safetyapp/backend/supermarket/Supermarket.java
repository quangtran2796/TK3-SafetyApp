package com.example.tk3_safetyapp.backend.supermarket;

import android.util.Pair;

/**
 * Abstract class of a supermarket
 */
public abstract class Supermarket {
    private String name;

    public Supermarket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected Pair<Double, Double> degToUTM(double Lat, double Lon) {
        int zone = (int) Math.floor(Lon / 6 + 31);
        double easting;
        double northing;
        char letter;

        if (Lat < -72)
            letter = 'C';
        else if (Lat < -64)
            letter = 'D';
        else if (Lat < -56)
            letter = 'E';
        else if (Lat < -48)
            letter = 'F';
        else if (Lat < -40)
            letter = 'G';
        else if (Lat < -32)
            letter = 'H';
        else if (Lat < -24)
            letter = 'J';
        else if (Lat < -16)
            letter = 'K';
        else if (Lat < -8)
            letter = 'L';
        else if (Lat < 0)
            letter = 'M';
        else if (Lat < 8)
            letter = 'N';
        else if (Lat < 16)
            letter = 'P';
        else if (Lat < 24)
            letter = 'Q';
        else if (Lat < 32)
            letter = 'R';
        else if (Lat < 40)
            letter = 'S';
        else if (Lat < 48)
            letter = 'T';
        else if (Lat < 56)
            letter = 'U';
        else if (Lat < 64)
            letter = 'V';
        else if (Lat < 72)
            letter = 'W';
        else
            letter = 'X';
        easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) / 3) + 500000;
        easting = Math.round(easting * 100) * 0.01;
        northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos((Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 3);
        if (letter < 'M')
            northing = northing + 10000000;
        northing = Math.round(northing * 100) * 0.01;

        return new Pair<>(easting, northing);
    }

    /**
     * Calculates the distance between this supermarket and the given location in meter
     */
    public abstract double distance(double longitude, double latitude);
}
