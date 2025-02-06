package com.example.appdadm.objetos;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.Serializable;

public class Plataforma implements Serializable {

    private int id;
    private String idUsuario;
    private String imageUrl;
    private String nombre;
    private String url;
    private String password;
    private String emailUsuarioAcceso;
    //

    public Plataforma(String idUsuario,String nombre, String url, String password, String imageUrl) {
        this.idUsuario = idUsuario;
        this.imageUrl = imageUrl;
        this.nombre = nombre;
        this.url = url;
        this.password = password;
    }
    public Plataforma(){

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
