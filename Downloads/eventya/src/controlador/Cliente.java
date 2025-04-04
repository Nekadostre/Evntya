/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

/**
 *
 * @author rye
 */
public class Cliente {
private String Nombre;
private String ApellidoP ;
private String ApellidoM;
private String Telefono;
private String RFC;

    public Cliente() {
    }

    public Cliente(String Nombre, String ApellidoP, String ApellidoM, String Telefono, String RFC) {
        this.Nombre = Nombre;
        this.ApellidoP = ApellidoP;
        this.ApellidoM = ApellidoM;
        this.Telefono = Telefono;
        this.RFC = RFC;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String Nombre) {
        this.Nombre = Nombre;
    }

    public String getApellidoP() {
        return ApellidoP;
    }

    public void setApellidoP(String ApellidoP) {
        this.ApellidoP = ApellidoP;
    }

    public String getApellidoM() {
        return ApellidoM;
    }

    public void setApellidoM(String ApellidoM) {
        this.ApellidoM = ApellidoM;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String Telefono) {
        this.Telefono = Telefono;
    }

    public String getRFC() {
        return RFC;
    }

    public void setRFC(String RFC) {
        this.RFC = RFC;
    }

    @Override
    public String toString() {
        return "Cliente{" 
             + "Nombre=" + Nombre 
             + ", ApellidoP=" + ApellidoP 
             + ", ApellidoM=" + ApellidoM 
             + ", Telefono=" + Telefono 
             + ", RFC=" + RFC + '}';
    }






    
}
