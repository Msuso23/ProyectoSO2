package persistence;

import EDD.Lista;
import models.Proceso;
import models.SolicitudIO;
import process.GestorProcesos;
import java.io.*;

/**
 * Gestor de persistencia para guardar y cargar los procesos del sistema.
 * Utiliza formato CSV para almacenar la información.
 */
public class GestorPersistencia {

    private static final String ARCHIVO_PROCESOS = "procesos.csv";
    private static final String SEPARADOR = ",";
    private static final String FIN_SECCION = "###FIN###";

    /**
     * Guarda los procesos y solicitudes en un archivo CSV
     */
    public static boolean guardarProcesos(GestorProcesos gestorProcesos, String rutaArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {

            // Guardar información general
            writer.println("=== INFO ===");
            writer.println("posicionCabeza," + gestorProcesos.getPosicionCabeza());
            writer.println("totalSolicitudesAtendidas," + gestorProcesos.getTotalSolicitudesAtendidas());
            writer.println("movimientosTotales," + gestorProcesos.getMovimientosTotales());
            writer.println(FIN_SECCION);

            // Guardar procesos
            writer.println("=== PROCESOS ===");
            writer.println("id,nombre,estado,operacion,archivoObjetivo,propietario,tamanoEnBloques,operacionEjecutada");

            Lista<Proceso> procesos = gestorProcesos.getProcesos();
            for (int i = 0; i < procesos.getSize(); i++) {
                Proceso p = procesos.get(i);
                // Guardar TODOS los procesos como BLOQUEADO y operacionEjecutada=false
                // para que puedan re-ejecutarse al cargar
                String linea = p.getId() + SEPARADOR +
                        escaparCSV(p.getNombre()) + SEPARADOR +
                        "BLOQUEADO" + SEPARADOR +
                        p.getOperacion().name() + SEPARADOR +
                        escaparCSV(p.getArchivoObjetivo()) + SEPARADOR +
                        escaparCSV(p.getPropietario()) + SEPARADOR +
                        p.getTamanoEnBloques() + SEPARADOR +
                        "false";
                writer.println(linea);
            }
            writer.println(FIN_SECCION);

            // Guardar TODAS las solicitudes como pendientes (para re-ejecutar)
            // Combina pendientes + atendidas en una sola sección
            writer.println("=== SOLICITUDES_PENDIENTES ===");
            writer.println("id,procesoId,bloqueDestino,tipoOperacion,atendida");

            // Primero las pendientes actuales
            Lista<SolicitudIO> pendientes = gestorProcesos.getSolicitudesPendientes();
            for (int i = 0; i < pendientes.getSize(); i++) {
                SolicitudIO s = pendientes.get(i);
                String linea = s.getId() + SEPARADOR +
                        s.getProceso().getId() + SEPARADOR +
                        s.getBloqueDestino() + SEPARADOR +
                        s.getTipoOperacion().name() + SEPARADOR +
                        "false";
                writer.println(linea);
            }

            // Luego las atendidas (también como pendientes para re-ejecutar)
            Lista<SolicitudIO> atendidas = gestorProcesos.getSolicitudesAtendidas();
            for (int i = 0; i < atendidas.getSize(); i++) {
                SolicitudIO s = atendidas.get(i);
                String linea = s.getId() + SEPARADOR +
                        s.getProceso().getId() + SEPARADOR +
                        s.getBloqueDestino() + SEPARADOR +
                        s.getTipoOperacion().name() + SEPARADOR +
                        "false";
                writer.println(linea);
            }
            writer.println(FIN_SECCION);

            // Sección vacía de atendidas (ya no se usa pero mantener compatibilidad)
            writer.println("=== SOLICITUDES_ATENDIDAS ===");
            writer.println("id,procesoId,bloqueDestino,tipoOperacion,atendida");
            writer.println(FIN_SECCION);

            writer.println("=== FIN PROCESOS ===");
            return true;

        } catch (IOException e) {
            System.err.println("Error al guardar procesos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Escapa un valor para CSV (maneja comas y comillas)
     */
    private static String escaparCSV(String valor) {
        if (valor == null)
            return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    /**
     * Carga los procesos desde un archivo CSV
     */
    public static boolean cargarProcesos(GestorProcesos gestorProcesos, String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.out.println("Archivo de procesos no encontrado: " + rutaArchivo);
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            // Limpiar el gestor antes de cargar
            gestorProcesos.limpiarTodo();

            // Mapa temporal para asociar procesos por ID
            Lista<Proceso> procesosTemp = new Lista<>();

            String linea;
            String seccionActual = "";
            boolean saltarEncabezado = false;

            int posicionCabeza = 0;

            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();

                if (linea.startsWith("===")) {
                    if (linea.contains("INFO")) {
                        seccionActual = "INFO";
                    } else if (linea.contains("PROCESOS") && !linea.contains("FIN")) {
                        seccionActual = "PROCESOS";
                        saltarEncabezado = true;
                    } else if (linea.contains("SOLICITUDES_PENDIENTES")) {
                        seccionActual = "SOLICITUDES_PENDIENTES";
                        saltarEncabezado = true;
                    } else if (linea.contains("SOLICITUDES_ATENDIDAS")) {
                        seccionActual = "SOLICITUDES_ATENDIDAS";
                        saltarEncabezado = true;
                    }
                    continue;
                }

                if (linea.equals(FIN_SECCION)) {
                    continue;
                }

                // Saltar encabezados CSV
                if (saltarEncabezado) {
                    saltarEncabezado = false;
                    continue;
                }

                // Procesar según la sección
                switch (seccionActual) {
                    case "INFO":
                        String[] infoParts = linea.split(",");
                        if (infoParts.length >= 2) {
                            if (infoParts[0].equals("posicionCabeza")) {
                                posicionCabeza = Integer.parseInt(infoParts[1]);
                            }
                            // Las estadísticas se recalculan automáticamente
                        }
                        break;

                    case "PROCESOS":
                        Proceso proceso = cargarProceso(linea);
                        if (proceso != null) {
                            procesosTemp.insertarFinal(proceso);
                            gestorProcesos.agregarProcesoDirecto(proceso);
                        }
                        break;

                    case "SOLICITUDES_PENDIENTES":
                        cargarSolicitud(linea, procesosTemp, gestorProcesos, false);
                        break;

                    case "SOLICITUDES_ATENDIDAS":
                        cargarSolicitud(linea, procesosTemp, gestorProcesos, true);
                        break;
                }
            }

            gestorProcesos.setPosicionCabeza(posicionCabeza);
            return true;

        } catch (IOException e) {
            System.err.println("Error al cargar procesos: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error en formato de datos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Parsea una línea CSV manejando valores entre comillas
     */
    private static String[] parsearCSV(String linea) {
        Lista<String> valores = new Lista<>();
        StringBuilder valorActual = new StringBuilder();
        boolean dentroComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '"') {
                if (dentroComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    valorActual.append('"');
                    i++;
                } else {
                    dentroComillas = !dentroComillas;
                }
            } else if (c == ',' && !dentroComillas) {
                valores.insertarFinal(valorActual.toString());
                valorActual = new StringBuilder();
            } else {
                valorActual.append(c);
            }
        }
        valores.insertarFinal(valorActual.toString());

        String[] resultado = new String[valores.getSize()];
        for (int i = 0; i < valores.getSize(); i++) {
            resultado[i] = valores.get(i);
        }
        return resultado;
    }

    /**
     * Carga un proceso desde una línea CSV
     */
    private static Proceso cargarProceso(String linea) {
        String[] partes = parsearCSV(linea);
        // id,nombre,estado,operacion,archivoObjetivo,propietario,tamanoEnBloques,operacionEjecutada
        if (partes.length >= 8) {
            // partes[0] es el ID original (no se usa, el proceso genera uno nuevo)
            String nombre = partes[1];
            // partes[2] es el estado guardado (ignoramos, siempre será BLOQUEADO)
            Proceso.TipoOperacion operacion = Proceso.TipoOperacion.valueOf(partes[3]);
            String archivoObjetivo = partes[4];
            String propietario = partes[5];
            int tamanoEnBloques = Integer.parseInt(partes[6]);
            // partes[7] es operacionEjecutada guardado (ignoramos, siempre será false)

            Proceso proceso = new Proceso(nombre, operacion, archivoObjetivo, propietario);
            // Forzar estado BLOQUEADO y operacionEjecutada=false para re-ejecución
            proceso.setEstado(Proceso.Estado.BLOQUEADO);
            proceso.setTamanoEnBloques(tamanoEnBloques);
            proceso.setOperacionEjecutada(false);

            return proceso;
        }
        return null;
    }

    /**
     * Carga una solicitud desde una línea CSV
     */
    private static void cargarSolicitud(String linea, Lista<Proceso> procesos,
            GestorProcesos gestorProcesos, boolean esAtendida) {
        String[] partes = parsearCSV(linea);
        // id,procesoId,bloqueDestino,tipoOperacion,atendida
        if (partes.length >= 5) {
            int procesoId = Integer.parseInt(partes[1]);
            int bloqueDestino = Integer.parseInt(partes[2]);
            Proceso.TipoOperacion tipoOperacion = Proceso.TipoOperacion.valueOf(partes[3]);

            // Buscar el proceso correspondiente
            Proceso proceso = null;
            for (int i = 0; i < procesos.getSize(); i++) {
                if (procesos.get(i).getId() == procesoId) {
                    proceso = procesos.get(i);
                    break;
                }
            }

            if (proceso != null) {
                if (esAtendida) {
                    gestorProcesos.agregarSolicitudAtendidaDirecta(proceso, bloqueDestino, tipoOperacion);
                } else {
                    gestorProcesos.agregarSolicitudES(proceso, bloqueDestino, tipoOperacion);
                }
            }
        }
    }

    /**
     * Guarda los procesos en la ubicación por defecto
     */
    public static boolean guardarProcesos(GestorProcesos gestorProcesos) {
        return guardarProcesos(gestorProcesos, ARCHIVO_PROCESOS);
    }

    /**
     * Carga los procesos desde la ubicación por defecto
     */
    public static boolean cargarProcesos(GestorProcesos gestorProcesos) {
        return cargarProcesos(gestorProcesos, ARCHIVO_PROCESOS);
    }

    /**
     * Verifica si existe un archivo de procesos guardado
     */
    public static boolean existeArchivoGuardado() {
        return new File(ARCHIVO_PROCESOS).exists();
    }

    /**
     * Verifica si existe un archivo de procesos en una ruta específica
     */
    public static boolean existeArchivoGuardado(String ruta) {
        return new File(ruta).exists();
    }
}
