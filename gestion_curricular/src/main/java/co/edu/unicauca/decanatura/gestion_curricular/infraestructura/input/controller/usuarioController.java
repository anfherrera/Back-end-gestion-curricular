package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.usuarioEntradaDto;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.usuarioSalidaDto;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;

@RestController
@RequestMapping("/usuarios")
public class usuarioController {
    
    private final GestionarUsuarioCUIntPort usuarioCU;
    private final Mapper mapper;

    public usuarioController(GestionarUsuarioCUIntPort usuarioCU, Mapper mapper) {
        this.usuarioCU = usuarioCU;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<usuarioSalidaDto> crearUsuario(@RequestBody usuarioEntradaDto dto) {
        Usuario usuarioDominio = mapper.usuarioEntradaDtoAUsuario(dto);
        Usuario usuarioCreado = usuarioCU.crearUsuario(usuarioDominio);
        usuarioSalidaDto respuesta = mapper.usuarioAUsuarioSalidaDto(usuarioCreado);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<usuarioSalidaDto>> obtenerUsuarios() {
        List<Usuario> usuarios = usuarioCU.listarUsuarios();
        List<usuarioSalidaDto> respuesta = usuarios.stream()
                .map(mapper::usuarioAUsuarioSalidaDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

}
