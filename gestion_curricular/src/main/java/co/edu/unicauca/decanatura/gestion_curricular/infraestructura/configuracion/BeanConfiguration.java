package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoCursoOfertadoGatewayIntPort;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarNotificacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarArchivosCUIAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocentesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocumentosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarMateriasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSalonesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarProgramasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarRolesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCursoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudEcaesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudHomologacionCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudPazYSalvoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudReingresoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarUsuarioCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarNotificacionCUAdapter;

@Configuration
public class BeanConfiguration {

    @Bean
    public GestionarCursoOfertadoVeranoCUAdapter crearGestionarCursoOfertadoVeranoCUInt(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarMateriasIntPort objGestionarMateriasGateway,
            GestionarDocenteGatewayIntPort objGestionarDocenteGateway,
            GestionarEstadoCursoOfertadoGatewayIntPort objGestionarEstadoCursoOfertadoGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU){
                return new GestionarCursoOfertadoVeranoCUAdapter(objGestionarCursoOfertadoVeranoGateway, objGestionarSolicitudGateway, objGestionarUsuarioGateway,
                 objGestionarMateriasGateway, objGestionarDocenteGateway, objFormateadorResultados, objGestionarNotificacionCU);
            }

    // GestionarEstadisticasGatewayImplAdapter ya está definido como @Service, no necesita definición adicional
    
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
    public GestionarSalonesCUAdapter crearGestionarSalonesCUInt(GestionarSalonesIntPort salonesGateway,
                                      FormateadorResultadosIntPort formateadorResultados){
                 return new GestionarSalonesCUAdapter(salonesGateway, formateadorResultados);
    }
    
    
    @Bean
    public GestionarSolicitudCUAdapter GestionarSolicitudCUInt (GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway,
    GestionarUsuarioGatewayIntPort objUsuario, 
    FormateadorResultadosIntPort objFormateadorResultados) {
        return new GestionarSolicitudCUAdapter(objGestionarSolicitudGateway, objUsuario, objFormateadorResultados);
    }

    @Bean 
    public GestionarUsuarioCUAdapter crearGestionarUsuarioCUInt(GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
                                    GestionarProgramaGatewayIntPort objGestionarProgramaGateway,
                                    GestionarRolGatewayIntPort objGestionarRolGateway,
                                     FormateadorResultadosIntPort objFormateadorResultados,
                                     PasswordEncoder passwordEncoder) {
        return new GestionarUsuarioCUAdapter(objGestionarUsuarioGateway, objGestionarProgramaGateway, objGestionarRolGateway, objFormateadorResultados, passwordEncoder);
    }

    @Bean
    public GestionarSolicitudCursoVeranoCUAdapter crearGestionarSolicitudCursoVeranoCUInt(
        GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway,
        GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,
        GestionarUsuarioGatewayIntPort objUsuario, 
        GestionarDocumentosGatewayIntPort objDocumentosGateway,
        FormateadorResultadosIntPort objFormateadorResultados,
        GestionarArchivosCUIntPort objGestionarArchivos,
        GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        return new GestionarSolicitudCursoVeranoCUAdapter(objGestionarSolicitudGateway, objCursoOfertado, objUsuario, objDocumentosGateway, objFormateadorResultados, objGestionarArchivos, objGestionarNotificacionCU);
    }

    @Bean
    public GestionarSolicitudPazYSalvoCUAdapter crearGestionarSolicitudPazYSalvoCUInt(
            GestionarSolicitudPazYSalvoGatewayIntPort objGestionarSolicitudGateway,
            GestionarUsuarioGatewayIntPort objUsuario, 
            GestionarDocumentosGatewayIntPort objDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarArchivosCUIntPort objGestionarArchivos,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        return new GestionarSolicitudPazYSalvoCUAdapter(
                objGestionarSolicitudGateway,
                objUsuario,
                objDocumentosGateway,
                objGestionarEstadoSolicitudGateway,
                objFormateadorResultados,
                objGestionarArchivos,
                objGestionarNotificacionCU
        );
    }


