package schedulers;

import EDD.Lista;
import models.SolicitudIO;

/**
 * Planificador SSTF (Shortest Seek Time First).
 * Selecciona la solicitud más cercana a la posición actual de la cabeza.
 * 
 * Ventajas: Minimiza el tiempo de búsqueda promedio
 * Desventajas: Puede causar inanición en solicitudes lejanas
 */
public class SSTF implements PlanificadorDisco {

    @Override
    public String getNombre() {
        return "SSTF";
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Lista<SolicitudIO> cola, int posicionCabeza) {
        if (cola.isEmpty()) {
            return null;
        }

        SolicitudIO masCercana = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (int i = 0; i < cola.getSize(); i++) {
            SolicitudIO solicitud = cola.get(i);
            int distancia = Math.abs(solicitud.getBloqueDestino() - posicionCabeza);

            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercana = solicitud;
            }
        }

        return masCercana;
    }

    @Override
    public Lista<SolicitudIO> ordenarCola(Lista<SolicitudIO> cola, int posicionCabeza) {
        Lista<SolicitudIO> colaOrdenada = new Lista<>();
        Lista<SolicitudIO> copiaCola = copiarLista(cola);

        int cabezaSimulada = posicionCabeza;

        while (!copiaCola.isEmpty()) {
            SolicitudIO siguiente = seleccionarSiguiente(copiaCola, cabezaSimulada);
            if (siguiente != null) {
                colaOrdenada.insertarFinal(siguiente);
                cabezaSimulada = siguiente.getBloqueDestino();
                copiaCola.remove(siguiente);
            }
        }

        return colaOrdenada;
    }

    /**
     * Crea una copia de la lista de solicitudes
     */
    private Lista<SolicitudIO> copiarLista(Lista<SolicitudIO> original) {
        Lista<SolicitudIO> copia = new Lista<>();
        for (int i = 0; i < original.getSize(); i++) {
            copia.insertarFinal(original.get(i));
        }
        return copia;
    }

    /**
     * Calcula el movimiento total de la cabeza para esta cola
     */
    public int calcularMovimientoTotal(Lista<SolicitudIO> cola, int posicionInicial) {
        Lista<SolicitudIO> ordenada = ordenarCola(cola, posicionInicial);
        int movimiento = 0;
        int posActual = posicionInicial;

        for (int i = 0; i < ordenada.getSize(); i++) {
            int destino = ordenada.get(i).getBloqueDestino();
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
