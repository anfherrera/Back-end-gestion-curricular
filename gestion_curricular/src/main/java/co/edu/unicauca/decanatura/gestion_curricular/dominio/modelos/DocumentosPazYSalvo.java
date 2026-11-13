package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Documentos que entrega el estudiante, con la clave que usa la respuesta. Un archivo cuenta para el
     * primero de esta lista cuyo nombre encaja.
     */
    public enum Requerido {
        FORMATO_PM_FO_4_FOR_27("formato_pm_fo_4_for_27", true),
        AUTORIZACION_PUBLICAR("autorizacion_publicar", true),
        HOJA_VIDA_ACADEMICA("hoja_vida_academica", true),
        COMPROBANTE_PAGO("comprobante_pago", true),
        DOCUMENTO_TRABAJO_GRADO("documento_trabajo_grado", true),
        /** Se informa, pero no hace falta para que la solicitud esté completa. */
        RESULTADO_SABER_PRO("resultado_saber_pro", false);

        private final String clave;
        private final boolean obligatorio;

        Requerido(String clave, boolean obligatorio) {
            this.clave = clave;
            this.obligatorio = obligatorio;
        }

        public String clave() {
            return clave;
        }

        public boolean obligatorio() {
            return obligatorio;
        }

        /** @param n nombre del archivo en minúsculas */
        private boolean encaja(String n) {
            return switch (this) {
                case FORMATO_PM_FO_4_FOR_27 -> n.contains("formato") && n.contains("pm-fo-4-for-27");
                case AUTORIZACION_PUBLICAR -> n.contains("autorizacion") && n.contains("publicar");
                case HOJA_VIDA_ACADEMICA -> n.contains("hoja") && n.contains("vida") && n.contains("academica");
                case COMPROBANTE_PAGO -> n.contains("comprobante") && n.contains("pago");
                case DOCUMENTO_TRABAJO_GRADO -> n.contains("documento") && n.contains("trabajo") && n.contains("grado");
                case RESULTADO_SABER_PRO -> n.contains("resultado") && n.contains("saber");
            };
        }
    }

    /**
     * @param entregados      cada documento requerido, en el orden de {@link Requerido}, y si hay un archivo para él
     * @param todosCompletos  true si están todos los obligatorios
     * @param totalDocumentos archivos de la solicitud, tengan nombre o no
     */
    public record RevisionDocumentos(Map<Requerido, Boolean> entregados, boolean todosCompletos, int totalDocumentos) {
    }

    /** El documento requerido al que corresponde el archivo, si corresponde a alguno. */
    public static Optional<Requerido> requeridoDe(String nombreArchivo) {
        if (nombreArchivo == null) {
            return Optional.empty();
        }
        String n = nombreArchivo.toLowerCase();
        return Arrays.stream(Requerido.values()).filter(r -> r.encaja(n)).findFirst();
    }

    /** Qué documentos requeridos hay entre los archivos de la solicitud (null cuenta como ninguno). */
    public static RevisionDocumentos revisar(List<Documento> documentos) {
        List<Documento> lista = documentos == null ? List.of() : documentos;
        Map<Requerido, Boolean> entregados = new EnumMap<>(Requerido.class);
        for (Requerido r : Requerido.values()) {
            entregados.put(r, false);
        }
        for (Documento documento : lista) {
            requeridoDe(documento.getNombre()).ifPresent(r -> entregados.put(r, true));
        }
        boolean todosCompletos = Arrays.stream(Requerido.values())
                .filter(Requerido::obligatorio)
                .allMatch(entregados::get);
        return new RevisionDocumentos(entregados, todosCompletos, lista.size());
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
