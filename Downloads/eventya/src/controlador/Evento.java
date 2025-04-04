/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.util.Date;

/**
 *
 * @author rye
 */
public class Evento {
private Date Fechainicio;
private int Turno;
private int Cupo;
private int Paquete;
private int Presonal;
private String Extras;

    public Evento() {
    }

    public Evento(Date Fechainicio, int Turno, int Cupo, int Paquete, int Presonal, String Extras) {
        this.Fechainicio = Fechainicio;
        this.Turno = Turno;
        this.Cupo = Cupo;
        this.Paquete = Paquete;
        this.Presonal = Presonal;
        this.Extras = Extras;
    }

    public Date getFechainicio() {
        return Fechainicio;
    }

    public void setFechainicio(Date Fechainicio) {
        this.Fechainicio = Fechainicio;
    }

    public int getTurno() {
        return Turno;
    }

    public void setTurno(int Turno) {
        this.Turno = Turno;
    }

    public int getCupo() {
        return Cupo;
    }

    public void setCupo(int Cupo) {
        this.Cupo = Cupo;
    }

    public int getPaquete() {
        return Paquete;
    }

    public void setPaquete(int Paquete) {
        this.Paquete = Paquete;
    }

    public int getPresonal() {
        return Presonal;
    }

    public void setPresonal(int Presonal) {
        this.Presonal = Presonal;
    }

    public String getExtras() {
        return Extras;
    }

    public void setExtras(String Extras) {
        this.Extras = Extras;
    }

    @Override
    public String toString() {
        return "Evento{" 
                + "Fechainicio=" + Fechainicio 
                + ", Turno=" + Turno 
                + ", Cupo=" + Cupo 
                + ", Paquete=" + Paquete 
                + ", Presonal=" + Presonal 
                + ", Extras=" + Extras + '}';
    }

            

}
