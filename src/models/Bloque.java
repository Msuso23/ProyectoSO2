package models;

import java.awt.Color;

/**
 * Representa un bloque de almacenamiento en el disco simulado.
 * Usado en la asignación encadenada.
 */
public class Bloque {
    private int id; // Número de bloque (0 a N-1)
    private boolean ocupado; // Si está ocupado o libre
    private int siguienteBloque; // Puntero al siguiente bloque (-1 si es el último)
    private String archivoAsociado; // Nombre del archivo que ocupa este bloque
    private Color color; // Color para visualización

    public Bloque(int id) {
        this.id = id;
        this.ocupado = false;
        this.siguienteBloque = -1;
        this.archivoAsociado = null;
        this.color = Color.WHITE; // Blanco = libre
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }

    public int getSiguienteBloque() {
        return siguienteBloque;
    }

    public void setSiguienteBloque(int siguienteBloque) {
        this.siguienteBloque = siguienteBloque;
    }

    public String getArchivoAsociado() {
        return archivoAsociado;
    }

    public void setArchivoAsociado(String archivoAsociado) {
        this.archivoAsociado = archivoAsociado;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Libera el bloque para que pueda ser reutilizado
     */
    public void liberar() {
        this.ocupado = false;
        this.siguienteBloque = -1;
        this.archivoAsociado = null;
        this.color = Color.WHITE;
    }

    /**
     * Asigna el bloque a un archivo
     */
    public void asignar(String nombreArchivo, Color color) {
        this.ocupado = true;
        this.archivoAsociado = nombreArchivo;
        this.color = color;
    }

    @Override
    public String toString() {
        return "Bloque{" +
                "id=" + id +
                ", ocupado=" + ocupado +
                ", siguiente=" + siguienteBloque +
                ", archivo='" + archivoAsociado + '\'' +
                '}';
    }
}
