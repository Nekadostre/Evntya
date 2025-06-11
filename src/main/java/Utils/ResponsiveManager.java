// ========== 1. CLASE MANEJADORA DE MEDIA QUERIES ==========
package utils;

import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResponsiveManager {
    
    // Breakpoints estándar (puedes personalizarlos)
    public static final double MOBILE_MAX = 768;
    public static final double TABLET_MAX = 1024;
    public static final double DESKTOP_MIN = 1025;
    public static final double LARGE_DESKTOP_MIN = 1440;
    public static final double ULTRA_WIDE_MIN = 1920;
    
    private Scene scene;
    private Stage stage;
    private List<String> currentStylesheets;
    
    public ResponsiveManager(Scene scene, Stage stage) {
        this.scene = scene;
        this.stage = stage;
        this.currentStylesheets = new ArrayList<>();
        
        // Listener para cambios de tamaño
        setupResponsiveListeners();
        
        // Aplicar estilos iniciales
        applyResponsiveStyles();
    }
    
    private void setupResponsiveListeners() {
        // Listener para ancho
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            applyResponsiveStyles();
        });
        
        // Listener para alto
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            applyResponsiveStyles();
        });
        
        // Listener para modo maximizado
        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            applyResponsiveStyles();
        });
    }
    
    private void applyResponsiveStyles() {
        double width = stage.getWidth();
        double height = stage.getHeight();
        
        // Limpiar estilos previos
        scene.getStylesheets().removeAll(currentStylesheets);
        currentStylesheets.clear();
        
        // Determinar qué CSS aplicar
        String deviceType = getDeviceType(width);
        String orientation = getOrientation(width, height);
        
        // Aplicar CSS base
        addStylesheet("/css/estilos-base.css");
        
        // Aplicar CSS específico por dispositivo
        switch (deviceType) {
            case "mobile":
                addStylesheet("/css/estilos-mobile.css");
                break;
            case "tablet":
                addStylesheet("/css/estilos-tablet.css");
                break;
            case "desktop":
                addStylesheet("/css/estilos-desktop.css");
                break;
            case "large-desktop":
                addStylesheet("/css/estilos-large-desktop.css");
                break;
            case "ultra-wide":
                addStylesheet("/css/estilos-ultra-wide.css");
                break;
        }
        
        // Aplicar CSS de orientación
        if ("landscape".equals(orientation)) {
            addStylesheet("/css/estilos-landscape.css");
        } else {
            addStylesheet("/css/estilos-portrait.css");
        }
        
        // CSS específico para resoluciones especiales
        applySpecialResolutionStyles(width, height);
        
        System.out.println("📱 RESPONSIVE: Aplicado estilo para " + deviceType + " (" + width + "x" + height + ")");
    }
    
    private String getDeviceType(double width) {
        if (width <= MOBILE_MAX) return "mobile";
        if (width <= TABLET_MAX) return "tablet";
        if (width < LARGE_DESKTOP_MIN) return "desktop";
        if (width < ULTRA_WIDE_MIN) return "large-desktop";
        return "ultra-wide";
    }
    
    private String getOrientation(double width, double height) {
        return width > height ? "landscape" : "portrait";
    }
    
    private void applySpecialResolutionStyles(double width, double height) {
        // 4K
        if (width >= 3840 && height >= 2160) {
            addStylesheet("/css/estilos-4k.css");
        }
        // QHD
        else if (width >= 2560 && height >= 1440) {
            addStylesheet("/css/estilos-qhd.css");
        }
        // Full HD
        else if (width >= 1920 && height >= 1080) {
            addStylesheet("/css/estilos-fullhd.css");
        }
        // HD
        else if (width >= 1366 && height >= 768) {
            addStylesheet("/css/estilos-hd.css");
        }
    }
    
    private void addStylesheet(String path) {
        URL cssUrl = getClass().getResource(path);
        if (cssUrl != null) {
            String stylesheet = cssUrl.toExternalForm();
            scene.getStylesheets().add(stylesheet);
            currentStylesheets.add(stylesheet);
        }
    }
    
    // Método público para forzar actualización
    public void forceUpdate() {
        applyResponsiveStyles();
    }
    
    // Getters útiles
    public String getCurrentDeviceType() {
        return getDeviceType(stage.getWidth());
    }
    
    public boolean isMobile() {
        return "mobile".equals(getCurrentDeviceType());
    }
    
    public boolean isTablet() {
        return "tablet".equals(getCurrentDeviceType());
    }
    
    public boolean isDesktop() {
        return getCurrentDeviceType().contains("desktop");
    }
}

