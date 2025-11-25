package persistence;

import EDD.Lista;
import models.Archivo;
import models.Bloque;
import models.Directorio;
import filesystem.SimuladorDisco;
import filesystem.GestorArchivos;
import java.io.*;

/**
 * Gestor de persistencia para guardar y cargar el estado del sistema de
 * archivos.
 * Utiliza formato de texto plano para almacenar la información.
 */
public class GestorPersistencia {

    private static final String ARCHIVO_SISTEMA = "sistema_archivos.dat";
    private static final String SEPARADOR = "|||";
    private static final String FIN_SECCION = "###FIN###";

    /**
     * Guarda el estado completo del sistema en un archivo
     */
    public static boolean guardarSistema(GestorArchivos gestor, String rutaArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            SimuladorDisco disco = gestor.getDisco();

            // Guardar información del disco
            writer.println("=== DISCO ===");
            writer.println(disco.getCabezaActual());

            // Guardar estado de cada bloque
            writer.println("=== BLOQUES ===");
            Bloque[] bloques = disco.getBloques();
            for (int i = 0; i < bloques.length; i++) {
                Bloque b = bloques[i];
                // Formato: id|ocupado|siguiente|archivoAsociado|colorR|colorG|colorB
                String linea = b.getId() + SEPARADOR +
                        b.isOcupado() + SEPARADOR +
                        b.getSiguienteBloque() + SEPARADOR +
                        (b.getArchivoAsociado() != null ? b.getArchivoAsociado() : "null") + SEPARADOR +
                        b.getColor().getRed() + SEPARADOR +
                        b.getColor().getGreen() + SEPARADOR +
                        b.getColor().getBlue();
                writer.println(linea);
            }
            writer.println(FIN_SECCION);

            // Guardar estructura de directorios y archivos
            writer.println("=== DIRECTORIOS ===");
            guardarDirectorioRecursivo(writer, gestor.getRaiz(), "");
            writer.println(FIN_SECCION);

            writer.println("=== FIN SISTEMA ===");
            return true;

        } catch (IOException e) {
            System.err.println("Error al guardar el sistema: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda un directorio y todo su contenido recursivamente
     */
    private static void guardarDirectorioRecursivo(PrintWriter writer, Directorio dir, String ruta) {
        String rutaActual = ruta.isEmpty() ? dir.getNombre() : ruta + "/" + dir.getNombre();

        // Guardar info del directorio
        // Formato: DIR|ruta|nombre|propietario
        writer.println("DIR" + SEPARADOR + rutaActual + SEPARADOR +
                dir.getNombre() + SEPARADOR + dir.getPropietario());

        // Guardar archivos del directorio
        Lista<Archivo> archivos = dir.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            Archivo a = archivos.get(i);
            // Formato:
            // FILE|ruta|nombre|tamaño|primerBloque|propietario|esPublico|colorR|colorG|colorB
            writer.println("FILE" + SEPARADOR + rutaActual + SEPARADOR +
                    a.getNombre() + SEPARADOR +
                    a.getTamanoEnBloques() + SEPARADOR +
                    a.getPrimerBloque() + SEPARADOR +
                    a.getPropietario() + SEPARADOR +
                    a.isEsPublico() + SEPARADOR +
                    a.getColor().getRed() + SEPARADOR +
                    a.getColor().getGreen() + SEPARADOR +
                    a.getColor().getBlue());
        }

        // Recursivamente guardar subdirectorios
        Lista<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getSize(); i++) {
            guardarDirectorioRecursivo(writer, subdirs.get(i), rutaActual);
        }
    }

