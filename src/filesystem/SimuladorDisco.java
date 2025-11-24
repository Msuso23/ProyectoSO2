package filesystem;

import EDD.Lista;
import models.Archivo;
import models.Bloque;

/**
 * Simula el disco con asignación encadenada de bloques.
 * Gestiona la asignación y liberación de bloques para archivos.
 */
public class SimuladorDisco {

    public static final int TOTAL_BLOQUES = 100; // Cantidad máxima de bloques

    private Bloque[] bloques;
    private int bloquesLibres;
    private int cabezaActual; // Posición actual de la cabeza del disco (para planificadores)

    public SimuladorDisco() {
        this.bloques = new Bloque[TOTAL_BLOQUES];
        this.bloquesLibres = TOTAL_BLOQUES;
        this.cabezaActual = 0;

        // Inicializar todos los bloques como libres
        for (int i = 0; i < TOTAL_BLOQUES; i++) {
            bloques[i] = new Bloque(i);
        }
    }

    /**
     * Asigna bloques para un archivo usando asignación encadenada.
     * 
     * @param archivo El archivo al que asignar bloques
     * @return Lista de IDs de bloques asignados, null si no hay espacio suficiente
     */
    public Lista<Integer> asignarBloques(Archivo archivo) {
        int cantidadBloques = archivo.getTamanoEnBloques();

        // Verificar si hay suficiente espacio
        if (cantidadBloques > bloquesLibres) {
            return null;
        }

        Lista<Integer> bloquesAsignados = new Lista<>();
        int bloquesEncontrados = 0;
        int bloqueAnterior = -1;
        int primerBloque = -1;

        // Buscar bloques libres y encadenarlos
        for (int i = 0; i < TOTAL_BLOQUES && bloquesEncontrados < cantidadBloques; i++) {
            if (!bloques[i].isOcupado()) {
                // Asignar este bloque
                bloques[i].asignar(archivo.getNombre(), archivo.getColor());
                bloquesAsignados.insertarFinal(i);

                // Guardar el primer bloque
                if (primerBloque == -1) {
                    primerBloque = i;
                }

                // Encadenar con el bloque anterior
                if (bloqueAnterior != -1) {
                    bloques[bloqueAnterior].setSiguienteBloque(i);
                }

                bloqueAnterior = i;
                bloquesEncontrados++;
                bloquesLibres--;
            }
        }

        // Actualizar el archivo con el primer bloque
        archivo.setPrimerBloque(primerBloque);

        return bloquesAsignados;
    }

    /**
     * Libera todos los bloques asociados a un archivo.
     * 
     * @param primerBloque El primer bloque del archivo
     * @return La cantidad de bloques liberados
     */
    public int liberarBloques(int primerBloque) {
        if (primerBloque < 0 || primerBloque >= TOTAL_BLOQUES) {
            return 0;
        }

        int bloquesLiberados = 0;
        int bloqueActual = primerBloque;

        // Recorrer la cadena de bloques y liberarlos
        while (bloqueActual != -1) {
            int siguienteBloque = bloques[bloqueActual].getSiguienteBloque();
            bloques[bloqueActual].liberar();
            bloquesLiberados++;
            bloquesLibres++;
            bloqueActual = siguienteBloque;
        }

        return bloquesLiberados;
    }

    /**
     * Obtiene la lista de bloques que ocupa un archivo (siguiendo la cadena).
     * 
     * @param primerBloque El primer bloque del archivo
     * @return Lista con los IDs de bloques que forman el archivo
     */
    public Lista<Integer> obtenerCadenaBloquesArchivo(int primerBloque) {
        Lista<Integer> cadena = new Lista<>();
        int bloqueActual = primerBloque;

        while (bloqueActual != -1 && bloqueActual < TOTAL_BLOQUES) {
            cadena.insertarFinal(bloqueActual);
            bloqueActual = bloques[bloqueActual].getSiguienteBloque();
        }

        return cadena;
    }

    /**
     * Verifica si hay espacio suficiente para un archivo
     */
    public boolean hayEspacio(int cantidadBloques) {
        return cantidadBloques <= bloquesLibres;
    }

    /**
     * Mueve la cabeza del disco a un bloque específico
     * 
     * @return La distancia recorrida
     */
    public int moverCabeza(int bloqueDestino) {
        int distancia = Math.abs(bloqueDestino - cabezaActual);
        cabezaActual = bloqueDestino;
        return distancia;
    }

    // Getters
    public Bloque[] getBloques() {
        return bloques;
    }

    public Bloque getBloque(int index) {
        if (index >= 0 && index < TOTAL_BLOQUES) {
            return bloques[index];
        }
        return null;
    }

    public int getBloquesLibres() {
        return bloquesLibres;
    }

    public int getBloquesOcupados() {
        return TOTAL_BLOQUES - bloquesLibres;
    }

    public int getCabezaActual() {
        return cabezaActual;
    }

    public void setCabezaActual(int cabezaActual) {
        this.cabezaActual = cabezaActual;
    }

    /**
     * Obtiene el porcentaje de uso del disco
     */
    public double getPorcentajeUso() {
        return ((double) getBloquesOcupados() / TOTAL_BLOQUES) * 100;
    }

    /**
     * Reinicia el disco liberando todos los bloques
     */
    public void reiniciar() {
        for (int i = 0; i < TOTAL_BLOQUES; i++) {
            bloques[i].liberar();
        }
        bloquesLibres = TOTAL_BLOQUES;
        cabezaActual = 0;
    }

    @Override
    public String toString() {
        return "SimuladorDisco{" +
                "totalBloques=" + TOTAL_BLOQUES +
                ", bloquesLibres=" + bloquesLibres +
                ", bloquesOcupados=" + getBloquesOcupados() +
                ", cabezaActual=" + cabezaActual +
                '}';
    }
}
