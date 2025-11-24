package models;

import EDD.Lista;

/**
 * Representa un directorio en el sistema de archivos simulado.
 * Contiene archivos y subdirectorios.
 */
public class Directorio {
    private String nombre;
    private Directorio padre; // Directorio padre (null si es raíz)
    private Lista<Directorio> subdirectorios; // Lista de subdirectorios
    private Lista<Archivo> archivos; // Lista de archivos en este directorio
    private String propietario;
    private long fechaCreacion;

    public Directorio(String nombre, Directorio padre, String propietario) {
        this.nombre = nombre;
        this.padre = padre;
        this.subdirectorios = new Lista<>();
        this.archivos = new Lista<>();
        this.propietario = propietario;
        this.fechaCreacion = System.currentTimeMillis();
    }

    /**
     * Agrega un subdirectorio
     */
    public void agregarSubdirectorio(Directorio dir) {
        subdirectorios.insertarFinal(dir);
    }

    /**
     * Agrega un archivo al directorio
     */
    public void agregarArchivo(Archivo archivo) {
        archivos.insertarFinal(archivo);
    }

    /**
     * Elimina un subdirectorio por nombre
     */
    public boolean eliminarSubdirectorio(String nombreDir) {
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio dir = subdirectorios.get(i);
            if (dir.getNombre().equals(nombreDir)) {
                subdirectorios.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina un archivo por nombre
     */
    public boolean eliminarArchivo(String nombreArchivo) {
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = archivos.get(i);
            if (archivo.getNombre().equals(nombreArchivo)) {
                archivos.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Busca un subdirectorio por nombre
     */
    public Directorio buscarSubdirectorio(String nombreDir) {
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            Directorio dir = subdirectorios.get(i);
            if (dir.getNombre().equals(nombreDir)) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Busca un archivo por nombre
     */
    public Archivo buscarArchivo(String nombreArchivo) {
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo archivo = archivos.get(i);
            if (archivo.getNombre().equals(nombreArchivo)) {
                return archivo;
            }
        }
        return null;
    }

    /**
     * Obtiene la ruta completa del directorio
     */
    public String getRutaCompleta() {
        if (padre == null) {
            return "/" + nombre;
        }
        return padre.getRutaCompleta() + "/" + nombre;
    }

    /**
     * Calcula el tamaño total en bloques del directorio y su contenido
     */
    public int getTamanoTotal() {
        int total = 0;

        // Sumar tamaño de archivos
        for (int i = 0; i < archivos.getSize(); i++) {
            total += archivos.get(i).getTamanoEnBloques();
        }

        // Sumar tamaño de subdirectorios recursivamente
        for (int i = 0; i < subdirectorios.getSize(); i++) {
            total += subdirectorios.get(i).getTamanoTotal();
        }

        return total;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Directorio getPadre() {
        return padre;
    }

    public void setPadre(Directorio padre) {
        this.padre = padre;
    }

    public Lista<Directorio> getSubdirectorios() {
        return subdirectorios;
    }

    public Lista<Archivo> getArchivos() {
        return archivos;
    }

    public String getPropietario() {
        return propietario;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
