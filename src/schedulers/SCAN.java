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

            boolean enDireccionCorrecta;
            if (direccionAscendente) {
                enDireccionCorrecta = (bloque >= posicionCabeza);
            } else {
                enDireccionCorrecta = (bloque <= posicionCabeza);
            }

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
            mejorDistancia = Integer.MAX_VALUE; // Reiniciar para la nueva búsqueda

            // Buscar en la nueva dirección (ahora la más cercana en dirección opuesta)
            for (int i = 0; i < cola.getSize(); i++) {
                SolicitudIO solicitud = cola.get(i);
                int bloque = solicitud.getBloqueDestino();

                boolean enDireccionCorrecta;
                if (direccionAscendente) {
                    enDireccionCorrecta = (bloque >= posicionCabeza);
                } else {
                    enDireccionCorrecta = (bloque <= posicionCabeza);
                }

                if (enDireccionCorrecta) {
                    int distancia = Math.abs(bloque - posicionCabeza);
                    if (distancia < mejorDistancia) {
                        mejorDistancia = distancia;
                        seleccionada = solicitud;
                    }
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
        // Crear una copia ordenada
        Lista<SolicitudIO> copia = new Lista<>();
        for (int i = 0; i < lista.getSize(); i++) {
            copia.insertarFinal(lista.get(i));
        }

        // Bubble sort en la copia usando índices
        int n = copia.getSize();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                SolicitudIO actual = copia.get(j);
                SolicitudIO siguiente = copia.get(j + 1);

                boolean debeIntercambiar = ascendente
                        ? (actual.getBloqueDestino() > siguiente.getBloqueDestino())
                        : (actual.getBloqueDestino() < siguiente.getBloqueDestino());

                if (debeIntercambiar) {
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
