package process;

import EDD.Lista;
import EDD.Queue;
import models.Proceso;
import models.SolicitudIO;
import models.Archivo;
import schedulers.PlanificadorDisco;
import schedulers.FIFO;
import filesystem.GestorArchivos;
import filesystem.SimuladorDisco;

/**
 * Gestiona la cola de procesos y las solicitudes de E/S del sistema.
 * Coordina la ejecución de procesos según el planificador de disco activo.
 */
public class GestorProcesos {

    private Lista<Proceso> procesos; // Todos los procesos
    private Lista<SolicitudIO> solicitudesPendientes; // Solicitudes en espera
    private Lista<SolicitudIO> solicitudesAtendidas; // Historial de atendidas
    private PlanificadorDisco planificador;
    private Proceso procesoActual; // Proceso en ejecución
    private int posicionCabeza; // Posición de la cabeza del disco

    // Referencias al sistema de archivos para ejecutar operaciones reales
    private GestorArchivos gestorArchivos;
    private SimuladorDisco disco;

    // Estadísticas
    private int totalSolicitudesAtendidas;
    private int movimientosTotales;

    public GestorProcesos() {
        this.procesos = new Lista<>();
        this.solicitudesPendientes = new Lista<>();
        this.solicitudesAtendidas = new Lista<>();
        this.planificador = new FIFO(); // Planificador por defecto
        this.procesoActual = null;
        this.posicionCabeza = 0;
        this.totalSolicitudesAtendidas = 0;
        this.movimientosTotales = 0;
        this.gestorArchivos = null;
        this.disco = null;
    }

    /**
     * Configura las referencias al sistema de archivos
     */
    public void configurarSistemaArchivos(GestorArchivos gestorArchivos, SimuladorDisco disco) {
        this.gestorArchivos = gestorArchivos;
        this.disco = disco;
    }

    /**
     * Crea un nuevo proceso y lo agrega al sistema
     */
    public Proceso crearProceso(String nombre, Proceso.TipoOperacion operacion,
            String archivoObjetivo, String propietario) {
        Proceso proceso = new Proceso(nombre, operacion, archivoObjetivo, propietario);
        proceso.cambiarEstado(Proceso.Estado.LISTO);
        procesos.insertarFinal(proceso);
        return proceso;
    }

    /**
     * Agrega una solicitud de E/S a la cola de pendientes
     */
    public void agregarSolicitudES(Proceso proceso, int bloqueDestino,
            Proceso.TipoOperacion tipoOperacion) {
        SolicitudIO solicitud = new SolicitudIO(proceso, bloqueDestino, tipoOperacion);
        solicitudesPendientes.insertarFinal(solicitud);
        proceso.cambiarEstado(Proceso.Estado.BLOQUEADO);
    }

    /**
     * Agrega múltiples solicitudes para todos los bloques de un archivo
     */
    public void agregarSolicitudesParaArchivo(Proceso proceso, Lista<Integer> bloques,
            Proceso.TipoOperacion tipoOperacion) {
        for (int i = 0; i < bloques.getSize(); i++) {
            agregarSolicitudES(proceso, bloques.get(i), tipoOperacion);
        }
    }

    /**
     * Procesa la siguiente solicitud según el planificador activo
     * 
     * @return La solicitud atendida, o null si no hay solicitudes
     */
    public SolicitudIO procesarSiguienteSolicitud() {
        if (solicitudesPendientes.isEmpty()) {
            return null;
        }

        // Usar el planificador para seleccionar la siguiente solicitud
        SolicitudIO solicitud = planificador.seleccionarSiguiente(
                solicitudesPendientes, posicionCabeza);

        if (solicitud != null) {
            // Calcular y acumular el movimiento de la cabeza
            int distancia = Math.abs(solicitud.getBloqueDestino() - posicionCabeza);
            movimientosTotales += distancia;

            // Mover la cabeza
            posicionCabeza = solicitud.getBloqueDestino();

            // Marcar como atendida y mover al historial
            solicitud.setAtendida(true);
            solicitudesPendientes.remove(solicitud);
            solicitudesAtendidas.insertarFinal(solicitud);

            // Actualizar estado del proceso
            Proceso proceso = solicitud.getProceso();
            proceso.cambiarEstado(Proceso.Estado.EJECUTANDO);
            proceso.setBloqueActual(solicitud.getBloqueDestino());
            procesoActual = proceso;

            totalSolicitudesAtendidas++;

            // Verificar si el proceso terminó todas sus solicitudes
            if (!tieneSolicitudesPendientes(proceso)) {
                // Ejecutar la operación real cuando el proceso termina
                ejecutarOperacionReal(proceso);
                proceso.cambiarEstado(Proceso.Estado.TERMINADO);
                procesoActual = null;
            }
        }

        return solicitud;
    }