    /**
     * Carga el estado del sistema desde un archivo
     */
    public static boolean cargarSistema(GestorArchivos gestor, String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.out.println("Archivo de sistema no encontrado: " + rutaArchivo);
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            SimuladorDisco disco = gestor.getDisco();
            disco.reiniciar();

            String linea;
            String seccionActual = "";

            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();

                if (linea.startsWith("===")) {
                    if (linea.contains("DISCO")) {
                        seccionActual = "DISCO";
                        // Leer posición de cabeza
                        String cabeza = reader.readLine();
                        if (cabeza != null) {
                            disco.setCabezaActual(Integer.parseInt(cabeza.trim()));
                        }
                    } else if (linea.contains("BLOQUES")) {
                        seccionActual = "BLOQUES";
                    } else if (linea.contains("DIRECTORIOS")) {
                        seccionActual = "DIRECTORIOS";
                    }
                    continue;
                }

                if (linea.equals(FIN_SECCION)) {
                    continue;
                }

                // Procesar según la sección
                switch (seccionActual) {
                    case "BLOQUES":
                        cargarBloque(disco, linea);
                        break;
                    case "DIRECTORIOS":
                        cargarElementoDirectorio(gestor, linea);
                        break;
                }
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error al cargar el sistema: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.err.println("Error en formato de datos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga un bloque desde una línea de texto
     */
    private static void cargarBloque(SimuladorDisco disco, String linea) {
        String[] partes = linea.split("\\|\\|\\|");
        if (partes.length >= 7) {
            int id = Integer.parseInt(partes[0]);
            boolean ocupado = Boolean.parseBoolean(partes[1]);
            int siguiente = Integer.parseInt(partes[2]);
            String archivoAsociado = partes[3].equals("null") ? null : partes[3];
            int r = Integer.parseInt(partes[4]);
            int g = Integer.parseInt(partes[5]);
            int b = Integer.parseInt(partes[6]);

            Bloque bloque = disco.getBloque(id);
            if (bloque != null) {
                bloque.setOcupado(ocupado);
                bloque.setSiguienteBloque(siguiente);
                bloque.setArchivoAsociado(archivoAsociado);
                bloque.setColor(new java.awt.Color(r, g, b));
            }
        }
    }

    /**
     * Carga un elemento (directorio o archivo) desde una línea de texto
     */
    private static void cargarElementoDirectorio(GestorArchivos gestor, String linea) {
        String[] partes = linea.split("\\|\\|\\|");

        if (partes.length < 2)
            return;

        String tipo = partes[0];
        String ruta = partes[1];

        if (tipo.equals("DIR") && partes.length >= 4) {
            String nombre = partes[2];
            String propietario = partes[3];

            // No crear el root, ya existe
            if (!nombre.equals("root")) {
                navegarARuta(gestor, obtenerRutaPadre(ruta));
                gestor.setModoAdministrador(true);
                gestor.crearDirectorio(nombre);
            }

        } else if (tipo.equals("FILE") && partes.length >= 10) {
            String nombre = partes[2];
            int tamano = Integer.parseInt(partes[3]);
            int primerBloque = Integer.parseInt(partes[4]);
            String propietario = partes[5];
            boolean esPublico = Boolean.parseBoolean(partes[6]);
            int r = Integer.parseInt(partes[7]);
            int g = Integer.parseInt(partes[8]);
            int b = Integer.parseInt(partes[9]);

            // Navegar al directorio padre y crear el archivo
            navegarARuta(gestor, ruta);

            // Crear archivo manualmente (los bloques ya están asignados)
            Archivo archivo = new Archivo(nombre, tamano, propietario);
            archivo.setPrimerBloque(primerBloque);
            archivo.setEsPublico(esPublico);
            archivo.setColor(new java.awt.Color(r, g, b));
            gestor.getDirectorioActual().agregarArchivo(archivo);
        }
    }

    /**
     * Navega a una ruta específica en el sistema de archivos
     */
    private static void navegarARuta(GestorArchivos gestor, String ruta) {
        gestor.irARaiz();
        if (ruta == null || ruta.isEmpty() || ruta.equals("root")) {
            return;
        }

        String[] partes = ruta.split("/");
        for (int i = 1; i < partes.length; i++) { // Empezar en 1 para saltar "root"
            if (!partes[i].isEmpty()) {
                gestor.entrarDirectorio(partes[i]);
            }
        }
    }

    /**
     * Obtiene la ruta del directorio padre
     */
    private static String obtenerRutaPadre(String ruta) {
        int ultimoSlash = ruta.lastIndexOf('/');
        if (ultimoSlash <= 0) {
            return "root";
        }
        return ruta.substring(0, ultimoSlash);
    }

    /**
     * Guarda el sistema en la ubicación por defecto
     */
    public static boolean guardarSistema(GestorArchivos gestor) {
        return guardarSistema(gestor, ARCHIVO_SISTEMA);
    }

    /**
     * Carga el sistema desde la ubicación por defecto
     */
    public static boolean cargarSistema(GestorArchivos gestor) {
        return cargarSistema(gestor, ARCHIVO_SISTEMA);
    }

    /**
     * Verifica si existe un archivo de sistema guardado
     */
    public static boolean existeArchivoGuardado() {
        return new File(ARCHIVO_SISTEMA).exists();
    }

    /**
     * Verifica si existe un archivo de sistema guardado en una ruta específica
     */
    public static boolean existeArchivoGuardado(String ruta) {
        return new File(ruta).exists();
    }
}
