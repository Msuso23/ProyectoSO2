package filesystem;

import EDD.Lista;
import models.Archivo;
import models.Directorio;

/**
 * Gestor principal del sistema de archivos.
 * Administra la estructura de directorios y coordina operaciones CRUD.
 */
public class GestorArchivos {

    private Directorio raiz;
    private SimuladorDisco disco;
    private Directorio directorioActual;
    private boolean modoAdministrador;
    private String usuarioActual;

    public GestorArchivos(SimuladorDisco disco) {
        this.disco = disco;
        this.raiz = new Directorio("root", null, "admin");
        this.directorioActual = raiz;
        this.modoAdministrador = true;
        this.usuarioActual = "admin";
    }

    /**
     * Crea un nuevo archivo en el directorio actual.
     * Solo permitido en modo administrador.
     */
    public boolean crearArchivo(String nombre, int tamanoEnBloques) {
        if (!modoAdministrador) {
            System.out.println("Error: Solo administradores pueden crear archivos");
            return false;
        }

        // Verificar que no exista un archivo con el mismo nombre
        if (directorioActual.buscarArchivo(nombre) != null) {
            System.out.println("Error: Ya existe un archivo con ese nombre");
            return false;
        }

        // Verificar espacio disponible
        if (!disco.hayEspacio(tamanoEnBloques)) {
            System.out.println("Error: No hay suficiente espacio en disco");
            return false;
        }

        // Crear el archivo
        Archivo nuevoArchivo = new Archivo(nombre, tamanoEnBloques, usuarioActual);

        // Asignar bloques en el disco
        Lista<Integer> bloquesAsignados = disco.asignarBloques(nuevoArchivo);
        if (bloquesAsignados == null) {
            System.out.println("Error: Falló la asignación de bloques");
            return false;
        }

        // Agregar archivo al directorio
        directorioActual.agregarArchivo(nuevoArchivo);

        System.out.println("Archivo '" + nombre + "' creado exitosamente con " +
                tamanoEnBloques + " bloques (primer bloque: " +
                nuevoArchivo.getPrimerBloque() + ")");
        return true;
    }

    /**
     * Crea un nuevo directorio en el directorio actual.
     * Solo permitido en modo administrador.
     */
    public boolean crearDirectorio(String nombre) {
        if (!modoAdministrador) {
            System.out.println("Error: Solo administradores pueden crear directorios");
            return false;
        }

        // Verificar que no exista un directorio con el mismo nombre
        if (directorioActual.buscarSubdirectorio(nombre) != null) {
            System.out.println("Error: Ya existe un directorio con ese nombre");
            return false;
        }

        Directorio nuevoDir = new Directorio(nombre, directorioActual, usuarioActual);
        directorioActual.agregarSubdirectorio(nuevoDir);

        System.out.println("Directorio '" + nombre + "' creado exitosamente");
        return true;
    }

    /**
     * Elimina un archivo del directorio actual.
     * Solo permitido en modo administrador.
     */
    public boolean eliminarArchivo(String nombre) {
        if (!modoAdministrador) {
            System.out.println("Error: Solo administradores pueden eliminar archivos");
            return false;
        }

        Archivo archivo = directorioActual.buscarArchivo(nombre);
        if (archivo == null) {
            System.out.println("Error: Archivo no encontrado");
            return false;
        }

        // Liberar bloques del disco
        int bloquesLiberados = disco.liberarBloques(archivo.getPrimerBloque());

        // Eliminar del directorio
        directorioActual.eliminarArchivo(nombre);

        System.out.println("Archivo '" + nombre + "' eliminado. " +
                bloquesLiberados + " bloques liberados");
        return true;
    }

    /**
     * Elimina un directorio y todo su contenido recursivamente.
     * Solo permitido en modo administrador.
     */
    public boolean eliminarDirectorio(String nombre) {
        if (!modoAdministrador) {
            System.out.println("Error: Solo administradores pueden eliminar directorios");
            return false;
        }

        Directorio dir = directorioActual.buscarSubdirectorio(nombre);
        if (dir == null) {
            System.out.println("Error: Directorio no encontrado");
            return false;
        }

        // Eliminar todo el contenido recursivamente
        eliminarContenidoRecursivo(dir);

        // Eliminar el directorio del padre
        directorioActual.eliminarSubdirectorio(nombre);

        System.out.println("Directorio '" + nombre + "' eliminado exitosamente");
        return true;
    }

    /**
     * Elimina recursivamente todo el contenido de un directorio
     */
    private void eliminarContenidoRecursivo(Directorio dir) {
        // Eliminar archivos
        Lista<Archivo> archivos = dir.getArchivos();
        while (!archivos.isEmpty()) {
            Archivo archivo = archivos.getFirst();
            disco.liberarBloques(archivo.getPrimerBloque());
            archivos.removeFirst();
        }

        // Eliminar subdirectorios recursivamente
        Lista<Directorio> subdirs = dir.getSubdirectorios();
        while (!subdirs.isEmpty()) {
            Directorio subdir = subdirs.getFirst();
            eliminarContenidoRecursivo(subdir);
            subdirs.removeFirst();
        }
    }

