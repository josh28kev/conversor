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

import java.util.ArrayList;
import java.util.List;

/**
Clase de entidad, en la que se define los estados según las carácteristicas que utiliza JFLAP.
Tiene un array para almacenar las transiciones que tiene el estado* 
 */
public class Estado {
    String id; //ID del estado
    String nombre; //Nombre del estado
    String tipo;//Final, Inicial o normal
    List<Transicion> transiciones;//Transiciones 

    public Estado(String id, String nombre, String tipo, List<Transicion> transiciones) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.transiciones = transiciones;
    }

     public Estado() {
        this.id = "";
        this.nombre = "";
        this.tipo = "";
        this.transiciones = new ArrayList();
    }
     
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<Transicion> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(List<Transicion> transiciones) {
        this.transiciones = transiciones;
    }

   
    
}
