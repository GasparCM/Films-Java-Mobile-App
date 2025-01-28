package com.example.appdadm.objetos;

public class ListElementPlataf {

    public String nombre;
    public String color;
    public String url;

    public ListElementPlataf(String nombre, String color, String url) {
        this.nombre = nombre;
        this.color = color;
        this.url = url;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
