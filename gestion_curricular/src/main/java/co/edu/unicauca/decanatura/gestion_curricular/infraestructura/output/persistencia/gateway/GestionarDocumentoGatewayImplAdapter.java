package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocumentoRepositoryInt;

@Service
@Transactional
public class GestionarDocumentoGatewayImplAdapter implements GestionarDocumentosGatewayIntPort {


    private final DocumentoRepositoryInt documentoRepository;
    private final ModelMapper documentoMapper;

    public GestionarDocumentoGatewayImplAdapter(DocumentoRepositoryInt documentoRepository, ModelMapper documentoMapper){
        this.documentoRepository = documentoRepository;
        this.documentoMapper = documentoMapper;
    }

    @Override
    @Transactional
    public Documento crearDocumento(Documento documento) {
        DocumentoEntity documentoEntity = documentoMapper.map(documento, DocumentoEntity.class);
        DocumentoEntity saved = documentoRepository.save(documentoEntity);
        return documentoMapper.map(saved, Documento.class);
    }

    @Override
    @Transactional
    public Documento actualizarDocumento(Documento documento) {
        DocumentoEntity existente = documentoRepository.findById(documento.getId_documento())
            .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + documento.getId_documento()));

        // Actualizar solo campos editables; no sobrescribir objSolicitud cuando viene null en el dominio
        // (p. ej. tras buscarDocumentoId sin cargar la relación), para no perder la asociación.
        existente.setNombre(documento.getNombre());
        existente.setRuta_documento(documento.getRuta_documento());
        existente.setFecha_documento(documento.getFecha_documento());
        existente.setEsValido(documento.isEsValido());
        existente.setComentario(documento.getComentario());
        if (documento.getObjSolicitud() != null) {
            existente.setObjSolicitud(documentoMapper.map(documento.getObjSolicitud(), SolicitudEntity.class));
        }
        // si documento.getObjSolicitud() == null, se mantiene el valor actual de existente (no se desasocia)

        DocumentoEntity actualizado = documentoRepository.save(existente);
        return documentoMapper.map(actualizado, Documento.class);
    }
    @Override
    @Transactional(readOnly = true)
    public Documento buscarDocumentoId(Integer idDocumento) {
        return documentoRepository.findById(idDocumento)
            .map(entity -> documentoMapper.map(entity, Documento.class))
            .orElse(null);
    }

    @Override
    @Transactional
    public boolean eliminarDocumento(Documento documento) {
        Optional<DocumentoEntity> entityOpt = documentoRepository.findById(documento.getId_documento());
        if (entityOpt.isPresent()) {
            documentoRepository.deleteById(documento.getId_documento());
            return true;
        }
        return false;
    }

    @Override
    public List<Documento> buscarDocumentosSinSolicitud() {
       return documentoRepository.findByobjSolicitudIsNull()
            .stream()
            .map(entity -> documentoMapper.map(entity, Documento.class))
            .toList();
    }

    @Override
    public void añadirComentario(Documento documento, String comentario) {
        
        
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Documento> buscarDocumentoPorNombre(String nombre) {
        return documentoRepository.findByNombre(nombre)
            .map(entity -> documentoMapper.map(entity, Documento.class));
    }

    
    
}
