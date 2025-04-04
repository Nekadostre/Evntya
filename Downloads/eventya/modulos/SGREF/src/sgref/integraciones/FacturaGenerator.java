package sgref.integraciones;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

public class FacturaGenerator {

    public static void generarFactura(String cliente, String paquete, String metodoPago, double total) {
        try {
            String nombreArchivo;
            try (PDDocument documento = new PDDocument()) {
                PDPage pagina = new PDPage(PDRectangle.LETTER);
                documento.addPage(pagina);
                try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                    contenido.beginText();
                    contenido.setFont(PDType1Font.HELVETICA_BOLD, 16); // Corregido
                    contenido.setLeading(20f);
                    contenido.newLineAtOffset(50, 700);
                    contenido.showText("Factura - Salón de Eventos");
                    contenido.newLine();
                    contenido.newLine();
                    contenido.setFont(PDType1Font.HELVETICA, 12); // Corregido
                    contenido.showText("Cliente: " + cliente);
                    contenido.newLine();
                    contenido.showText("Paquete: " + paquete);
                    contenido.newLine();
                    contenido.showText("Método de pago: " + metodoPago);
                    contenido.newLine();
                    contenido.showText("Total: $" + total);
                    contenido.newLine();
                    contenido.showText(" ");
                    contenido.showText("¡Gracias por tu compra!");
                    contenido.endText();
                }
                File carpeta = new File("facturas");
                if (!carpeta.exists()) {
                    carpeta.mkdir();
                }   nombreArchivo = "facturas/factura_" + cliente.replace(" ", "_") + ".pdf";
                documento.save(nombreArchivo);
            }

            System.out.println("Factura generada correctamente: " + nombreArchivo);

        } catch (IOException e) {
            System.out.println("Error al generar factura: " + e.getMessage());
        }
    }
}
