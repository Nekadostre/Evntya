package utils;

import controladores.App;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class ResponsiveHelper {
    
    public static void makeResponsive(Node node) {
        ResponsiveManager manager = App.getResponsiveManager();
        if (manager != null) {
            
            // Aplicar clases CSS dinámicas
            String deviceType = manager.getCurrentDeviceType();
            node.getStyleClass().removeIf(style -> 
                style.startsWith("device-") || style.startsWith("responsive-"));
            
            node.getStyleClass().addAll(
                "device-" + deviceType,
                "responsive-component"
            );
            
            // Ajustar tamaños específicos si es Region
            if (node instanceof Region) {
                Region region = (Region) node;
                adjustRegionForDevice(region, deviceType);
            }
        }
    }
    
    private static void adjustRegionForDevice(Region region, String deviceType) {
        switch (deviceType) {
            case "mobile":
                region.setMaxWidth(Region.USE_PREF_SIZE);
                region.setPrefWidth(300);
                break;
            case "tablet":
                region.setMaxWidth(Region.USE_PREF_SIZE);
                region.setPrefWidth(600);
                break;
            case "desktop":
                region.setMaxWidth(Region.USE_PREF_SIZE);
                region.setPrefWidth(800);
                break;
            case "large-desktop":
                region.setMaxWidth(Region.USE_PREF_SIZE);
                region.setPrefWidth(1200);
                break;
            case "ultra-wide":
                region.setMaxWidth(Region.USE_PREF_SIZE);
                region.setPrefWidth(1600);
                break;
        }
    }
    
    // Métodos de utilidad para controladores
    public static boolean shouldShowMobileLayout() {
        ResponsiveManager manager = App.getResponsiveManager();
        return manager != null && manager.isMobile();
    }
    
    public static boolean shouldUseCompactMode() {
        ResponsiveManager manager = App.getResponsiveManager();
        return manager != null && (manager.isMobile() || manager.isTablet());
    }
}