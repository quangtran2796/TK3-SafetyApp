package com.example.tk3_safetyapp.backend.supermarket;

import android.util.Pair;

public class SupermarketNode extends Supermarket {
    private double longitude;
    private double latitude;

    public SupermarketNode(String name, double longitude, double latitude) {
        super(name);
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public double distance(double longitude, double latitude) {
        Pair<Double, Double> argLocation = degToUTM(latitude, longitude);
        Pair<Double, Double> supermarketLocation = degToUTM(this.latitude, this.longitude);

        return Math.sqrt(Math.pow(argLocation.first - supermarketLocation.first, 2) + Math.pow(argLocation.second - supermarketLocation.second, 2));
    }
}
