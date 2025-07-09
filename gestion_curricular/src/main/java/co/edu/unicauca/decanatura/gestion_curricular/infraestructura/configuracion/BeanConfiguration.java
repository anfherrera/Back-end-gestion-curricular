package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarMateriasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarUsuarioCUAdapter;

@Configuration
public class BeanConfiguration {

    @Bean
    public GestionarCursoOfertadoVeranoCUAdapter crearGestionarCursoOfertadoVeranoCUInt(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            FormateadorResultadosIntPort objFormateadorResultados){
                return new GestionarCursoOfertadoVeranoCUAdapter(objGestionarCursoOfertadoVeranoGateway, objGestionarSolicitudGateway, objGestionarUsuarioGateway, objFormateadorResultados);
            }

    @Bean
    public GestionarEstadisticasCUAdapter crearGestionarEstadisticasCUInt(
            GestionarEstadisticasGatewayIntPort estadisticasGateway,
            FormateadorResultadosIntPort formateadorResultados) {
                return new GestionarEstadisticasCUAdapter(estadisticasGateway,formateadorResultados);
    }
    
    @Bean 
    public GestionarMateriasCUAdapter crearGestionarMateriasCUInt(GestionarMateriasIntPort materiasGateway,
                                      FormateadorResultadosIntPort formateadorResultados){
                 return new GestionarMateriasCUAdapter(materiasGateway, formateadorResultados);
    }
    
    
    @Bean
    public GestionarSolicitudCUAdapter GestionarSolicitudCUInt (GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway,
    GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,  GestionarUsuarioGatewayIntPort objUsuario, 
    GestionarDocumentosGatewayIntPort objDocumentosGateway,
    FormateadorResultadosIntPort objFormateadorResultados) {
        return new GestionarSolicitudCUAdapter(objGestionarSolicitudGateway, objCursoOfertado, objUsuario, objDocumentosGateway, objFormateadorResultados);
    }

    @Bean
    public GestionarUsuarioCUAdapter crearGestionarUsuarioCUInt(GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            FormateadorResultadosIntPort objFormateadorResultados) {
        return new GestionarUsuarioCUAdapter(objGestionarUsuarioGateway, objFormateadorResultados);
    }
    
}
