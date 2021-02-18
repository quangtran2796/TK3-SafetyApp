package com.example.tk3_safetyapp.backend.supermarket;

import android.util.Pair;

import java.util.ArrayList;

public class SupermarketWay extends Supermarket {
    private ArrayList<Pair<Double, Double>> cornerLocations;

    public SupermarketWay(String name, ArrayList<Pair<Double, Double>> cornerLocations) {
        super(name);
        this.cornerLocations = cornerLocations;
    }

    @Override
    public double distance(double longitude, double latitude) {
        return 0;
    }
}
