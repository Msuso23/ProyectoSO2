package schedulers;

import EDD.Lista;
import models.SolicitudIO;

/**
 * Planificador FIFO (First In, First Out).
 * Atiende las solicitudes en el orden en que llegaron.
 * 
 * Ventajas: Simple, justo (no hay inanición)
 * Desventajas: No optimiza el movimiento de la cabeza
 */
public class FIFO implements PlanificadorDisco {

    @Override
    public String getNombre() {
        return "FIFO";
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Lista<SolicitudIO> cola, int posicionCabeza) {
        if (cola.isEmpty()) {
            return null;
        }
        // FIFO simplemente toma el primer elemento de la cola
        return cola.getFirst();
    }

    @Override
    public Lista<SolicitudIO> ordenarCola(Lista<SolicitudIO> cola, int posicionCabeza) {
        // En FIFO, el orden ya es el correcto (orden de llegada)
        Lista<SolicitudIO> colaOrdenada = new Lista<>();

        for (int i = 0; i < cola.getSize(); i++) {
            colaOrdenada.insertarFinal(cola.get(i));
        }

        return colaOrdenada;
    }

    /**
     * Calcula el movimiento total de la cabeza para esta cola
     */
    public int calcularMovimientoTotal(Lista<SolicitudIO> cola, int posicionInicial) {
        int movimiento = 0;
        int posActual = posicionInicial;

        for (int i = 0; i < cola.getSize(); i++) {
            int destino = cola.get(i).getBloqueDestino();
            movimiento += Math.abs(destino - posActual);
            posActual = destino;
        }

        return movimiento;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
