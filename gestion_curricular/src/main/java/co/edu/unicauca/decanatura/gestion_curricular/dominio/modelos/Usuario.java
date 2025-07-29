package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;


import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Usuario {

    private Integer id_usuario;
    private String nombre_completo;
    private Rol objRol;
    private String codigo;
    private String correo;
    private String password;
    private boolean estado_usuario;
    private Programa objPrograma;

    private List<Solicitud> solicitudes;

    private List<CursoOfertadoVerano> cursosOfertadosInscritos;

    public Usuario(){
        this.solicitudes = new ArrayList<Solicitud>();
        this.cursosOfertadosInscritos = new ArrayList<CursoOfertadoVerano>();
    }


    
}