    /**
     * Renombra un archivo.
     * Solo permitido en modo administrador.
     */
    public boolean renombrarArchivo(String nombreActual, String nuevoNombre) {
        if (!modoAdministrador) {
            System.out.println("Error: Solo administradores pueden renombrar archivos");
            return false;
        }

        Archivo archivo = directorioActual.buscarArchivo(nombreActual);
        if (archivo == null) {
            System.out.println("Error: Archivo no encontrado");
            return false;
        }

        // Verificar que no exista otro archivo con el nuevo nombre
        if (directorioActual.buscarArchivo(nuevoNombre) != null) {
            System.out.println("Error: Ya existe un archivo con ese nombre");
            return false;
        }

        // Actualizar nombre en los bloques
        Lista<Integer> bloques = disco.obtenerCadenaBloquesArchivo(archivo.getPrimerBloque());
        for (int i = 0; i < bloques.getSize(); i++) {
            disco.getBloque(bloques.get(i)).setArchivoAsociado(nuevoNombre);
        }

        archivo.setNombre(nuevoNombre);
        System.out.println("Archivo renombrado a '" + nuevoNombre + "'");
        return true;
    }

    /**
     * Renombra un directorio.
     * Solo permitido en modo administrador.
     */
    public boolean renombrarDirectorio(String nombreActual, String nuevoNombre) {
        if (!modoAdministrador) {
            System.out.println("Error: Solo administradores pueden renombrar directorios");
            return false;
        }

        Directorio dir = directorioActual.buscarSubdirectorio(nombreActual);
        if (dir == null) {
            System.out.println("Error: Directorio no encontrado");
            return false;
        }

        // Verificar que no exista otro directorio con el nuevo nombre
        if (directorioActual.buscarSubdirectorio(nuevoNombre) != null) {
            System.out.println("Error: Ya existe un directorio con ese nombre");
            return false;
        }

        dir.setNombre(nuevoNombre);
        System.out.println("Directorio renombrado a '" + nuevoNombre + "'");
        return true;
    }

    /**
     * Navega a un subdirectorio
     */
    public boolean entrarDirectorio(String nombre) {
        Directorio dir = directorioActual.buscarSubdirectorio(nombre);
        if (dir == null) {
            System.out.println("Error: Directorio no encontrado");
            return false;
        }
        directorioActual = dir;
        return true;
    }

    /**
     * Navega al directorio padre
     */
    public boolean salirDirectorio() {
        if (directorioActual.getPadre() != null) {
            directorioActual = directorioActual.getPadre();
            return true;
        }
        System.out.println("Ya estás en el directorio raíz");
        return false;
    }

    /**
     * Navega a la raíz
     */
    public void irARaiz() {
        directorioActual = raiz;
    }

    /**
     * Obtiene todos los archivos del sistema recursivamente
     */
    public Lista<Archivo> obtenerTodosLosArchivos() {
        Lista<Archivo> todos = new Lista<>();
        obtenerArchivosRecursivo(raiz, todos);
        return todos;
    }

    private void obtenerArchivosRecursivo(Directorio dir, Lista<Archivo> lista) {
        // Agregar archivos del directorio actual
        Lista<Archivo> archivos = dir.getArchivos();
        for (int i = 0; i < archivos.getSize(); i++) {
            lista.insertarFinal(archivos.get(i));
        }

        // Recorrer subdirectorios
        Lista<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getSize(); i++) {
            obtenerArchivosRecursivo(subdirs.get(i), lista);
        }
    }

    /**
     * Busca un archivo en todo el sistema de archivos
     */
    public Archivo buscarArchivoEnSistema(String nombre) {
        return buscarArchivoRecursivo(raiz, nombre);
    }

    private Archivo buscarArchivoRecursivo(Directorio dir, String nombre) {
        // Buscar en archivos del directorio actual
        Archivo archivo = dir.buscarArchivo(nombre);
        if (archivo != null) {
            return archivo;
        }

        // Buscar en subdirectorios
        Lista<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getSize(); i++) {
            archivo = buscarArchivoRecursivo(subdirs.get(i), nombre);
            if (archivo != null) {
                return archivo;
            }
        }

        return null;
    }

    // Getters y Setters
    public Directorio getRaiz() {
        return raiz;
    }

    public Directorio getDirectorioActual() {
        return directorioActual;
    }

    public void setDirectorioActual(Directorio directorioActual) {
        this.directorioActual = directorioActual;
    }

    public boolean isModoAdministrador() {
        return modoAdministrador;
    }

    public void setModoAdministrador(boolean modoAdministrador) {
        this.modoAdministrador = modoAdministrador;
        this.usuarioActual = modoAdministrador ? "admin" : "usuario";
    }

    public String getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public SimuladorDisco getDisco() {
        return disco;
    }
}
