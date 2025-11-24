package models;

/**
 * Representa una solicitud de E/S en la cola del disco.
 * Asocia un proceso con el bloque que necesita acceder.
 */
public class SolicitudIO {

    private static int contadorId = 0;

    private int id;
    private Proceso proceso; // Proceso que genera la solicitud
    private int bloqueDestino; // Bloque al que se quiere acceder
    private Proceso.TipoOperacion tipoOperacion;
    private long tiempoLlegada;
    private boolean atendida;

    public SolicitudIO(Proceso proceso, int bloqueDestino, Proceso.TipoOperacion tipoOperacion) {
        this.id = ++contadorId;
        this.proceso = proceso;
        this.bloqueDestino = bloqueDestino;
        this.tipoOperacion = tipoOperacion;
        this.tiempoLlegada = System.currentTimeMillis();
        this.atendida = false;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public Proceso getProceso() {
        return proceso;
    }

    public void setProceso(Proceso proceso) {
        this.proceso = proceso;
    }

    public int getBloqueDestino() {
        return bloqueDestino;
    }

    public void setBloqueDestino(int bloqueDestino) {
        this.bloqueDestino = bloqueDestino;
    }

    public Proceso.TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(Proceso.TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public long getTiempoLlegada() {
        return tiempoLlegada;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }

    public static void resetContador() {
        contadorId = 0;
    }

    @Override
    public String toString() {
        return "Solicitud #" + id + " [Bloque: " + bloqueDestino +
                ", Proceso: P" + proceso.getId() +
                ", Op: " + tipoOperacion + "]";
    }
}
