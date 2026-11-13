package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarDocumentosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;

public class GenerarDocumentosCUAdapter implements GenerarDocumentosCUIntPort {
    private final GestionarSolicitudHomologacionCUIntPort homologacion;
    private final GestionarSolicitudReingresoCUIntPort reingreso;
    private final GestionarSolicitudPazYSalvoCUIntPort paz;
    private final GenerarDocumentosIntPort generador;

    public GenerarDocumentosCUAdapter(GestionarSolicitudHomologacionCUIntPort homologacion,
            GestionarSolicitudReingresoCUIntPort reingreso, GestionarSolicitudPazYSalvoCUIntPort paz,
            GenerarDocumentosIntPort generador) {
        this.homologacion = homologacion;
        this.reingreso = reingreso;
        this.paz = paz;
        this.generador = generador;
    }

    @Override public byte[] generarDocumento(int id, String tipo, Map<String, Object> documento,
            Map<String, Object> solicitud) throws IOException {
        SolicitudHomologacion hom = null;
        SolicitudReingreso rein = null;
        // Se conserva el respaldo al mapa si la consulta no encuentra la solicitud o falla.
        if (id > 0) {
            try {
                if ("OFICIO_HOMOLOGACION".equals(tipo)) hom = homologacion.buscarPorId(id);
                else if ("RESOLUCION_REINGRESO".equals(tipo)) rein = reingreso.obtenerSolicitudReingresoPorId(id);
            } catch (Exception ignorada) {
                // El generador conserva los valores alternativos del mapa.
            }
        }
        return generador.generarDocumento(new DatosGeneracionDocumento(id, tipo, documento, solicitud, hom, rein));
    }

    @Override public List<PlantillaDocumento> getTemplates(String proceso) {
        return generador.getTemplates(proceso);
    }

    @Override public Optional<DocumentoGenerado> generarPazYSalvo(Integer id, String cedula, String titulo, String director,
            boolean porDefecto, boolean pdf, String numero, String fecha, String observaciones) throws IOException {
        var datos = paz.prepararDocumento(id, cedula, titulo, director, porDefecto);
        if (datos.isEmpty()) return Optional.empty();
        String nombreArchivo;
        if (porDefecto) {
            numero = "PYS-" + id;
            fecha = LocalDate.now().toString();
            observaciones = "Paz y Salvo generado";
            nombreArchivo = "PazYSalvo_" + id + (pdf ? ".pdf" : ".docx");
        } else {
            String nombre = datos.get().nombreArchivo().replaceAll("[^a-zA-Z0-9]", "_");
            nombreArchivo = String.format("PAZ_SALVO_%s_%s.docx", nombre, numero);
            fecha = fechaDocumentoNormalizada(fecha);
            observaciones = observaciones == null ? "" : observaciones;
        }
        return Optional.of(new DocumentoGenerado(nombreArchivo,
                generador.generarPazYSalvo(numero, fecha, observaciones, datos.get(), pdf)));
    }

    private static String fechaDocumentoNormalizada(String fechaDocumento) {
        if (fechaDocumento != null && !fechaDocumento.trim().isEmpty()) {
            String fecha = fechaDocumento.trim();
            if (fecha.contains("T")) fecha = fecha.substring(0, 10);
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) return fecha;
        }
        return LocalDate.now().toString();
    }
}
