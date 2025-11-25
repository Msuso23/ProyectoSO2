package schedulers;

import EDD.Lista;
import models.SolicitudIO;

/**
 * Planificador SCAN (Algoritmo del Elevador).
 * La cabeza se mueve en una dirección atendiendo solicitudes,
 * y cuando llega al final, invierte la dirección.
 * 
 * Ventajas: Evita inanición, buen rendimiento
 * Desventajas: Las solicitudes en los extremos esperan más
 */
public class SCAN implements PlanificadorDisco {

    private boolean direccionAscendente; // true = hacia bloques mayores
    public static final int BLOQUE_MAXIMO = 99; // Límite del disco

    public SCAN() {
        this.direccionAscendente = true;
    }

    @Override
    public String getNombre() {
        return "SCAN";
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Lista<SolicitudIO> cola, int posicionCabeza) {
        if (cola.isEmpty()) {
            return null;
        }

        SolicitudIO seleccionada = null;
        int mejorDistancia = Integer.MAX_VALUE;

        // Buscar la más cercana en la dirección actual
        for (int i = 0; i < cola.getSize(); i++) {
            SolicitudIO solicitud = cola.get(i);
            int bloque = solicitud.getBloqueDestino();

            boolean enDireccionCorrecta = direccionAscendente ? (bloque >= posicionCabeza) : (bloque <= posicionCabeza);

            if (enDireccionCorrecta) {
                int distancia = Math.abs(bloque - posicionCabeza);
                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    seleccionada = solicitud;
                }
            }
        }

        // Si no hay solicitudes en la dirección actual, cambiar dirección
        if (seleccionada == null) {
            direccionAscendente = !direccionAscendente;

            // Buscar en la nueva dirección
            for (int i = 0; i < cola.getSize(); i++) {
                SolicitudIO solicitud = cola.get(i);
                int bloque = solicitud.getBloqueDestino();
                int distancia = Math.abs(bloque - posicionCabeza);

                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    seleccionada = solicitud;
                }
            }
        }

        return seleccionada;
    }

    @Override
    public Lista<SolicitudIO> ordenarCola(Lista<SolicitudIO> cola, int posicionCabeza) {
        Lista<SolicitudIO> colaOrdenada = new Lista<>();

        // Separar solicitudes en dos grupos: mayores y menores que la cabeza
        Lista<SolicitudIO> mayores = new Lista<>();
        Lista<SolicitudIO> menores = new Lista<>();

        for (int i = 0; i < cola.getSize(); i++) {
            SolicitudIO s = cola.get(i);
            if (s.getBloqueDestino() >= posicionCabeza) {
                mayores.insertarFinal(s);
            } else {
                menores.insertarFinal(s);
            }
        }

        // Ordenar mayores de menor a mayor (ascendente)
        ordenarPorBloque(mayores, true);
        // Ordenar menores de mayor a menor (descendente)
        ordenarPorBloque(menores, false);

        // Si dirección es ascendente: primero mayores, luego menores
        if (direccionAscendente) {
            agregarTodos(colaOrdenada, mayores);
            agregarTodos(colaOrdenada, menores);
        } else {
            agregarTodos(colaOrdenada, menores);
            agregarTodos(colaOrdenada, mayores);
        }

        return colaOrdenada;
    }

    /**
     * Ordena una lista de solicitudes por número de bloque usando bubble sort
     * 
     * @param lista      Lista a ordenar
     * @param ascendente true para orden ascendente, false para descendente
     */
    private void ordenarPorBloque(Lista<SolicitudIO> lista, boolean ascendente) {
        for (int i = 0; i < lista.getSize() - 1; i++) {
            for (int j = 0; j < lista.getSize() - 1 - i; j++) {
                SolicitudIO actual = lista.get(j);
                SolicitudIO siguiente = lista.get(j + 1);

                boolean debeIntercambiar = ascendente ? (actual.getBloqueDestino() > siguiente.getBloqueDestino())
                        : (actual.getBloqueDestino() < siguiente.getBloqueDestino());

                if (debeIntercambiar) {
                    // Intercambiar usando remove e insert
                    lista.remove(j);
                    lista.insertarPosicion(j, siguiente);
                    lista.remove(j + 1);
                    lista.insertarPosicion(j + 1, actual);
                }
            }
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

    // Getters y Setters
    public boolean isDireccionAscendente() {
        return direccionAscendente;
    }

    public void setDireccionAscendente(boolean direccionAscendente) {
        this.direccionAscendente = direccionAscendente;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
