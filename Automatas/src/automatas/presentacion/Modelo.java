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
package automatas.presentacion;

import automatas.logica.Estado;
import automatas.logica.Transicion;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;

/**
 * Clase contenedora
 */
public class Modelo {

    private List<String> alfabeto;
    private List<Estado> estados;
    private List<Element> transiciones;
    ArrayList<ArrayList> matriz_estados;
    private List<String> estados_iniciales;
    private List<String> estados_finales;
    private List<String> estados_iniciales2;
    private List<String> estados_finales2;

    public List<String> getEstados_iniciales2() {
        return estados_iniciales2;
    }

    public void setEstados_iniciales2(List<String> estados_iniciales2) {
        this.estados_iniciales2 = estados_iniciales2;
    }

    public List<String> getEstados_finales2() {
        return estados_finales2;
    }

    public void setEstados_finales2(List<String> estados_finales2) {
        this.estados_finales2 = estados_finales2;
    }

    public Modelo() {
        this.alfabeto = new ArrayList();
        this.estados = new ArrayList();
        this.transiciones = new ArrayList();
        this.matriz_estados = new ArrayList();
        this.estados_iniciales = new ArrayList();
        this.estados_finales = new ArrayList();
        this.estados_iniciales2 = new ArrayList();
        this.estados_finales2 = new ArrayList();
    }

    public List<String> getEstados_iniciales() {
        return estados_iniciales;
    }

    public void setEstados_iniciales(List<String> estados_iniciales) {
        this.estados_iniciales = estados_iniciales;
    }

    public List<String> getEstados_finales() {
        return estados_finales;
    }

    public void setEstados_finales(List<String> estados_finales) {
        this.estados_finales = estados_finales;
    }

    public ArrayList<ArrayList> getMatriz_estados() {
        return matriz_estados;
    }

    public void setMatriz_estados(ArrayList<ArrayList> matriz_estados) {
        this.matriz_estados = matriz_estados;
    }

    public List<Element> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(List<Element> transiciones) {
        this.transiciones = transiciones;
    }

    public List<Estado> getEstados() {
        return estados;
    }

    public void setEstados(List<Estado> estados) {
        this.estados = estados;
    }

    public List<String> getAlfabeto() {
        return alfabeto;
    }

    public void setAlfabeto(List<String> alfabeto) {
        this.alfabeto = alfabeto;
    }

}
