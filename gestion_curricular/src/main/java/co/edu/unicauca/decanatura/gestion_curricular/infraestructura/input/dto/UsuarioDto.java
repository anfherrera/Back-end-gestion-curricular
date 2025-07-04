package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto;

import java.util.HashSet;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public class UsuarioDto {
    private Integer id_usuario;
    private String nombre_completo;
    private Rol objRol;
    private String codigo;
    private String correo;
    private String password;
    private boolean estado_usuario;
    private Programa objPrograma;

    private Set<solicitudDto> solicitudes;

    //private Set<CursoOfertadoVerano> cursosOfertadosInscritos;

    public UsuarioDto(){
        this.solicitudes = new HashSet<solicitudDto>();
        //this.cursosOfertadosInscritos = new HashSet<CursoOfertadoVerano>();
    }

}
