package controladores;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelos.SesionTemporal;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        try {
            Parent root = loadFXML("LoginView");
            scene = new Scene(root, 800, 600);
            
            cargarEstilosCSS();
            
            stage.setTitle("SEGUNDO CASTILLO - Sistema de Gestión");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
 
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Cargar hojas de estilo CSS - OPTIMIZADO PARA TU PROYECTO
     */
    private void cargarEstilosCSS() {
        try {
            
            // Buscar archivos CSS en las ubicaciones de tu proyecto
            String[] archivosCSS = {
                "/estilos.css",           // En la raíz de resources
                "/css/estilos.css",       // En carpeta css
                "/css/styles.css",        // Alternativo
                "/application.css"        // Otro alternativo
            };
            
            boolean cssEncontrado = false;
            
            for (String archivoCSS : archivosCSS) {
                URL cssUrl = getClass().getResource(archivoCSS);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                    cssEncontrado = true;
                }
            }
            
            // Información detallada sobre el estado de CSS
            if (cssEncontrado) {
                for (String stylesheet : scene.getStylesheets()) {
                }
            } else {
                for (String archivo : archivosCSS) {
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Método principal para cambiar vistas con validación de sesión
     */
    public static void setRoot(String fxml) throws IOException {
        
        // Verificar que no sea null
        if (fxml == null || fxml.trim().isEmpty()) {
            throw new IOException("El nombre del FXML no puede ser null o vacío");
        }
        
        try {
            // Log del estado actual de sesión
            SesionTemporal sesion = SesionTemporal.getInstancia();
            // Validaciones específicas por vista
            if (!validarNavegacion(fxml, sesion)) {
                return; // No continuar si la validación falla
            }
            
            // Cargar la nueva vista
            Parent root = loadFXML(fxml);
            
            if (scene != null) {
                scene.setRoot(root);
                
                // Mantener estilos CSS después del cambio de vista
                mantenlerEstilosCSS();
                
            } else {
                throw new IOException("Scene no está inicializada");
            }
            
        } catch (IOException e) {
            if (!fxml.equals("LoginView")) {
                try {
                    scene.setRoot(loadFXML("LoginView"));
                    mantenlerEstilosCSS();
                } catch (IOException fallbackError) {
                    throw fallbackError;
                }
            } else {
                throw e; // Si ya estamos intentando cargar LoginView y falla, propagar el error
            }
        }
    }
    
    /**
     * Mantener estilos CSS después de cambiar vista - OPTIMIZADO
     */
    private static void mantenlerEstilosCSS() {
        try {
            if (scene != null && scene.getStylesheets().isEmpty()) {
                
                String[] archivosCSS = {
                    "/css/estilos.css",             
                };
                
                boolean cssRecargado = false;
                
                for (String archivoCSS : archivosCSS) {
                    URL cssUrl = App.class.getResource(archivoCSS);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                        cssRecargado = true;
                    }
                }
                
                if (!cssRecargado) {
                }
            } else if (scene != null) {
            }
        } catch (Exception e) {
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
                    }
                    return false;
                }
                return true;
                
            case "PaquetesPresupuesto":
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView");  
                    } catch (IOException e) {
                    }
                    return false;
                }
                if (!sesion.hayClienteSeleccionado()) {
                    try {
                        setRoot("ClientePresupuestoView");
                    } catch (IOException e) {
                    }
                    return false;
                }
                return true;
                
            case "VistaPreviaPresupuesto":
                
                if (!sesion.hayUsuarioLogueado()) {
                    try {
                        setRoot("LoginView"); 
                    } catch (IOException e) {
                    }
                    return false;
                }
                
                if (!sesion.hayClienteSeleccionado()) {
                    try {
                        setRoot("ClientePresupuestoView");
                    } catch (IOException e) {
                    }
                    return false;
                }
                
                if (!sesion.hayPaqueteSeleccionado()) {
                    try {
                        setRoot("PaquetesPresupuesto");
                    } catch (IOException e) {
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
                
                throw new IOException("No se pudo encontrar el archivo FXML: " + rutaCompleta);
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(recurso);
            Parent parent = fxmlLoader.load();
            return parent;
            
        } catch (IOException e) {
            URL recurso = App.class.getResource(rutaCompleta);
            if (recurso == null) {
            } else {
            }
            throw e;
        }
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static Scene getCurrentScene() {
        return scene;
    }
    

    public static void mostrarEstadoApp() {
        if (scene != null) {
            for (String stylesheet : scene.getStylesheets()) {
                System.out.println("   - " + stylesheet);
            }
            
            if (scene.getRoot() != null) {
            }
        }
        
        SesionTemporal sesion = SesionTemporal.getInstancia();
        if (sesion != null) {
            sesion.mostrarResumen();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}