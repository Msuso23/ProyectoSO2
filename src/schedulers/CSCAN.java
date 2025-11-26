package schedulers;

import EDD.Lista;
import models.SolicitudIO;

/**
 * Planificador C-SCAN (Circular SCAN).
 * La cabeza se mueve en una dirección atendiendo solicitudes.
 * Cuando llega al final, salta al inicio y continúa en la misma dirección.
 * 
 * Ventajas: Tiempo de espera más uniforme que SCAN
 * Desventajas: Mayor movimiento de cabeza que SCAN en algunos casos
 */
public class CSCAN implements PlanificadorDisco {

    public static final int BLOQUE_MINIMO = 0;
    public static final int BLOQUE_MAXIMO = 99; // Límite del disco

    @Override
    public String getNombre() {
        return "C-SCAN";
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Lista<SolicitudIO> cola, int posicionCabeza) {
        if (cola.isEmpty()) {
            return null;
        }

        SolicitudIO seleccionada = null;
        int menorDistanciaAdelante = Integer.MAX_VALUE;
        int menorBloqueAtras = Integer.MAX_VALUE;
        SolicitudIO seleccionadaAtras = null;

        // Primero buscar solicitudes >= posicionCabeza (hacia adelante)
        for (int i = 0; i < cola.getSize(); i++) {
            SolicitudIO solicitud = cola.get(i);
            int bloque = solicitud.getBloqueDestino();

            if (bloque >= posicionCabeza) {
                int distancia = bloque - posicionCabeza;
                if (distancia < menorDistanciaAdelante) {
                    menorDistanciaAdelante = distancia;
                    seleccionada = solicitud;
                }
            } else {
                // Guardar la menor para cuando haya que volver al inicio
                if (bloque < menorBloqueAtras) {
                    menorBloqueAtras = bloque;
                    seleccionadaAtras = solicitud;
                }
            }
        }

        // Si no hay solicitudes adelante, ir al inicio (menor bloque)
        if (seleccionada == null) {
            seleccionada = seleccionadaAtras;
        }

        return seleccionada;
    }

    @Override
    public Lista<SolicitudIO> ordenarCola(Lista<SolicitudIO> cola, int posicionCabeza) {
        Lista<SolicitudIO> colaOrdenada = new Lista<>();

        // Separar solicitudes
        Lista<SolicitudIO> mayoresOIguales = new Lista<>();
        Lista<SolicitudIO> menores = new Lista<>();

        for (int i = 0; i < cola.getSize(); i++) {
            SolicitudIO s = cola.get(i);
            if (s.getBloqueDestino() >= posicionCabeza) {
                mayoresOIguales.insertarFinal(s);
            } else {
                menores.insertarFinal(s);
            }
        }

        // Ordenar ambas listas ascendentemente
        ordenarAscendente(mayoresOIguales);
        ordenarAscendente(menores);

        // C-SCAN: primero las mayores (hacia el final), luego las menores (desde
        // inicio)
        agregarTodos(colaOrdenada, mayoresOIguales);
        agregarTodos(colaOrdenada, menores);

        return colaOrdenada;
    }

    /**
     * Ordena una lista de solicitudes de menor a mayor por bloque (bubble sort)
     */
    private void ordenarAscendente(Lista<SolicitudIO> lista) {
        // Crear una copia ordenada
        Lista<SolicitudIO> copia = new Lista<>();
        for (int i = 0; i < lista.getSize(); i++) {
            copia.insertarFinal(lista.get(i));
        }

        // Bubble sort en la copia
        int n = copia.getSize();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                SolicitudIO actual = copia.get(j);
                SolicitudIO siguiente = copia.get(j + 1);

                if (actual.getBloqueDestino() > siguiente.getBloqueDestino()) {
                    // Intercambiar: remover ambos y reinsertar en orden correcto
                    copia.remove(j + 1);
                    copia.remove(j);
                    copia.insertarPosicion(j, siguiente);
                    copia.insertarPosicion(j + 1, actual);
                }
            }
        }

        // Vaciar lista original y copiar los ordenados
        lista.vaciar();
        for (int i = 0; i < copia.getSize(); i++) {
            lista.insertarFinal(copia.get(i));
        }
    }

    /**
     * Agrega todos los elementos de origen a destino
     */
    private void agregarTodos(Lista<SolicitudIO> destino, Lista<SolicitudIO> origen) {
        for (int i = 0; i < origen.getSize(); i++) {
            destino.insertarFinal(origen.get(i));
        }
    }

    /**
     * Calcula el movimiento total de la cabeza
     * En C-SCAN, cuando llega al final salta al inicio
     */
    public int calcularMovimientoTotal(Lista<SolicitudIO> cola, int posicionInicial) {
        if (cola.isEmpty())
            return 0;

        Lista<SolicitudIO> ordenada = ordenarCola(cola, posicionInicial);
        int movimiento = 0;
        int posActual = posicionInicial;
        boolean saltoAlInicio = false;

        for (int i = 0; i < ordenada.getSize(); i++) {
            int destino = ordenada.get(i).getBloqueDestino();

            // Detectar si hubo salto circular (de final a inicio)
            if (!saltoAlInicio && destino < posActual) {
                // Ir al final + saltar al inicio + ir al destino
                movimiento += (BLOQUE_MAXIMO - posActual); // Ir al final
                movimiento += BLOQUE_MAXIMO; // Salto circular
                movimiento += destino; // Desde inicio al destino
                saltoAlInicio = true;
            } else {
                movimiento += Math.abs(destino - posActual);
            }
            posActual = destino;
        }

        return movimiento;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
