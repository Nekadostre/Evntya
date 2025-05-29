package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import modelos.SesionTemporal;
import modelos.Extra;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GeneradorPDF {
    
    private static final Logger LOGGER = Logger.getLogger(GeneradorPDF.class.getName());
    
    /**
     * Genera un PDF de presupuesto
     */
    public static File generarPresupuestoPDF(SesionTemporal sesion, String rutaArchivo) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            
            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                // Configurar fuentes - SINTAXIS CORRECTA PARA PDFBox 2.x
                PDType1Font fuenteTitulo = PDType1Font.HELVETICA_BOLD;
                PDType1Font fuenteNormal = PDType1Font.HELVETICA;
                PDType1Font fuenteNegrita = PDType1Font.HELVETICA_BOLD;
                
                float margenIzquierdo = 50;
                float yActual = 750;
                
                // TÍTULO
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 20);
                contenido.newLineAtOffset(200, yActual);
                contenido.showText("PRESUPUESTO DE EVENTO");
                contenido.endText();
                yActual -= 50;
                
                // Fecha del presupuesto
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                contenido.beginText();
                contenido.setFont(fuenteNormal, 12);
                contenido.newLineAtOffset(400, yActual);
                contenido.showText("Fecha: " + LocalDate.now().format(formatter));
                contenido.endText();
                yActual -= 30;
                
                // DATOS DEL CLIENTE
                yActual = escribirCampo(contenido, "Nombre del cliente:", 
                                      sesion.getClienteNombreCompleto(), 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                // Check if methods exist before calling them
                String rfc = "No disponible";
                try {
                    rfc = sesion.getClienteRfc();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Método getClienteRfc() no disponible: " + e.getMessage());
                }
                yActual = escribirCampo(contenido, "RFC:", rfc, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                String telefono = "No disponible";
                try {
                    telefono = sesion.getClienteTelefono();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Método getClienteTelefono() no disponible: " + e.getMessage());
                }
                yActual = escribirCampo(contenido, "Teléfono:", telefono, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                String email = "No disponible";
                try {
                    email = sesion.getClienteEmail();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Método getClienteEmail() no disponible: " + e.getMessage());
                }
                yActual = escribirCampo(contenido, "Email:", email, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                yActual -= 20;
                
                // DATOS DEL EVENTO
                String fechaEvento = "No especificada";
                try {
                    if (sesion.getFechaEvento() != null) {
                        fechaEvento = sesion.getFechaEvento().format(formatter);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Método getFechaEvento() no disponible: " + e.getMessage());
                }
                yActual = escribirCampo(contenido, "Fecha del evento:", fechaEvento, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                String horarioEvento = "No especificado";
                try {
                    if (sesion.getHorarioEvento() != null) {
                        horarioEvento = sesion.getHorarioEvento().toUpperCase();
                    }
                } catch (Exception e) {
                    // Try alternative method name
                    try {
                        if (sesion.getHorarioPresupuesto() != null) {
                            horarioEvento = sesion.getHorarioPresupuesto().toUpperCase();
                        }
                    } catch (Exception e2) {
                        LOGGER.log(Level.WARNING, "Métodos de horario no disponibles: " + e2.getMessage());
                    }
                }
                yActual = escribirCampo(contenido, "Horario del evento:", horarioEvento, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                yActual -= 20;
                
                // PAQUETE SELECCIONADO
                yActual = escribirCampo(contenido, "Paquete seleccionado:", 
                                      sesion.getPaqueteNombre() + " - $" + String.format("%.2f", sesion.getPaquetePrecio()), 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                // EXTRAS
                StringBuilder extrasTexto = new StringBuilder();
                try {
                    if (sesion.tieneExtras() && sesion.getExtrasSeleccionados() != null) {
                        for (Extra extra : sesion.getExtrasSeleccionados()) {
                            if (extra.getCantidad() > 0) {
                                extrasTexto.append(extra.getNombre())
                                          .append(" x").append(extra.getCantidad())
                                          .append(" ($").append(String.format("%.2f", extra.getPrecio() * extra.getCantidad()))
                                          .append("), ");
                            }
                        }
                    } else {
                        extrasTexto.append("Sin extras");
                    }
                } catch (Exception e) {
                    // Fallback to simple extras description
                    String resumenExtras = sesion.getResumenExtras();
                    if (resumenExtras != null && !resumenExtras.trim().isEmpty() && 
                        !resumenExtras.equals("Sin extras seleccionados")) {
                        extrasTexto.append(resumenExtras);
                    } else {
                        extrasTexto.append("Sin extras");
                    }
                }
                
                yActual = escribirCampoMultilinea(contenido, "Extras:", 
                                                extrasTexto.toString(), 
                                                margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                // CONDICIONES DE PAGO
                String plazos = "No especificado";
                try {
                    plazos = sesion.getPlazosPresupuesto();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Método getPlazosPresupuesto() no disponible: " + e.getMessage());
                }
                yActual = escribirCampo(contenido, "Plazos:", plazos, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                String formaPago = "No especificado";
                try {
                    formaPago = sesion.getFormaPagoPresupuesto();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Método getFormaPagoPresupuesto() no disponible: " + e.getMessage());
                }
                yActual = escribirCampo(contenido, "Formas de pago:", formaPago, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                yActual -= 30;
                
                // TOTAL
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 16);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("TOTAL: $" + String.format("%.2f", sesion.getTotalGeneral()) + " MXN");
                contenido.endText();
                
                yActual -= 40;
                
                // TÉRMINOS Y CONDICIONES
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("• Presupuesto válido por 30 días");
                contenido.endText();
                yActual -= 15;
                
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("• Para confirmar su evento, se requiere el 50% de anticipo");
                contenido.endText();
                yActual -= 15;
                
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("• El saldo restante se paga el día del evento");
                contenido.endText();
            }
            
            File archivo = new File(rutaArchivo);
            
            // Ensure parent directory exists
            File parentDir = archivo.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            documento.save(archivo);
            System.out.println("✅ PDF de presupuesto generado: " + rutaArchivo);
            return archivo;
        }
    }
    
    /**
     * Genera un PDF de contrato
     */
    public static File generarContratoPDF(SesionTemporal sesion, String nombreFestejado, LocalDate fechaContrato, String rutaArchivo) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            
            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                // Configurar fuentes - SINTAXIS CORRECTA PARA PDFBox 2.x
                PDType1Font fuenteTitulo = PDType1Font.HELVETICA_BOLD;
                PDType1Font fuenteNormal = PDType1Font.HELVETICA;
                PDType1Font fuenteNegrita = PDType1Font.HELVETICA_BOLD;
                
                float margenIzquierdo = 50;
                float yActual = 750;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                // TÍTULO
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 20);
                contenido.newLineAtOffset(200, yActual);
                contenido.showText("CONTRATO DE SERVICIOS");
                contenido.endText();
                yActual -= 50;
                
                // Fecha del contrato
                contenido.beginText();
                contenido.setFont(fuenteNormal, 12);
                contenido.newLineAtOffset(400, yActual);
                contenido.showText("Fecha: " + fechaContrato.format(formatter));
                contenido.endText();
                yActual -= 30;
                
                // DATOS DEL CLIENTE
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 14);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("DATOS DEL CLIENTE");
                contenido.endText();
                yActual -= 25;
                
                yActual = escribirCampo(contenido, "Nombre:", 
                                      sesion.getClienteNombreCompleto(), 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                // Safe method calls with error handling
                String rfc = "No disponible";
                try {
                    rfc = sesion.getClienteRfc();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "RFC no disponible");
                }
                yActual = escribirCampo(contenido, "RFC:", rfc, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                String telefono = "No disponible";
                try {
                    telefono = sesion.getClienteTelefono();
                    if (telefono != null && !telefono.equals("No disponible")) {
                        yActual = escribirCampo(contenido, "Teléfono:", telefono, 
                                              margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Teléfono no disponible");
                }
                
                String email = "No disponible";
                try {
                    email = sesion.getClienteEmail();
                    if (email != null && !email.equals("No disponible")) {
                        yActual = escribirCampo(contenido, "Email:", email, 
                                              margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Email no disponible");
                }
                
                yActual -= 20;
                
                // DATOS DEL EVENTO
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 14);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("DATOS DEL EVENTO");
                contenido.endText();
                yActual -= 25;
                
                yActual = escribirCampo(contenido, "Festejado:", 
                                      nombreFestejado, 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                try {
                    yActual = escribirCampo(contenido, "Fecha del evento:", 
                                          sesion.getFechaEvento().format(formatter), 
                                          margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                } catch (Exception e) {
                    yActual = escribirCampo(contenido, "Fecha del evento:", 
                                          "No especificada", 
                                          margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                }
                
                try {
                    if (sesion.getHorarioEvento() != null) {
                        yActual = escribirCampo(contenido, "Horario:", 
                                              sesion.getHorarioEvento().toUpperCase(), 
                                              margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                    }
                } catch (Exception e) {
                    try {
                        if (sesion.getHorarioPresupuesto() != null) {
                            yActual = escribirCampo(contenido, "Horario:", 
                                                  sesion.getHorarioPresupuesto().toUpperCase(), 
                                                  margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                        }
                    } catch (Exception e2) {
                        // Ignore if no horario methods available
                    }
                }
                
                yActual -= 20;
                
                // SERVICIOS CONTRATADOS
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 14);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("SERVICIOS CONTRATADOS");
                contenido.endText();
                yActual -= 25;
                
                yActual = escribirCampo(contenido, "Paquete:", 
                                      sesion.getPaqueteNombre(), 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                yActual = escribirCampo(contenido, "Precio del paquete:", 
                                      "$" + String.format("%.2f", sesion.getPaquetePrecio()) + " MXN", 
                                      margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                
                // Extras if available
                try {
                    if (sesion.tieneExtras() && sesion.getExtrasSeleccionados() != null) {
                        contenido.beginText();
                        contenido.setFont(fuenteNegrita, 12);
                        contenido.newLineAtOffset(margenIzquierdo, yActual);
                        contenido.showText("Extras contratados:");
                        contenido.endText();
                        yActual -= 20;
                        
                        for (Extra extra : sesion.getExtrasSeleccionados()) {
                            if (extra.getCantidad() > 0) {
                                String extraTexto = "• " + extra.getNombre() + " x" + extra.getCantidad() + 
                                                  " - $" + String.format("%.2f", extra.getPrecio() * extra.getCantidad());
                                contenido.beginText();
                                contenido.setFont(fuenteNormal, 10);
                                contenido.newLineAtOffset(margenIzquierdo + 20, yActual);
                                contenido.showText(extraTexto);
                                contenido.endText();
                                yActual -= 15;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Fallback to simple extras display
                    String resumenExtras = sesion.getResumenExtras();
                    if (resumenExtras != null && !resumenExtras.trim().isEmpty() && 
                        !resumenExtras.equals("Sin extras seleccionados")) {
                        yActual = escribirCampo(contenido, "Extras:", resumenExtras, 
                                              margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                    }
                }
                
                yActual -= 20;
                
                // CONDICIONES DE PAGO
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 14);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("CONDICIONES DE PAGO");
                contenido.endText();
                yActual -= 25;
                
                try {
                    if (sesion.getFormaPagoPresupuesto() != null) {
                        yActual = escribirCampo(contenido, "Forma de pago:", 
                                              sesion.getFormaPagoPresupuesto(), 
                                              margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Forma de pago no disponible");
                }
                
                try {
                    if (sesion.getPlazosPresupuesto() != null) {
                        yActual = escribirCampo(contenido, "Plazos:", 
                                              sesion.getPlazosPresupuesto(), 
                                              margenIzquierdo, yActual, fuenteNegrita, fuenteNormal);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Plazos no disponible");
                }
                
                yActual -= 30;
                
                // TOTAL A PAGAR
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 16);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("TOTAL A PAGAR: $" + String.format("%.2f", sesion.getTotalGeneral()) + " MXN");
                contenido.endText();
                yActual -= 50;
                
                // FIRMAS
                contenido.beginText();
                contenido.setFont(fuenteTitulo, 14);
                contenido.newLineAtOffset(margenIzquierdo, yActual);
                contenido.showText("FIRMAS");
                contenido.endText();
                yActual -= 40;
                
                // Líneas para firmas
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(80, yActual);
                contenido.showText("_______________________");
                contenido.endText();
                
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(350, yActual);
                contenido.showText("_______________________");
                contenido.endText();
                yActual -= 20;
                
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(120, yActual);
                contenido.showText("CLIENTE");
                contenido.endText();
                
                contenido.beginText();
                contenido.setFont(fuenteNormal, 10);
                contenido.newLineAtOffset(390, yActual);
                contenido.showText("EMPRESA");
                contenido.endText();
            }
            
            File archivo = new File(rutaArchivo);
            
            // Ensure parent directory exists
            File parentDir = archivo.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            documento.save(archivo);
            System.out.println("✅ PDF de contrato generado: " + rutaArchivo);
            return archivo;
        }
    }
    
    /**
     * Método auxiliar para escribir un campo con etiqueta y valor
     */
    private static float escribirCampo(PDPageContentStream contenido, String etiqueta, String valor, 
                                      float x, float y, PDType1Font fuenteEtiqueta, PDType1Font fuenteValor) throws IOException {
        // Escribir etiqueta
        contenido.beginText();
        contenido.setFont(fuenteEtiqueta, 12);
        contenido.newLineAtOffset(x, y);
        contenido.showText(etiqueta);
        contenido.endText();
        
        // Escribir valor
        contenido.beginText();
        contenido.setFont(fuenteValor, 12);
        contenido.newLineAtOffset(x + 150, y);
        contenido.showText(valor != null ? valor : "No especificado");
        contenido.endText();
        
        return y - 20;
    }
    
    /**
     * Método auxiliar para escribir campos multilínea
     */
    private static float escribirCampoMultilinea(PDPageContentStream contenido, String etiqueta, String valor, 
                                               float x, float y, PDType1Font fuenteEtiqueta, PDType1Font fuenteValor) throws IOException {
        // Escribir etiqueta
        contenido.beginText();
        contenido.setFont(fuenteEtiqueta, 12);
        contenido.newLineAtOffset(x, y);
        contenido.showText(etiqueta);
        contenido.endText();
        
        // Dividir texto largo en líneas
        String[] lineas = dividirTexto(valor != null ? valor : "No especificado", 60);
        float yActual = y;
        
        for (String linea : lineas) {
            contenido.beginText();
            contenido.setFont(fuenteValor, 10);
            contenido.newLineAtOffset(x + 150, yActual);
            contenido.showText(linea);
            contenido.endText();
            yActual -= 15;
        }
        
        return yActual - 10;
    }
    
    /**
     * Divide un texto largo en líneas más cortas
     */
    private static String[] dividirTexto(String texto, int maxCaracteres) {
        if (texto.length() <= maxCaracteres) {
            return new String[]{texto};
        }
        
        java.util.List<String> lineas = new java.util.ArrayList<>();
        int inicio = 0;
        
        while (inicio < texto.length()) {
            int fin = Math.min(inicio + maxCaracteres, texto.length());
            if (fin < texto.length()) {
                // Buscar el último espacio para no cortar palabras
                int ultimoEspacio = texto.lastIndexOf(' ', fin);
                if (ultimoEspacio > inicio) {
                    fin = ultimoEspacio;
                }
            }
            lineas.add(texto.substring(inicio, fin).trim());
            inicio = fin + 1;
        }
        
        return lineas.toArray(new String[0]);
    }
}