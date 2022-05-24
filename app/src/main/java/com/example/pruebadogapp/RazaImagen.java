package com.example.pruebadogapp;

public class RazaImagen {
    private String raza;
    private String url;

    public RazaImagen() {
    }

    public RazaImagen(String raza, String url) {
        this.raza = raza;
        this.url = url;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
