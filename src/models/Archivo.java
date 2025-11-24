package models;

import java.awt.Color;
import java.util.Random;

/**
 * Representa un archivo en el sistema de archivos simulado.
 */
public class Archivo {
    private String nombre;
    private int tamanoEnBloques; // Cantidad de bloques que ocupa
    private int primerBloque; // Dirección del primer bloque en el disco
    private String propietario; // Usuario que creó el archivo
    private boolean esPublico; // Si es accesible por todos los usuarios
    private Color color; // Color para visualización
    private long fechaCreacion;
    private long fechaModificacion;

    public Archivo(String nombre, int tamanoEnBloques, String propietario) {
        this.nombre = nombre;
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = -1; // Se asigna cuando se crean los bloques
        this.propietario = propietario;
        this.esPublico = false;
        this.color = generarColorAleatorio();
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaModificacion = this.fechaCreacion;
    }

    /**
     * Genera un color aleatorio para el archivo
     */
    private Color generarColorAleatorio() {
        Random random = new Random();
        // Evitamos colores muy claros para que se vean bien
        int r = random.nextInt(200) + 30;
        int g = random.nextInt(200) + 30;
        int b = random.nextInt(200) + 30;
        return new Color(r, g, b);
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.fechaModificacion = System.currentTimeMillis();
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    public void setTamanoEnBloques(int tamanoEnBloques) {
        this.tamanoEnBloques = tamanoEnBloques;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }

    public void setPrimerBloque(int primerBloque) {
        this.primerBloque = primerBloque;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public boolean isEsPublico() {
        return esPublico;
    }

    public void setEsPublico(boolean esPublico) {
        this.esPublico = esPublico;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public long getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(long fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
