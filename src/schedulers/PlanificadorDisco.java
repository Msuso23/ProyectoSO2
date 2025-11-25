package schedulers;

import EDD.Lista;
import models.SolicitudIO;

/**
 * Interfaz para los algoritmos de planificación de disco.
 */
public interface PlanificadorDisco {

    /**
     * Obtiene el nombre del algoritmo
     */
    String getNombre();

    /**
     * Selecciona la siguiente solicitud a atender de la cola.
     * 
     * @param cola           Cola de solicitudes pendientes
     * @param posicionCabeza Posición actual de la cabeza del disco
     * @return La solicitud seleccionada, o null si la cola está vacía
     */
    SolicitudIO seleccionarSiguiente(Lista<SolicitudIO> cola, int posicionCabeza);

    /**
     * Ordena todas las solicitudes según el algoritmo.
     * Útil para visualizar el orden en que serán atendidas.
     * 
     * @param cola           Cola de solicitudes
     * @param posicionCabeza Posición actual de la cabeza
     * @return Nueva lista ordenada según el algoritmo
     */
    Lista<SolicitudIO> ordenarCola(Lista<SolicitudIO> cola, int posicionCabeza);
}
