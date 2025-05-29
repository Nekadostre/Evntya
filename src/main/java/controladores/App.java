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
        System.out.println("🚀 Iniciando aplicación SEGUNDO CASTILLO...");
        
        primaryStage = stage;
        
        try {
            // CORRECCIÓN: Usar LoginView en lugar de Login
            Parent root = loadFXML("LoginView");
            scene = new Scene(root, 800, 600);
            
            // Cargar hojas de estilo CSS si existen
            cargarEstilosCSS();
            
            stage.setTitle("SEGUNDO CASTILLO - Sistema de Gestión");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            
            System.out.println("✅ Aplicación iniciada correctamente con estilos CSS");
            
        } catch (IOException e) {
            System.err.println("❌ Error crítico al iniciar aplicación: " + e.getMessage());
            System.err.println("💡 Verifica que LoginView.fxml existe en src/main/resources/vistas/");
            throw e;
        }
    }

    /**
     * Cargar hojas de estilo CSS - OPTIMIZADO PARA TU PROYECTO
     */
    private void cargarEstilosCSS() {
        try {
            System.out.println("🎨 Cargando estilos CSS...");
            
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
                    System.out.println("✅ CSS cargado exitosamente: " + archivoCSS);
                    System.out.println("   URL: " + cssUrl.toString());
                    cssEncontrado = true;
                }
            }
            
            // Información detallada sobre el estado de CSS
            if (cssEncontrado) {
                System.out.println("🎨 Total de hojas de estilo cargadas: " + scene.getStylesheets().size());
                for (String stylesheet : scene.getStylesheets()) {
                    System.out.println("   - " + stylesheet);
                }
            } else {
                System.out.println("⚠️ No se encontraron archivos CSS");
                System.out.println("💡 Ubicaciones verificadas:");
                for (String archivo : archivosCSS) {
                    System.out.println("   - " + archivo);
                }
                System.out.println("💡 Tu estructura actual: src/main/resources/estilos.css");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar CSS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método principal para cambiar vistas con validación de sesión
     */
    public static void setRoot(String fxml) throws IOException {
        System.out.println("🔄 App.setRoot() llamado con: " + fxml);
        
        // Verificar que no sea null
        if (fxml == null || fxml.trim().isEmpty()) {
            System.err.println("❌ FXML es null o vacío");
            throw new IOException("El nombre del FXML no puede ser null o vacío");
        }
        
        try {
            // Log del estado actual de sesión
            SesionTemporal sesion = SesionTemporal.getInstancia();
            System.out.println("📊 Estado de sesión antes de navegar:");
            System.out.println("   - Usuario logueado: " + sesion.hayUsuarioLogueado());
            System.out.println("   - Cliente seleccionado: " + sesion.hayClienteSeleccionado());
            System.out.println("   - Paquete seleccionado: " + sesion.hayPaqueteSeleccionado());
            
            // Validaciones específicas por vista
            if (!validarNavegacion(fxml, sesion)) {
                return; // No continuar si la validación falla
            }
            
            // Cargar la nueva vista
            Parent root = loadFXML(fxml);
            
            if (scene != null) {
                scene.setRoot(root);
                System.out.println("✅ Vista cambiada exitosamente a: " + fxml);
                
                // Mantener estilos CSS después del cambio de vista
                mantenlerEstilosCSS();
                
            } else {
                System.err.println("❌ Scene es null");
                throw new IOException("Scene no está inicializada");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error al cargar FXML '" + fxml + "': " + e.getMessage());
            e.printStackTrace();
            
            // Intentar cargar vista de error o regresar a login
            if (!fxml.equals("LoginView")) {
                System.out.println("🔄 Intentando regresar a LoginView como fallback...");
                try {
                    scene.setRoot(loadFXML("LoginView"));
                    mantenlerEstilosCSS();
                } catch (IOException fallbackError) {
                    System.err.println("❌ Error crítico: No se puede cargar ni la vista solicitada ni LoginView");
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
                System.out.println("🎨 Recargando estilos CSS después del cambio de vista...");
                
                // Recargar CSS con las mismas ubicaciones que en el inicio
                String[] archivosCSS = {
                    "/estilos.css",           // Tu archivo principal
                    "/css/estilos.css",       
                    "/css/styles.css",        
                    "/application.css"        
                };
                
                boolean cssRecargado = false;
                
                for (String archivoCSS : archivosCSS) {
                    URL cssUrl = App.class.getResource(archivoCSS);
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                        System.out.println("🎨 CSS recargado: " + archivoCSS);
                        cssRecargado = true;
                    }
                }
                
                if (!cssRecargado) {
                    System.out.println("⚠️ No se pudieron recargar los estilos CSS");
                }
            } else if (scene != null) {
                System.out.println("✅ Estilos CSS mantenidos: " + scene.getStylesheets().size() + " hojas");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al mantener CSS: " + e.getMessage());
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
        System.out.println("🔍 Validando navegación a: " + fxml);
        
        switch (fxml) {
            case "LoginView":  // CORREGIDO: Cambiar Login por LoginView
                // Siempre permitir ir a LoginView
                return true;
                
            case "PanelPrincipal":
                if (!sesion.hayUsuarioLogueado()) {
                    System.err.println("❌ Acceso denegado a PanelPrincipal: Usuario no logueado");
                    try {
                        setRoot("LoginView");  // CORREGIDO
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a LoginView: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            case "ClientePresupuestoView":
            case "Eventos":
                if (!sesion.hayUsuarioLogueado()) {
                    System.err.println("❌ Acceso denegado a " + fxml + ": Usuario no logueado");
                    try {
                        setRoot("LoginView");  // CORREGIDO
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a LoginView: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            case "PaquetesPresupuesto":
                if (!sesion.hayUsuarioLogueado()) {
                    System.err.println("❌ Acceso denegado a PaquetesPresupuesto: Usuario no logueado");
                    try {
                        setRoot("LoginView");  // CORREGIDO
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a LoginView: " + e.getMessage());
                    }
                    return false;
                }
                if (!sesion.hayClienteSeleccionado()) {
                    System.err.println("❌ Acceso denegado a PaquetesPresupuesto: No hay cliente seleccionado");
                    try {
                        setRoot("ClientePresupuestoView");
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a ClientePresupuestoView: " + e.getMessage());
                    }
                    return false;
                }
                return true;
                
            case "VistaPreviaPresupuesto":
                System.out.println("🔍 Validando acceso a VistaPreviaPresupuesto...");
                
                if (!sesion.hayUsuarioLogueado()) {
                    System.err.println("❌ Acceso denegado a VistaPreviaPresupuesto: Usuario no logueado");
                    try {
                        setRoot("LoginView");  // CORREGIDO
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a LoginView: " + e.getMessage());
                    }
                    return false;
                }
                
                if (!sesion.hayClienteSeleccionado()) {
                    System.err.println("❌ Acceso denegado a VistaPreviaPresupuesto: No hay cliente seleccionado");
                    try {
                        setRoot("ClientePresupuestoView");
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a ClientePresupuestoView: " + e.getMessage());
                    }
                    return false;
                }
                
                if (!sesion.hayPaqueteSeleccionado()) {
                    System.err.println("❌ Acceso denegado a VistaPreviaPresupuesto: No hay paquete seleccionado");
                    try {
                        setRoot("PaquetesPresupuesto");
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a PaquetesPresupuesto: " + e.getMessage());
                    }
                    return false;
                }
                
                System.out.println("✅ Validación exitosa para VistaPreviaPresupuesto");
                System.out.println("   - Cliente: " + sesion.getClienteNombreCompleto());
                System.out.println("   - Paquete: " + sesion.getPaqueteNombre());
                System.out.println("   - Total: $" + sesion.getTotalGeneral());
                return true;
                
            default:
                // Para otras vistas, verificar usuario logueado
                if (!sesion.hayUsuarioLogueado()) {
                    System.err.println("❌ Acceso denegado a " + fxml + ": Usuario no logueado");
                    try {
                        setRoot("LoginView");  // CORREGIDO
                    } catch (IOException e) {
                        System.err.println("❌ Error al redirigir a LoginView: " + e.getMessage());
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
        System.out.println("📂 Cargando FXML: " + fxml);
        
        // Construir la ruta del archivo FXML
        String rutaCompleta = "/vistas/" + fxml + ".fxml";
        System.out.println("📂 Ruta completa: " + rutaCompleta);
        
        try {
            // Verificar que el recurso existe antes de crear el FXMLLoader
            URL recurso = App.class.getResource(rutaCompleta);
            
            if (recurso == null) {
                System.err.println("❌ Archivo no encontrado: " + rutaCompleta);
                System.err.println("💡 Verifica que el archivo existe en src/main/resources/vistas/");
                System.err.println("💡 Estructura esperada:");
                System.err.println("   src/main/resources/vistas/" + fxml + ".fxml");
                throw new IOException("No se pudo encontrar el archivo FXML: " + rutaCompleta);
            }
            
            System.out.println("✅ Recurso encontrado: " + recurso.toString());
            
            // Crear FXMLLoader con la URL del recurso
            FXMLLoader fxmlLoader = new FXMLLoader(recurso);
            Parent parent = fxmlLoader.load();
            
            System.out.println("✅ FXML cargado exitosamente: " + fxml);
            return parent;
            
        } catch (IOException e) {
            System.err.println("❌ Error al cargar FXML: " + rutaCompleta);
            System.err.println("   Mensaje: " + e.getMessage());
            System.err.println("   Tipo de error: " + e.getClass().getSimpleName());
            
            // Información adicional de debugging
            URL recurso = App.class.getResource(rutaCompleta);
            if (recurso == null) {
                System.err.println("💡 POSIBLES SOLUCIONES:");
                System.err.println("   1. Verifica que el archivo " + fxml + ".fxml existe");
                System.err.println("   2. Verifica que esté en src/main/resources/vistas/");
                System.err.println("   3. Verifica que el nombre sea exacto (sensible a mayúsculas)");
                System.err.println("   4. Recompila el proyecto (mvn clean compile)");
            } else {
                System.err.println("💡 El archivo existe pero hay un error en su contenido");
                System.err.println("   1. Verifica la sintaxis XML del FXML");
                System.err.println("   2. Verifica que el fx:controller esté correcto");
                System.err.println("   3. Verifica que no haya errores en el controlador");
            }
            
            throw e;
        }
    }
    
    /**
     * Obtener la ventana principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Obtener la escena actual
     */
    public static Scene getCurrentScene() {
        return scene;
    }
    
    /**
     * Método para debug - mostrar estado actual
     */
    public static void mostrarEstadoApp() {
        System.out.println("========== ESTADO DE APP ==========");
        System.out.println("Primary Stage: " + (primaryStage != null ? "OK" : "NULL"));
        System.out.println("Scene: " + (scene != null ? "OK" : "NULL"));
        
        if (scene != null) {
            System.out.println("CSS Stylesheets: " + scene.getStylesheets().size());
            for (String stylesheet : scene.getStylesheets()) {
                System.out.println("   - " + stylesheet);
            }
            
            if (scene.getRoot() != null) {
                System.out.println("Root actual: " + scene.getRoot().getClass().getSimpleName());
            }
        }
        
        SesionTemporal sesion = SesionTemporal.getInstancia();
        if (sesion != null) {
            sesion.mostrarResumen();
        }
        System.out.println("===================================");
    }

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando aplicación SEGUNDO CASTILLO...");
        launch();
    }
}