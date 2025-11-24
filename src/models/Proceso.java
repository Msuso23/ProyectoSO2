package models;

/**
 * Representa un proceso de usuario que realiza operaciones de E/S.
 */
public class Proceso {

    // Estados posibles del proceso
    public enum Estado {
        NUEVO,
        LISTO,
        EJECUTANDO,
        BLOQUEADO,
        TERMINADO
    }

    // Tipos de operaciones CRUD
    public enum TipoOperacion {
        CREAR,
        LEER,
        ACTUALIZAR,
        ELIMINAR
    }

    private static int contadorId = 0;

    private int id;
    private String nombre;
    private Estado estado;
    private TipoOperacion operacion;
    private String archivoObjetivo; // Archivo sobre el que opera
    private int bloqueActual; // Bloque que está procesando
    private String propietario; // Usuario dueño del proceso
    private long tiempoCreacion;
    private long tiempoInicio; // Cuando empezó a ejecutar
    private long tiempoFin; // Cuando terminó

    public Proceso(String nombre, TipoOperacion operacion, String archivoObjetivo, String propietario) {
        this.id = ++contadorId;
        this.nombre = nombre;
        this.estado = Estado.NUEVO;
        this.operacion = operacion;
        this.archivoObjetivo = archivoObjetivo;
        this.bloqueActual = -1;
        this.propietario = propietario;
        this.tiempoCreacion = System.currentTimeMillis();
    }

    /**
     * Cambia el estado del proceso
     */
    public void cambiarEstado(Estado nuevoEstado) {
        this.estado = nuevoEstado;

        if (nuevoEstado == Estado.EJECUTANDO && tiempoInicio == 0) {
            tiempoInicio = System.currentTimeMillis();
        } else if (nuevoEstado == Estado.TERMINADO) {
            tiempoFin = System.currentTimeMillis();
        }
    }

    /**
     * Obtiene el tiempo de espera del proceso
     */
    public long getTiempoEspera() {
        if (tiempoInicio == 0) {
            return System.currentTimeMillis() - tiempoCreacion;
        }
        return tiempoInicio - tiempoCreacion;
    }

    /**
     * Obtiene el tiempo de ejecución
     */
    public long getTiempoEjecucion() {
        if (tiempoInicio == 0) {
            return 0;
        }
        if (tiempoFin == 0) {
            return System.currentTimeMillis() - tiempoInicio;
        }
        return tiempoFin - tiempoInicio;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public TipoOperacion getOperacion() {
        return operacion;
    }

    public void setOperacion(TipoOperacion operacion) {
        this.operacion = operacion;
    }

    public String getArchivoObjetivo() {
        return archivoObjetivo;
    }

    public void setArchivoObjetivo(String archivoObjetivo) {
        this.archivoObjetivo = archivoObjetivo;
    }

    public int getBloqueActual() {
        return bloqueActual;
    }

    public void setBloqueActual(int bloqueActual) {
        this.bloqueActual = bloqueActual;
    }

    public String getPropietario() {
        return propietario;
    }

    public long getTiempoCreacion() {
        return tiempoCreacion;
    }

    public static void resetContador() {
        contadorId = 0;
    }

    @Override
    public String toString() {
        return "P" + id + " [" + operacion + " - " + archivoObjetivo + "] (" + estado + ")";
    }
}