    /**
     * Ejecuta la operación real del proceso en el sistema de archivos
     */
    private void ejecutarOperacionReal(Proceso proceso) {
        if (gestorArchivos == null || disco == null) {
            System.out.println("Sistema de archivos no configurado");
            return;
        }

        if (proceso.isOperacionEjecutada()) {
            return; // Ya se ejecutó
        }

        String nombreArchivo = proceso.getArchivoObjetivo();
        boolean modoAnterior = gestorArchivos.isModoAdministrador();
        gestorArchivos.setModoAdministrador(true); // Permitir operaciones

        switch (proceso.getOperacion()) {
            case CREAR:
                int tamano = proceso.getTamanoEnBloques();
                if (tamano > 0) {
                    gestorArchivos.crearArchivo(nombreArchivo, tamano);
                }
                break;

            case ELIMINAR:
                gestorArchivos.eliminarArchivo(nombreArchivo);
                break;

            case ACTUALIZAR:
                // Actualizar puede significar renombrar
                String nuevoNombre = nombreArchivo + "_mod";
                gestorArchivos.renombrarArchivo(nombreArchivo, nuevoNombre);
                break;

            case LEER:
                // Leer no modifica nada, solo simula acceso
                break;
        }

        gestorArchivos.setModoAdministrador(modoAnterior); // Restaurar modo
        proceso.setOperacionEjecutada(true);
    }

