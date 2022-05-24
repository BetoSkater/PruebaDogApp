package com.example.pruebadogapp;

import androidx.annotation.NonNull;

public class Raza {
    //Esta clase no tiene mucho sentido, tenía otra idea en mente. Realmente podría haber usado un List<String>
    private String raza;

    public Raza() {
    }

    public Raza(String raza) {
        this.raza = raza;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }


}
