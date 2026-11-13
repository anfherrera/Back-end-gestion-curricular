package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.List;

/** Reglas de Paz y Salvo que dependen del nombre de los archivos de la solicitud. */
public final class DocumentosPazYSalvo {

    private DocumentosPazYSalvo() {
    }

    /** Oficio o resolución: lo que sube la secretaría (por el nombre del archivo). */
    public static boolean esOficio(String nombreArchivo) {
        String n = nombreArchivo.toLowerCase();
        return n.contains("oficio") || n.contains("resolucion") || n.contains("paz") || n.contains("salvo")
                || n.contains("aprobacion");
    }

    /** Oficio en PDF: los que se listan y descargan como oficio de la solicitud. */
    public static boolean esOficioPdf(String nombreArchivo) {
        return nombreArchivo != null && nombreArchivo.toLowerCase().endsWith(".pdf") && esOficio(nombreArchivo);
    }

    /** Los oficios en PDF de la lista, en su orden. Lista vacía si es null. */
    public static List<Documento> oficiosPdf(List<Documento> documentos) {
        if (documentos == null) {
            return List.of();
        }
        return documentos.stream().filter(d -> esOficioPdf(d.getNombre())).toList();
    }

    /** Tipo de documento que se muestra según el nombre del archivo. */
    public static String tipoDeDocumento(String nombreArchivo) {
        String n = nombreArchivo.toLowerCase();
        if (esOficio(n)) {
            return "Oficio/Resolución";
        } else if (n.contains("formato") || n.contains("pm_fo_4_for_27")) {
            return "Formato PM-FO-4-FOR-27";
        } else if (n.contains("autorizacion") || n.contains("publicar")) {
            return "Autorización de Publicación";
        } else if (n.contains("hoja") && n.contains("vida")) {
            return "Hoja de Vida Académica";
        } else if (n.contains("comprobante") || n.contains("pago")) {
            return "Comprobante de Pago";
        } else if (n.contains("trabajo") && n.contains("grado")) {
            return "Documento de Trabajo de Grado";
        } else if (n.contains("saber") && n.contains("pro")) {
            return "Resultado Saber Pro";
        }
        return "Documento del Estudiante";
    }
}
