/*
Universidad Nacional de COsta Rica
Facultad de Ciencias Exactas y Naturales
Escuela de Informática

Proyecto #1
Paradigmas de programación

Programa elaborado por:
->Kevin Morales Marin.

II Ciclo, 2016
 */
package automatas.logica;

/**
Clase entidad que define las caracteristicas definidas por JFLAP para las transiciones.
 */
public class Transicion {
    String origen; //Estado de origen
    String destino;//Estado de destino
    String simbolo;//Simbolo leído

    public Transicion(String origen, String destino, String simbolo) {
        this.origen = origen;
        this.destino = destino;
        this.simbolo = simbolo;
    }
    
     public Transicion() {
        this.origen = "";
        this.destino = "";
        this.simbolo = "";
    }
     
    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }
     
}
