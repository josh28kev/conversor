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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.validation.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.DOMImplementation;
import org.xml.sax.SAXException;

/**
 * Clase principal desde la cual se transforma a no determinista el autómata
 * ubicado en la dirección definida por el usuario.
 */
public class Controlador {

    Modelo modelo; //Instancia de la clase modelo

    public Controlador(Modelo modelo) {
        //Constructor de la clase
        this.modelo = modelo;
    }

    public void dibujar_automata(String[] ruta1) throws JDOMException, IOException, JAXBException {
        /*Desde este método, el programa llama a todas las funciones necesarias para determinar que
        el automata sea determinista, para reducirlo o para convertirlo a determinista*/
        try {
            String ruta = ruta1[0];
            boolean tipo = false;
            if (validarXML(ruta)) {
                SAXBuilder builder = new SAXBuilder();

                File xmlFile = new File(ruta);

                Document document = (Document) builder.build(xmlFile);//Lee el archivo .jff del usuario
                definirAlfabeto(document);
                leer_Estados_Transiciones(document);
                if (validar_transiciones()) {
                    tipo = this.determinar_tipo_automata();
                    if (tipo || modelo.getEstados().size() == 1) {
                        reducir_Automata();
                        System.out.println("El automata ya es determinista!!");
                        System.out.println("Automata reducido correctamente!!");
                    }
                    if (!tipo) {
                        this.elimina_transiciones_vacias();
                    }
                    crear_tablaTransiciones();
                    modificar_Nombres_Estados();
                    try {
                        generar_Xml(ruta);
                    } catch (Exception e) {
                        System.out.println("Error escribiendo el archivo!!");
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Ruta vacía");
        }
    }

    public boolean validarXML(String XML) throws JAXBException, IOException {
        /*Desde este método se valida que el archivo .jff sea valido. Para lograrlo,
        se compara con el archivo xsd que esta en este proyecto  */
        try {
            String XSD = "xsd.xsd";
            SchemaFactory factory
                    = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema esquema = factory.newSchema(getClass().getClassLoader().getResource(XSD));//Obtiene el archivo .xsd
            Validator parser = esquema.newValidator();
            parser.validate(new StreamSource(new File(XML)));
        } catch (SAXException e) {
            System.out.println("Exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    void elimina_transiciones_vacias() {
        /*Este método reduce el autómata para eliminar las transiciones vacías. Va leyendo cada estado y 
        busca si tiene ransiciones vacías, si las tiene realiza el proceso para eliminarlas.*/
        Estado e1 = new Estado();
        for (int i = 0; i < modelo.getEstados().size(); i++) {
            for (int j = 0; j < modelo.getEstados().get(i).getTransiciones().size(); j++) {
                if (modelo.getEstados().get(i).getTransiciones().get(j).getSimbolo().equals("λ")) {

                    e1 = estado_siguiente(modelo.getEstados().get(i).getTransiciones().get(j).getDestino());
                    for (int x = 0; x < e1.getTransiciones().size(); x++) {
                        modelo.getEstados().get(i).getTransiciones().add(e1.getTransiciones().get(x));
                    }
                    modelo.getEstados().get(i).getTransiciones().remove(j);
                    modelo.getEstados().remove(e1);
                    j = -1;
                }
            }
        }
    }

    boolean validar_transiciones() {
        /*Este metodo valida si el estado destino de la transición existe*/
        for (int i = 0; i < modelo.getEstados().size(); i++) {
            for (int j = 0; j < modelo.getEstados().get(i).getTransiciones().size(); j++) {
                if (!encuentra_estadoTo(modelo.getEstados().get(i).getTransiciones().get(j).getDestino())) {
                    System.out.println("El estado " + modelo.getEstados().get(i).getTransiciones().get(j).getDestino() + " no existe!!");
                    System.out.println("Por favor revise el destino de las transiciones!!");
                    return false;
                }
            }
        }
        return true;
    }

    boolean encuentra_estadoTo(String to) {
        /*Este método retorna true si encontró el estado que corresponde con el id que recibió por parámetros*/
        for (int i = 0; i < modelo.getEstados().size(); i++) {
            if (modelo.getEstados().get(i).getId().equals(to)) {
                return true;
            }
        }
        return false;
    }

    public void definirAlfabeto(Document document) {
        /*Este método se encarga de leer los símbolos de las transiciones del archivo
        del usuario para definir el alfabeto del autómata.*/
        Element raiz = document.getRootElement();
        Element hijos = raiz.getChild("automaton");
        List<Element> automata = hijos.getChildren("transition");
        List<String> alfabeto = new ArrayList<String>();
        for (Element hijo : automata) {
            String nombre = String.valueOf(hijo.getChildText("read"));
            if (alfabeto.contains(nombre) == false && !nombre.equals("")) {
                alfabeto.add(nombre);
            }
        }
        modelo.setAlfabeto(alfabeto);//Asigna el alfabeto a una variable del modelo para acceder al mismo de forma rápidda.
    }

    boolean determinar_tipo_automata() {
        /*Llama a otros métodos para determinar si el automata es o no determinista*/
        if (!parsear_Transiciones() || !parsear_Simbolos()) {
            System.out.println("Automata no determinista!!");
            return false;
        }
        return true;
    }

    void reducir_Automata() {
        /*Este método revisa las transiciones del autómata para determinar si son repetidas y eliminarlas*/
        List<Estado> estados = modelo.getEstados();
        if (estados.size() != 1) {
            Estado e1 = null;
            List<Transicion> transiciones = new ArrayList();
            Estado estado_final = new Estado(String.valueOf(estados.size()), "q" + String.valueOf(estados.size()), "final", transiciones);
            estados.add(estado_final);
            for (int i = 0; i < estados.size() - 1; i++) {
                for (int j = 0; j < estados.get(i).getTransiciones().size(); j++) {
                    e1 = this.estado_siguiente(estados.get(i).getTransiciones().get(j).getDestino());
                    if (e1 != null) {
                        if (e1.getTipo().equals("final")) {

                            for (int k = 0; k < estados.get(i).getTransiciones().size(); k++) {
                                if (estados.get(i).getTransiciones().get(k).getDestino().equals(e1.getId())) {
                                    estados.get(i).getTransiciones().get(k).setDestino(estado_final.getId());
                                }
                            }

                        }
                    }
                }
            }
            for (int h = 0; h < estados.size() - 1; h++) {
                if (estados.get(h).getTipo().equals("final")) {
                    estados.remove(h);
                }
            }
            modelo.setEstados(estados);
        }
    }

    Estado estado_siguiente(String id) {
        /*Retorna el estado destino de latransicion que recibe por paránetros*/
        List<Estado> estados = modelo.getEstados();
        for (int i = 0; i < estados.size(); i++) {
            if (estados.get(i).getId().equals(id)) {
                return estados.get(i);
            }
        }
        return null;
    }

    public void leer_Estados_Transiciones(Document document) {
        /*Lee los estados y las transiciones del autómata e el archivo .jff del usuario*/
        Element raiz = document.getRootElement();
        Element hijos = raiz.getChild("automaton");
        hijos = raiz.getChild("automaton");
        List<Estado> estados = new ArrayList();
        List<Element> estados_doc = hijos.getChildren("state");
        List<Element> transicion_doc = hijos.getChildren("transition");
        List<Transicion> transiciones_estado;
        for (Element estad : estados_doc) {//Lee los estados del archivo
            transiciones_estado = new ArrayList();

            String id = String.valueOf(estad.getAttribute("id")).substring(16);
            int indice = 0;
            for (indice = 0; indice < id.length() && id.charAt(indice) != '"'; indice++) {
            }
            id = id.substring(0, indice);
            String nombre = String.valueOf(estad.getAttribute("name")).substring(18);
            for (indice = 0; indice < nombre.length() && nombre.charAt(indice) != '"'; indice++) {
            }
            nombre = nombre.substring(0, indice);
            String tipo = "";
            if (estad.getChildText("initial") != null) {
                tipo += "initial";
            }
            if (estad.getChildText("final") != null) {
                tipo += "final";
            }
            for (Element trans : transicion_doc) {//Lee las transiciones del archivo y se las asigna al estado de origen
                if (String.valueOf(trans.getChildText("from")).equals(id)) {
                    String origen = trans.getChildText("from");
                    String destino = trans.getChildText("to");
                    String simbolo = trans.getChildText("read");
                    if (simbolo.equals("")) {
                        simbolo = "λ";//Transicion vacía
                    }
                    transiciones_estado.add(new Transicion(origen, destino, simbolo));
                }
            }
            estados.add(new Estado(id, nombre, tipo, transiciones_estado));
        }
        modelo.setEstados(estados);

    }

    public boolean parsear_Transiciones() {
        /*Revisa si algún estado tiene transiciones repetidas (si no es AFND)*/
        List<Estado> estados = modelo.getEstados();
        Vector<String> vector_estados = new Vector();
        for (Estado estado : estados) {
            vector_estados = new Vector();
            for (int i = 0; i < estado.getTransiciones().size(); i++) {
                if (!vector_estados.contains(estado.getTransiciones().get(i).getSimbolo())) {
                    vector_estados.add(estado.getTransiciones().get(i).getSimbolo());
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean parsear_Simbolos() {
        /*Revisa si el autómata tiene transiciones vacías (es AFND)*/
        for (int i = 0; i < modelo.getEstados().size(); i++) {
            for (int j = 0; j < modelo.getEstados().get(i).getTransiciones().size(); j++) {
                if (modelo.getEstados().get(i).getTransiciones().get(j).getSimbolo().equals("λ")) {//El símbolo que determina una transicón vacía
                    return false;
                }
            }
        }
        return true;
    }

    static String obtenerTransiciones(Estado e1, String transicion) {//Indica el destino de las transiciones repetidas del 
        //estado que recibe por parámetros y retorna el ID de los estados finales separados por coma.
        List<String> tabla_estados = new ArrayList();
        String t = "";
        for (Transicion t1 : e1.getTransiciones()) {
            if (t == "") {
                if (t1.getSimbolo().equals(transicion)) {
                    t = t1.getDestino();
                }
            } else if (t1.getSimbolo().equals(transicion)) {
                t = t + "," + t1.getDestino();
            }
        }
        return t;
    }

    public void crear_tablaTransiciones() {
        /*Convierte el automata AFND a AFD simulando la tabla de transiciones y asignandola a su respectiva 
        instancia en el modelo*/
        ArrayList<ArrayList> matriz_estados = modelo.getMatriz_estados();
        List<Estado> estados = modelo.getEstados();
        String e1;
        if (!matriz_estados.isEmpty()) {
            for (int j = 0; j < matriz_estados.size(); j++) {
                for (int x = 0; x < matriz_estados.get(j).size(); x++) {
                    e1 = String.valueOf(matriz_estados.get(j).get(x));
                    int bandera = revisar_matriz(e1);
                    if (bandera == -1) {
                        if (e1 != "") {
                            ArrayList<String> lista = new ArrayList();
                            lista.add(e1);
                            matriz_estados.add(lista);
                        }
                    } else if (!existe_Estado(e1)) {
                        List<Estado> estados2 = dividir_Estados(e1);
                        //Agrega estados a la matriz.
                        for (Estado e2 : estados2) {
                            if (e2.getTipo().equals("initial") || e2.getTipo().equals("finalinitial") || e2.getTipo().equals("initialfinal")) {
                                modelo.getEstados_iniciales().add(e1);
                            }
                            if (e2.getTipo().equals("final") || e2.getTipo().equals("finalinitial") || e2.getTipo().equals("initialfinal")) {
                                modelo.getEstados_finales().add(e1);
                            }
                            arreglar_Transiciones(bandera, e2);
                        }
                    }
                }
            }

        } else {//Estado inicial
            Estado e2 = estados.get(0);
            arreglar_Transiciones(0, e2);
            ArrayList<String> estados1 = new ArrayList();
            crear_tablaTransiciones();
        }
    }

    public int revisar_matriz(String nombre) {
        ArrayList<ArrayList> matriz_estados = modelo.getMatriz_estados();
        int auxiliar = -1;
        for (int i = 0; i < matriz_estados.size(); i++) {
            if (String.valueOf(matriz_estados.get(i).get(0)).equals(nombre)) {
                auxiliar = i;
                return auxiliar;
            }
        }
        return auxiliar;
    }

    List<Estado> dividir_Estados(String estados) {
        /*Recibe las transicion de la matriz y la divide, por ejemplo, la transicion
        es 1,2,3 y retorna en la lista los estados 1->2->3*/
        String e1 = "";
        String auxiliar = "";
        List<Estado> Lista_estados = new ArrayList();
        List<Estado> Lista_nombres_estados = modelo.getEstados();
        for (int i = 0; i < estados.length(); i++) {
            if (!String.valueOf(estados.charAt(i)).equals(",")) {
                e1 = e1 + estados.charAt(i);
            }
            auxiliar = String.valueOf(estados.charAt(i));
            if (String.valueOf(estados.charAt(i)).equals(",") || i == estados.length() - 1) {
                for (Estado e2 : Lista_nombres_estados) {
                    if (e2.getId().equals(e1)) {
                        Lista_estados.add(e2);
                    }
                }
                e1 = "";
            }
        }
        return Lista_estados;
    }

    boolean existe_Estado(String id) {
        /*Revisa si el estado que corresponde con el id que recibe por parámetros existe*/
        for (int i = 0; i < modelo.getMatriz_estados().size(); i++) {
            if (String.valueOf(modelo.getMatriz_estados().get(i)).equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void arreglar_Transiciones(int index, Estado e1) {
        /*Agrega la transicion que recibe por parámetros a la matriz de transiciones*/
        try {
            List<Estado> estados = modelo.getEstados();
            List<String> alfabeto = modelo.getAlfabeto();
            List<String> tabla_transiciones = new ArrayList();
            ArrayList<ArrayList> matriz_estados = modelo.getMatriz_estados();
            ArrayList<String> array_transiciones = new ArrayList();
            String estado = "";
            if (matriz_estados.isEmpty()) {
                array_transiciones.add(e1.getId());
                matriz_estados.add(array_transiciones);
            }
            for (int i = 0; i < alfabeto.size(); i++) {//Recorre el alfabeto y determina las transacciones que
                //corresponden con el simbolo especifico.
                if (matriz_estados.get(index).size() < alfabeto.size() + 1) {
                    matriz_estados.get(index).add(obtenerTransiciones(e1, alfabeto.get(i)));
                } else {
                    String auxiliar = String.valueOf(matriz_estados.get(index).get(i));
                    String auxiliar2 = obtenerTransiciones(e1, alfabeto.get(i));
                    if (auxiliar2 != "") {
                        auxiliar = auxiliar + "," + obtenerTransiciones(e1, alfabeto.get(i));
                    } else if (!auxiliar2.equals(auxiliar) && auxiliar2 == "") {
                        auxiliar += obtenerTransiciones(e1, alfabeto.get(i));
                        matriz_estados.get(index).remove(i);
                        matriz_estados.get(index).add(i, (Object) auxiliar);
                    }
                }
            }
            modelo.setMatriz_estados(matriz_estados);
        } catch (Exception e) {
            System.out.println("Error de parseo");
        }
    }

    ArrayList<ArrayList> clonar_Matriz() {
        /*Clona la matriz de estados. creando una de las mismas dimensiones 
        pero vacia(a cada campo le asigna "")*/
        ArrayList<ArrayList> matriz_estados = modelo.getMatriz_estados();
        ArrayList<ArrayList> matriz_nueva = new ArrayList();
        ArrayList<String> array = new ArrayList();
        for (int i = 0; i < matriz_estados.size(); i++) {
            for (int k = 0; k < matriz_estados.get(i).size(); k++) {
                array.add("");
            }
            matriz_nueva.add(array);
            array = new ArrayList();
        }
        return matriz_nueva;
    }

    void modificar_Nombres_Estados() {
        /*Modifica los nombres de las transiciones en la matriz para que jflap pueda reconocerlos*/
        ArrayList<ArrayList> matriz_estados = modelo.getMatriz_estados();
        ArrayList<ArrayList> matriz_nueva = clonar_Matriz();
        matriz_nueva.get(0).set(0, "0");
        String aux;
        revisarEstadosFinInic("0", 0);
        if (matriz_estados.size() == 1) {
            matriz_nueva = matriz_estados;
        } else {
            for (int i = 1; i < matriz_estados.size(); i++) {//Agrega a la matriz nueva las transiciones renombradas
                aux = String.valueOf(matriz_estados.get(i).get(0));
                matriz_nueva.get(i).set(0, String.valueOf(i));
                for (int k = 0; k < matriz_estados.size(); k++) {
                    for (int j = 1; j < matriz_estados.get(k).size(); j++) {
                        if (String.valueOf(matriz_estados.get(k).get(j)).equals(aux) && (String.valueOf(modelo.getMatriz_estados().get(k).get(j)).equals(aux))) {
                            revisarEstadosFinInic(aux, i);
                            matriz_nueva.get(k).set(j, String.valueOf(i));
                        }
                    }
                }
            }
        }

        modelo.setMatriz_estados(matriz_nueva);
    }

    void revisarEstadosFinInic(String estado, int i) {
        /*Determina si los nuevos estados son finales   no, comparandolos con los anteriores*/
        for (int k = 0; k < modelo.getEstados_iniciales().size(); k++) {
            if (modelo.getEstados_iniciales().contains(estado)) {
                modelo.getEstados_iniciales2().add(String.valueOf(i));
            }
        }

        for (int j = 0; j < modelo.getEstados_finales().size(); j++) {
            if (modelo.getEstados_finales().contains(estado)) {
                modelo.getEstados_finales2().add(String.valueOf(i));
            }
        }
    }

    public String conseguirRuta(String ruta) {
        /*Obtiene la ruta del archivo que el usuario ingreso, para luego guardar el AFD en la misma dirección*/
        for (int k = ruta.length() - 1; k > 0; k--) {
            if (ruta.charAt(k) == '\\' || ruta.charAt(k) == '/') {
                return ruta.substring(0, k + 1);
            }
        }
        return ruta;
    }

    void generar_Xml(String ruta) throws ParserConfigurationException, IOException {
        /*Genera un nevo archivo .jff con el automata determinista. Para generarlo, utiliza 
        la matriz de transiciones creada anteriormente con los nuevos estados*/
        ArrayList<ArrayList> matriz_estados = modelo.getMatriz_estados();
        Element estructura = new Element("structure");
        Document documento = new Document(estructura);
        String nueva_ruta = conseguirRuta(ruta);
        double coordenada_X = 48.0;
        double coordenada_Y = 41.0;
        int aux = 0;

        Element type = new Element("type");//crea la etiqueta TYPE
        type.setText("fa");//Agrega el tipo a la etiqueta type
        documento.getRootElement().addContent(type);

        Element automaton = new Element("automaton");//Crea la etiqueta automaton del .jff
        documento.getRootElement().addContent(automaton);

        for (int i = 0; i < matriz_estados.size(); i++) {//Graba los estados del nuevo autmata en el archivo
            aux = (i) % 2;
            Element estado = new Element("state");
            estado.setAttribute("id", String.valueOf(matriz_estados.get(i).get(0)));
            estado.setAttribute("name", "q" + String.valueOf(matriz_estados.get(i).get(0)));
            if (aux == 0) {//Acomoda los estados en forma de cascada
                Element x2 = new Element("x");
                x2.setText(String.valueOf(coordenada_X += 100));

                estado.addContent(x2);

                Element y2 = new Element("y");
                y2.setText(String.valueOf(coordenada_Y));
                estado.addContent(y2);
            } else {
                Element x2 = new Element("x");
                x2.setText(String.valueOf(coordenada_X));
                estado.addContent(x2);

                Element y2 = new Element("y");
                y2.setText(String.valueOf(coordenada_Y += 100));
                estado.addContent(y2);
            }//Determina el estado final o inicial
            if (modelo.getEstados_iniciales2().contains(String.valueOf(matriz_estados.get(i).get(0)))) {
                Element inicial = new Element("initial");
                estado.addContent(inicial);
            }

            if (modelo.getEstados_finales2().contains(String.valueOf(matriz_estados.get(i).get(0)))) {
                Element finale = new Element("final");
                estado.addContent(finale);
            }
            automaton.addContent(estado);
        }

        for (int k = 0; k < matriz_estados.size(); k++) {//Guarda las transiciones en el .jff
            for (int j = 1; j < matriz_estados.get(k).size(); j++) {
                if (!String.valueOf(matriz_estados.get(k).get(j)).equals("")) {
                    Element transicion = new Element("transition");
                    Element from = new Element("from");
                    from.setText(String.valueOf(matriz_estados.get(k).get(0)));
                    transicion.addContent(from);
                    Element to = new Element("to");
                    to.setText(String.valueOf(matriz_estados.get(k).get(j)));
                    transicion.addContent(to);
                    Element read = new Element("read");
                    read.setText(String.valueOf(modelo.getAlfabeto().get(j - 1)));
                    transicion.addContent(read);
                    automaton.addContent(transicion);
                }
            }
        }
        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());
        xml.output(documento, new FileWriter(nueva_ruta + "AFD.jff"));//Guatrda el archivo
        System.out.println("Automata convertido correctamente en " + nueva_ruta + "AFD.jff");
    }

}
