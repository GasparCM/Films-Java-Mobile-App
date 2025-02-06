package com.example.appdadm.objetos;

import java.io.Serializable;
import java.sql.Date;

public class Usuario implements Serializable {
    private String nombre;
    private String apellidos;
    private String email;
    private String preguntaSeg;
    private String respuestaSeg;
    private String intereses;
    private String password;

    private int dobleFactor;
    private Date fechaNac;

    public int getDobleFactor() {
        return dobleFactor;
    }

    public void setDobleFactor(int dobleFactor) {
        this.dobleFactor = dobleFactor;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
    }

    /**
     * Constructor, no estan todos los atributos ya que algunos no son obligatorios
     * @param nombre
     * @param email
     * @param preguntaSeg
     * @param respuestaSeg
     * @param password
     */
    public Usuario(String nombre, String apellidos, String email, String preguntaSeg, String respuestaSeg, String password,int dobleFact) {
        this.nombre = nombre;
        this.email = email;
        this.preguntaSeg = preguntaSeg;
        this.respuestaSeg = respuestaSeg;
        this.password = password;
        this.dobleFactor = dobleFact;
        this.apellidos = apellidos;
    }

    public Usuario(){

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPreguntaSeg() {
        return preguntaSeg;
    }

    public void setPreguntaSeg(String preguntaSeg) {
        this.preguntaSeg = preguntaSeg;
    }

    public String getRespuestaSeg() {
        return respuestaSeg;
    }

    public void setRespuestaSeg(String respuestaSeg) {
        this.respuestaSeg = respuestaSeg;
    }

    public String getIntereses() {
        return intereses;
    }

    public void setIntereses(String intereses) {
        this.intereses = intereses;
    }
}