    /**
     * Verifica si un proceso tiene solicitudes pendientes
     */
    public boolean tieneSolicitudesPendientes(Proceso proceso) {
        for (int i = 0; i < solicitudesPendientes.getSize(); i++) {
            if (solicitudesPendientes.get(i).getProceso().getId() == proceso.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene todas las solicitudes ordenadas según el planificador actual
     */
    public Lista<SolicitudIO> obtenerSolicitudesOrdenadas() {
        return planificador.ordenarCola(solicitudesPendientes, posicionCabeza);
    }

    /**
     * Cambia el algoritmo de planificación
     */
    public void cambiarPlanificador(PlanificadorDisco nuevoPlanificador) {
        this.planificador = nuevoPlanificador;
    }

    /**
     * Obtiene procesos filtrados por estado
     */
    public Lista<Proceso> obtenerProcesosPorEstado(Proceso.Estado estado) {
        Lista<Proceso> resultado = new Lista<>();
        for (int i = 0; i < procesos.getSize(); i++) {
            Proceso p = procesos.get(i);
            if (p.getEstado() == estado) {
                resultado.insertarFinal(p);
            }
        }
        return resultado;
    }

    /**
     * Verifica si hay un proceso pendiente de ELIMINAR para un archivo específico
     */
    public boolean hayEliminacionPendiente(String nombreArchivo) {
        for (int i = 0; i < procesos.getSize(); i++) {
            Proceso p = procesos.get(i);
            if (p.getEstado() != Proceso.Estado.TERMINADO &&
                    p.getOperacion() == Proceso.TipoOperacion.ELIMINAR &&
                    p.getArchivoObjetivo().equals(nombreArchivo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si hay un proceso pendiente de CREAR un archivo con ese nombre
     */
    public boolean hayCreacionPendiente(String nombreArchivo) {
        for (int i = 0; i < procesos.getSize(); i++) {
            Proceso p = procesos.get(i);
            if (p.getEstado() != Proceso.Estado.TERMINADO &&
                    p.getOperacion() == Proceso.TipoOperacion.CREAR &&
                    p.getArchivoObjetivo().equals(nombreArchivo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si se puede crear un proceso para un archivo
     * Retorna null si se puede, o un mensaje de error si no
     */
    public String validarOperacionArchivo(String nombreArchivo, Proceso.TipoOperacion operacion) {
        // Si se quiere CREAR, verificar que no haya otra creación pendiente con el
        // mismo nombre
        if (operacion == Proceso.TipoOperacion.CREAR) {
            if (hayCreacionPendiente(nombreArchivo)) {
                return "Ya existe un proceso pendiente para crear un archivo con ese nombre";
            }
            return null; // OK
        }

        // Para LEER, ACTUALIZAR o ELIMINAR, verificar que no haya eliminación pendiente
        if (hayEliminacionPendiente(nombreArchivo)) {
            return "El archivo '" + nombreArchivo + "' tiene una eliminación pendiente.\n" +
                    "Espere a que se procese antes de crear nuevas operaciones sobre él.";
        }

        return null; // OK
    }

    /**
     * Obtiene la cantidad de procesos por estado
     */
    public int contarProcesosPorEstado(Proceso.Estado estado) {
        int contador = 0;
        for (int i = 0; i < procesos.getSize(); i++) {
            if (procesos.get(i).getEstado() == estado) {
                contador++;
            }
        }
        return contador;
    }

    /**
     * Cancela un proceso y elimina sus solicitudes pendientes
     */
    public boolean cancelarProceso(int idProceso) {
        Proceso proceso = buscarProceso(idProceso);
        if (proceso == null || proceso.getEstado() == Proceso.Estado.TERMINADO) {
            return false;
        }

        // Eliminar solicitudes pendientes del proceso
        for (int i = solicitudesPendientes.getSize() - 1; i >= 0; i--) {
            if (solicitudesPendientes.get(i).getProceso().getId() == idProceso) {
                solicitudesPendientes.remove(i);
            }
        }

        proceso.cambiarEstado(Proceso.Estado.TERMINADO);
        if (procesoActual != null && procesoActual.getId() == idProceso) {
            procesoActual = null;
        }

        return true;
    }

    /**
     * Busca un proceso por su ID
     */
    public Proceso buscarProceso(int id) {
        for (int i = 0; i < procesos.getSize(); i++) {
            if (procesos.get(i).getId() == id) {
                return procesos.get(i);
            }
        }
        return null;
    }

    /**
     * Limpia procesos terminados del sistema
     */
    public int limpiarProcesosTerminados() {
        int eliminados = 0;
        for (int i = procesos.getSize() - 1; i >= 0; i--) {
            if (procesos.get(i).getEstado() == Proceso.Estado.TERMINADO) {
                procesos.remove(i);
                eliminados++;
            }
        }
        return eliminados;
    }

    /**
     * Reinicia las estadísticas
     */
    public void reiniciarEstadisticas() {
        totalSolicitudesAtendidas = 0;
        movimientosTotales = 0;
        solicitudesAtendidas.vaciar();
    }

    /**
     * Limpia todo el gestor
     */
    public void limpiarTodo() {
        procesos.vaciar();
        solicitudesPendientes.vaciar();
        solicitudesAtendidas.vaciar();
        procesoActual = null;
        posicionCabeza = 0;
        Proceso.resetContador();
        SolicitudIO.resetContador();
        reiniciarEstadisticas();
    }

    // Getters
    public Lista<Proceso> getProcesos() {
        return procesos;
    }

    public Lista<SolicitudIO> getSolicitudesPendientes() {
        return solicitudesPendientes;
    }

    public Lista<SolicitudIO> getSolicitudesAtendidas() {
        return solicitudesAtendidas;
    }

    public PlanificadorDisco getPlanificador() {
        return planificador;
    }

    public Proceso getProcesoActual() {
        return procesoActual;
    }

    public int getPosicionCabeza() {
        return posicionCabeza;
    }

    public void setPosicionCabeza(int posicionCabeza) {
        this.posicionCabeza = posicionCabeza;
    }

    public int getTotalSolicitudesAtendidas() {
        return totalSolicitudesAtendidas;
    }

    public int getMovimientosTotales() {
        return movimientosTotales;
    }

    public double getPromedioMovimientos() {
        if (totalSolicitudesAtendidas == 0)
            return 0;
        return (double) movimientosTotales / totalSolicitudesAtendidas;
    }

    public int getCantidadSolicitudesPendientes() {
        return solicitudesPendientes.getSize();
    }

    public int getCantidadProcesosActivos() {
        int activos = 0;
        for (int i = 0; i < procesos.getSize(); i++) {
            Proceso.Estado estado = procesos.get(i).getEstado();
            if (estado != Proceso.Estado.TERMINADO) {
                activos++;
            }
        }
        return activos;
    }
}
