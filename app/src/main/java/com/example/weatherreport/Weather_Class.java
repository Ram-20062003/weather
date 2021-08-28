package com.example.weatherreport;

import java.util.List;

public class Weather_Class {
    private Coord coord;
    private List<Weather> weather;
    private Main main;
    private Wind wind;

    public Coord getCoord() {
        return coord;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }
}
