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
package automatas;

import automatas.presentacion.Modelo;
import automatas.presentacion.Controlador;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.jdom2.JDOMException;

/**
Clase Main
 */
public class aplicacion {


    public static void main(String[] args) throws JDOMException, IOException, JAXBException  {
    Modelo modelo = new Modelo();
    Controlador control = new Controlador(modelo);
    control.dibujar_automata(args);
    }
}
