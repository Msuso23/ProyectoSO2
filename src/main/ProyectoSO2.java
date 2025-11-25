package main;

import gui.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Clase principal del Simulador de Sistema de Archivos.
 * Proyecto 2 - Sistemas Operativos II
 */
public class ProyectoSO2 {

    /**
     * Punto de entrada de la aplicación.
     * 
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo configurar el Look and Feel: " + e.getMessage());
        }

        // Iniciar la interfaz gráfica en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
