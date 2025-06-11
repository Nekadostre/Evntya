package controladores;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelos.SesionTemporal;
import utils.ResponsiveManager;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static ResponsiveManager responsiveManager;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        try {
            Parent root = loadFXML("LoginView");
            scene = new Scene(root, 800, 600);
            
            // ✨ INICIALIZAR SISTEMA RESPONSIVO
            responsiveManager = new ResponsiveManager(scene, stage);
            System.out.println("📱 RESPONSIVE: Sistema responsivo inicializado correctamente");
            
            stage.setTitle("SEGUNDO CASTILLO - Sistema de Gestión");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
 
        } catch (IOException e) {
            System.out.println("❌ APP ERROR: Error al inicializar aplicación: " + e.getMessage());
            throw e;
        }
    }

    // Método para acceder al responsive manager desde otras clases
    public static ResponsiveManager getResponsiveManager() {
        return responsiveManager;
    }

    /**
     * Método principal para cambiar vistas con validación de sesión y sistema responsivo
     */
    public static void setRoot(String fxml) throws IOException {
        System.out.println("🚀 APP DEBUG: Cambiando vista a: " + fxml);
        
        // Verificar que no sea null
        if (fxml == null || fxml.trim().isEmpty()) {
            System.out.println("❌ APP ERROR: El nombre del FXML no puede ser null o vacío");
            throw new IOException("El nombre del FXML no puede ser null o vacío");
        }
        
        try {
            // Log del estado actual de sesión
            SesionTemporal sesion = SesionTemporal.getInstancia();
            System.out.println("📊 APP DEBUG: Estado de sesión:");
            System.out.println("   - Usuario logueado: " + sesion.hayUsuarioLogueado());
            if (sesion.hayUsuarioLogueado()) {
                System.out.println("   - Usuario: " + sesion.getNombreCompletoUsuario());
                System.out.println("   - Rol: " + sesion.getUsuarioRol());
            }
            
            // Validaciones específicas por vista
            System.out.println("🔍 APP DEBUG: Validando navegación para: " + fxml);
            if (!validarNavegacion(fxml, sesion)) {
                System.out.println("❌ APP DEBUG: Validación de navegación falló");
                return; // No continuar si la validación falla
            }
            
            System.out.println("✅ APP DEBUG: Validación de navegación exitosa");
            
            // Cargar la nueva vista
            System.out.println("📁 APP DEBUG: Cargando FXML: " + fxml);
            Parent root = loadFXML(fxml);
            System.out.println("✅ APP DEBUG: FXML cargado exitosamente");
            
            if (scene != null) {
                System.out.println("🎭 APP DEBUG: Cambiando root de scene");
                scene.setRoot(root);
                System.out.println("✅ APP DEBUG: Root cambiado exitosamente");
                
                // ✨ ACTUALIZAR SISTEMA RESPONSIVO DESPUÉS DEL CAMBIO DE VISTA
                if (responsiveManager != null) {
                    responsiveManager.forceUpdate();
                    System.out.println("📱 RESPONSIVE: Estilos responsivos actualizados para vista: " + fxml);
                } else {
                    // Fallback: mantener estilos CSS básicos si el sistema responsivo falla
                    mantenlerEstilosCSS();
                    System.out.println("⚠️ APP WARNING: Sistema responsivo no disponible, usando CSS básico");
                }
                
                System.out.println("✅ APP DEBUG: Vista cambiada completamente a: " + fxml);
                
            } else {
                System.out.println("❌ APP ERROR: Scene no está inicializada");
                throw new IOException("Scene no está inicializada");
            }
            
        } catch (IOException e) {
            System.out.println("❌ APP ERROR: Error al cambiar vista: " + e.getMessage());
            e.printStackTrace();
            
            if (!fxml.equals("LoginView")) {
                System.out.println("🔄 APP DEBUG: Intentando fallback a LoginView");
                try {
                    scene.setRoot(loadFXML("LoginView"));
                    
                    // Aplicar estilos en fallback
                    if (responsiveManager != null) {
                        responsiveManager.forceUpdate();
                    } else {
                        mantenlerEstilosCSS();
                    }
                    
                    System.out.println("✅ APP DEBUG: Fallback a LoginView exitoso");
                } catch (IOException fallbackError) {
                    System.out.println("❌ APP ERROR: Fallback también falló: " + fallbackError.getMessage());
                    throw fallbackError;
                }
            } else {
                throw e; // Si ya estamos intentando cargar LoginView y falla, propagar el error
            }
        }
    }

    /**
     * Mantener estilos CSS después de cambiar vista - FALLBACK PARA COMPATIBILIDAD
     * Este método solo se usa si el sistema responsivo no está disponible
     */
    private static void mantenlerEstilosCSS() {
        try {
            if (scene != null && scene.getStylesheets().isEmpty()) {
                System.out.println("🎨 CSS DEBUG: Aplicando estilos CSS de fallback");
                
                String[] archivosCSS = {
                    "/css/estilos-base.css",    // CSS base (tu estilos.css renombrado)
                    "/css/estilos.css",         // Compatibilidad con nombre anterior
                    "/estilos.css"              // Ubicación anterior
                };
                
                boolean cssRecargado = false;
                
                for (String archivoCSS : archivosCSS) {
                    URL cssUrl = App.class.getResource(archivoCSS);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                        cssRecargado = true;
                        System.out.println("✅ CSS DEBUG: Aplicado " + archivoCSS);
                        break; // Solo usar el primero que encuentre
                    }
                }
                
                if (!cssRecargado) {
                    System.out.println("⚠️ CSS WARNING: No se encontraron archivos CSS de fallback");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ CSS ERROR: Error al aplicar estilos de fallback: " + e.getMessage());
        }
    }
    
    /**
     * Método alternativo para cambio de vistas (compatibilidad)
     */
    public static void changeView(String fxml) throws IOException {
        setRoot(fxml);
    }
    
    /**
     * Validar si se puede navegar a la vista solicitada
     */
    private static boolean validarNavegacion(String fxml, SesionTemporal sesion) {        
        switch (fxml) {
            case "LoginView": 
                return true;
                
            case "PanelPrincipal":
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView"); 
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            case "ClientePresupuestoView":
            case "Eventos":
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView"); 
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            case "PaquetesPresupuesto":
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView");  
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                if (!sesion.hayClienteSeleccionado()) {
                    try {
                        setRoot("ClientePresupuestoView");
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            case "VistaPreviaPresupuesto":
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView"); 
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                
                if (!sesion.hayClienteSeleccionado()) {
                    try {
                        setRoot("ClientePresupuestoView");
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                
                if (!sesion.hayPaqueteSeleccionado()) {
                    try {
                        setRoot("PaquetesPresupuesto");
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            default:
                // Para otras vistas, verificar usuario logueado
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView");
                    } catch (IOException e) {
                        System.out.println("❌ NAVEGACIÓN ERROR: " + e.getMessage());
                    }
                    return false;
                }
                return true;
        }
    }

    /**
     * Cargar archivo FXML con mejor manejo de errores
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Construir la ruta del archivo FXML
        String rutaCompleta = "/vistas/" + fxml + ".fxml";
        
        try {
            URL recurso = App.class.getResource(rutaCompleta);
            
            if (recurso == null) {
                System.out.println("❌ FXML ERROR: No se encontró el archivo: " + rutaCompleta);
                throw new IOException("No se pudo encontrar el archivo FXML: " + rutaCompleta);
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(recurso);
            Parent parent = fxmlLoader.load();
            System.out.println("✅ FXML DEBUG: Cargado exitosamente: " + rutaCompleta);
            return parent;
            
        } catch (IOException e) {
            System.out.println("❌ FXML ERROR: Error al cargar " + rutaCompleta + ": " + e.getMessage());
            throw e;
        }
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static Scene getCurrentScene() {
        return scene;
    }
    
    /**
     * Mostrar estado completo de la aplicación incluyendo sistema responsivo
     */
    public static void mostrarEstadoApp() {
        System.out.println("📊 ===== ESTADO DE LA APLICACIÓN =====");
        
        // Estado de la escena
        if (scene != null) {
            System.out.println("🎭 Scene activa:");
            System.out.println("   - Ancho: " + (primaryStage != null ? primaryStage.getWidth() : "N/A"));
            System.out.println("   - Alto: " + (primaryStage != null ? primaryStage.getHeight() : "N/A"));
            System.out.println("   - Maximizada: " + (primaryStage != null ? primaryStage.isMaximized() : "N/A"));
            
            System.out.println("🎨 Hojas de estilo cargadas:");
            for (String stylesheet : scene.getStylesheets()) {
                System.out.println("   - " + stylesheet);
            }
            
            if (scene.getRoot() != null) {
                System.out.println("   - Root: " + scene.getRoot().getClass().getSimpleName());
            }
        }
        
        // Estado del sistema responsivo
        if (responsiveManager != null) {
            System.out.println("📱 Sistema Responsivo:");
            System.out.println("   - Dispositivo actual: " + responsiveManager.getCurrentDeviceType());
            System.out.println("   - Es móvil: " + responsiveManager.isMobile());
            System.out.println("   - Es tablet: " + responsiveManager.isTablet());
            System.out.println("   - Es desktop: " + responsiveManager.isDesktop());
        } else {
            System.out.println("❌ Sistema Responsivo: NO INICIALIZADO");
        }
        
        // Estado de sesión
        SesionTemporal sesion = SesionTemporal.getInstancia();
        if (sesion != null) {
            sesion.mostrarResumen();
        }
        
        System.out.println("📊 ===================================");
    }

    /**
     * Método de utilidad para reinicializar el sistema responsivo
     */
    public static void reinicializarSistemaResponsivo() {
        if (scene != null && primaryStage != null) {
            responsiveManager = new ResponsiveManager(scene, primaryStage);
            System.out.println("📱 RESPONSIVE: Sistema responsivo reinicializado");
        } else {
            System.out.println("❌ RESPONSIVE ERROR: No se puede reinicializar, scene o stage son null");
        }
    }

    /**
     * Método para obtener información del dispositivo actual
     */
    public static String getDeviceInfo() {
        if (responsiveManager != null) {
            return String.format("Dispositivo: %s | Ancho: %.0f | Alto: %.0f", 
                responsiveManager.getCurrentDeviceType(),
                primaryStage.getWidth(),
                primaryStage.getHeight()
            );
        }
        return "Sistema responsivo no disponible";
    }

    public static void main(String[] args) {
        launch();
    }
}