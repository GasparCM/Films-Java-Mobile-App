package com.example.appdadm.objetos;

import android.widget.ImageView;

import java.io.Serializable;

public class Pelicula implements Serializable {
    private int id;
    private String idUsuario;
    private int idPlataforma;
    private int calificacion;
    private String duracionMinutos;

    private String caratuaUrl;
    private String titulo;
    private String genero;


    public Pelicula( int idPlataforma,String idUsuario, int calificacion, String duracionMinutos,String titulo,String genero,String imageUrl){
        this.idUsuario = idUsuario;
        this.idPlataforma = idPlataforma;
        this.calificacion = calificacion;
        this.duracionMinutos = duracionMinutos;
        this.titulo = titulo;
        this.genero = genero;
        this.caratuaUrl = imageUrl;
    }
    public Pelicula(){

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

    public int getIdPlataforma() {
        return idPlataforma;
    }

    public void setIdPlataforma(int idPlataforma) {
        this.idPlataforma = idPlataforma;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public String getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(String duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
    public String getCaratuaUrl() {
        return caratuaUrl;
    }

    public void setCaratuaUrl(String caratuaUrl) {
        this.caratuaUrl = caratuaUrl;
    }
}