    @Bean
    public GestionarSolicitudEcaesCUAdapter crearGestionarSolicitudEcaesCUInt(
            GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway,FormateadorResultadosIntPort objFormateadorResultados
            ,GestionarDocumentosGatewayIntPort objDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU
            ) {
        return new GestionarSolicitudEcaesCUAdapter(objGestionarSolicitudEcaesGateway,objFormateadorResultados,objDocumentosGateway,objGestionarEstadoSolicitudGateway,objGestionarUsuarioGateway,objGestionarNotificacionCU);
    }

    @Bean
    public GestionarSolicitudHomologacionCUAdapter crearGestionarSolicitudHomologacionCUInt(
            FormateadorResultadosIntPort formateadorResultados, GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway
    ,GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
    GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
    GestionarNotificacionCUIntPort objGestionarNotificacionCU
    ) {
        return new GestionarSolicitudHomologacionCUAdapter(formateadorResultados, gestionarSolicitudHomologacionGateway, gestionarUsuarioGateway, gestionarDocumentosGateway, gestionarEstadoSolicitudGateway, objGestionarNotificacionCU);
    }

    @Bean
    public GestionarSolicitudReingresoCUAdapter crearGestionarSolicitudReingresoCUInt(
        GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
        GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
        FormateadorResultadosIntPort formateadorResultados,GestionarSolicitudReingresoGatewayIntPort gestionarSolicitudReingresoGateway,
        GestionarNotificacionCUIntPort objGestionarNotificacionCU
    ) {
        return new GestionarSolicitudReingresoCUAdapter(gestionarUsuarioGateway, gestionarDocumentosGateway, gestionarEstadoSolicitudGateway, formateadorResultados, gestionarSolicitudReingresoGateway, objGestionarNotificacionCU);
    }

    @Bean
    public GestionarDocumentosCUAdapter crearGestionarDocumentosCUInt(GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway,
            FormateadorResultadosIntPort objFormateadorResultados) {
        return new GestionarDocumentosCUAdapter(objGestionarDocumentosGateway, objFormateadorResultados);
    }

    @Bean    public GestionarArchivosCUIAdapter crearGestionarArchivosCUInt(GestionarArchivosGatewayIntPort objGestionarArchivos, FormateadorResultadosIntPort objFormateadorResultados){
        return new GestionarArchivosCUIAdapter(objGestionarArchivos, objFormateadorResultados);
    }

    @Bean
    public GestionarDocentesCUAdapter crearGestionarDocentesCUInt(
            GestionarDocenteGatewayIntPort docentesGateway,
            FormateadorResultadosIntPort formateadorResultados) {
        return new GestionarDocentesCUAdapter(docentesGateway, formateadorResultados);
    }

    @Bean
    public GestionarProgramasCUAdapter crearGestionarProgramasCUInt(
            GestionarProgramaGatewayIntPort programasGateway,
            FormateadorResultadosIntPort formateadorResultados) {
        return new GestionarProgramasCUAdapter(programasGateway, formateadorResultados);
    }

    @Bean
    public GestionarRolesCUAdapter crearGestionarRolesCUInt(
            GestionarRolGatewayIntPort rolesGateway,
            FormateadorResultadosIntPort formateadorResultados) {
        return new GestionarRolesCUAdapter(rolesGateway, formateadorResultados);
    }

    @Bean
    public GestionarNotificacionCUAdapter crearGestionarNotificacionCUInt(
            GestionarNotificacionGatewayIntPort objGestionarNotificacionGateway,
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarRolGatewayIntPort objGestionarRolGateway) {
        return new GestionarNotificacionCUAdapter(
                objGestionarNotificacionGateway,
                objGestionarUsuarioGateway,
                objGestionarRolGateway);
    }

    // GestionarEstadisticasGatewayImplAdapter ya está definido como @Service, no necesita definición adicional
    // GestionarNotificacionGatewayImplAdapter ya está definido como @Service, no necesita definición adicional

    // JwtUtil ya está definido como @Component, no necesita definición adicional
    // DocumentGeneratorService ya está definido como @Service, no necesita definición adicional
}
