package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesCursosVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarDocumentosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarExcelSolicitudesVeranoIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarPdfCursosVeranoIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarReportesEstadisticasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoCursoOfertadoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarNotificacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GenerarDocumentosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GenerarReportesCursosVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GenerarReportesEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarArchivosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocentesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocumentosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarMateriasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarNotificacionCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarProgramasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarRolesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSalonesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCursoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudEcaesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudHomologacionCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudPazYSalvoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudReingresoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarUsuarioCUAdapter;

@Configuration
public class BeanConfiguration {
    @Bean
    public GenerarReportesCursosVeranoCUIntPort crearGenerarReportesCursosVerano(
            GestionarCursoOfertadoVeranoCUIntPort cursos,
            GestionarSolicitudCursoVeranoCUIntPort solicitudes,
            GenerarPdfCursosVeranoIntPort pdf,
            GenerarExcelSolicitudesVeranoIntPort excel) {
        return new GenerarReportesCursosVeranoCUAdapter(cursos, solicitudes, pdf, excel);
    }
    @Bean
    public GenerarDocumentosCUIntPort crearGenerarDocumentos(
            GestionarSolicitudHomologacionCUIntPort homologacion,
            GestionarSolicitudReingresoCUIntPort reingreso,
            GestionarSolicitudPazYSalvoCUIntPort paz,
            GenerarDocumentosIntPort generador) {
        return new GenerarDocumentosCUAdapter(homologacion, reingreso, paz, generador);
    }
    @Bean
    public GenerarReportesEstadisticasCUIntPort crearGenerarReportesEstadisticas(
            GestionarEstadisticasCUIntPort estadisticas,
            GenerarReportesEstadisticasIntPort generador) {
        return new GenerarReportesEstadisticasCUAdapter(estadisticas, generador);
    }

    @Bean
    public GestionarCursoOfertadoVeranoCUAdapter crearGestionarCursoOfertadoVeranoCUInt(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarMateriasIntPort objGestionarMateriasGateway,
            GestionarDocenteGatewayIntPort objGestionarDocenteGateway,
            GestionarSalonesIntPort salonesGateway,
            GestionarEstadoCursoOfertadoGatewayIntPort objGestionarEstadoCursoOfertadoGateway,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU){
                return new GestionarCursoOfertadoVeranoCUAdapter(objGestionarCursoOfertadoVeranoGateway, objGestionarSolicitudGateway, objGestionarUsuarioGateway,
                 objGestionarMateriasGateway, objGestionarDocenteGateway, salonesGateway, objGestionarNotificacionCU);
            }

    // GestionarEstadisticasGatewayImplAdapter ya está definido como @Service, no necesita definición adicional
    
    @Bean
    public GestionarEstadisticasCUAdapter crearGestionarEstadisticasCUInt(
            GestionarEstadisticasGatewayIntPort estadisticasGateway) {
                return new GestionarEstadisticasCUAdapter(estadisticasGateway);
    }
    
    @Bean 
    public GestionarMateriasCUAdapter crearGestionarMateriasCUInt(GestionarMateriasIntPort materiasGateway){
                 return new GestionarMateriasCUAdapter(materiasGateway);
    }

    @Bean
    public GestionarSalonesCUAdapter crearGestionarSalonesCUInt(GestionarSalonesIntPort salonesGateway){
                 return new GestionarSalonesCUAdapter(salonesGateway);
    }
    
    
    @Bean
    public GestionarSolicitudCUAdapter GestionarSolicitudCUInt (GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway,
    GestionarUsuarioGatewayIntPort objUsuario) {
        return new GestionarSolicitudCUAdapter(objGestionarSolicitudGateway, objUsuario);
    }

    @Bean 
    public GestionarUsuarioCUAdapter crearGestionarUsuarioCUInt(GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
                                    GestionarProgramaGatewayIntPort objGestionarProgramaGateway,
                                    GestionarRolGatewayIntPort objGestionarRolGateway,
                                     PasswordEncoder passwordEncoder) {
        return new GestionarUsuarioCUAdapter(objGestionarUsuarioGateway, objGestionarProgramaGateway, objGestionarRolGateway, passwordEncoder::encode);
    }

    @Bean
    public GestionarSolicitudCursoVeranoCUAdapter crearGestionarSolicitudCursoVeranoCUInt(
        GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway,
        GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,
        GestionarUsuarioGatewayIntPort objUsuario, 
        GestionarDocumentosGatewayIntPort objDocumentosGateway,
        GestionarArchivosCUIntPort objGestionarArchivos,
        GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        return new GestionarSolicitudCursoVeranoCUAdapter(objGestionarSolicitudGateway, objCursoOfertado, objUsuario, objDocumentosGateway, objGestionarArchivos, objGestionarNotificacionCU);
    }

    @Bean
    public GestionarSolicitudPazYSalvoCUAdapter crearGestionarSolicitudPazYSalvoCUInt(
            GestionarSolicitudPazYSalvoGatewayIntPort objGestionarSolicitudGateway,
            GestionarUsuarioGatewayIntPort objUsuario, 
            GestionarDocumentosGatewayIntPort objDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            GestionarArchivosCUIntPort objGestionarArchivos,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        return new GestionarSolicitudPazYSalvoCUAdapter(
                objGestionarSolicitudGateway,
                objUsuario,
                objDocumentosGateway,
                objGestionarEstadoSolicitudGateway,
                objGestionarArchivos,
                objGestionarNotificacionCU
        );
    }


    @Bean
    public GestionarSolicitudEcaesCUAdapter crearGestionarSolicitudEcaesCUInt(
            GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway,
            GestionarDocumentosGatewayIntPort objDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU,
            GestionarArchivosCUIntPort gestionarArchivos
            ) {
        return new GestionarSolicitudEcaesCUAdapter(objGestionarSolicitudEcaesGateway,objDocumentosGateway,objGestionarEstadoSolicitudGateway,objGestionarUsuarioGateway,objGestionarNotificacionCU,gestionarArchivos);
    }

    @Bean
    public GestionarSolicitudHomologacionCUAdapter crearGestionarSolicitudHomologacionCUInt(
            GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway
    ,GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
    GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
    GestionarNotificacionCUIntPort objGestionarNotificacionCU,
    GestionarArchivosCUIntPort gestionarArchivos
    ) {
        return new GestionarSolicitudHomologacionCUAdapter(gestionarSolicitudHomologacionGateway, gestionarUsuarioGateway, gestionarDocumentosGateway, gestionarEstadoSolicitudGateway, objGestionarNotificacionCU, gestionarArchivos);
    }

    @Bean
    public GestionarSolicitudReingresoCUAdapter crearGestionarSolicitudReingresoCUInt(
        GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
        GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
        GestionarSolicitudReingresoGatewayIntPort gestionarSolicitudReingresoGateway,
        GestionarNotificacionCUIntPort objGestionarNotificacionCU,
        GestionarArchivosCUIntPort gestionarArchivos
    ) {
        return new GestionarSolicitudReingresoCUAdapter(gestionarUsuarioGateway, gestionarDocumentosGateway, gestionarEstadoSolicitudGateway, gestionarSolicitudReingresoGateway, objGestionarNotificacionCU, gestionarArchivos);
    }

    @Bean
    public GestionarDocumentosCUAdapter crearGestionarDocumentosCUInt(GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway,
            GestionarArchivosCUIntPort objGestionarArchivos) {
        return new GestionarDocumentosCUAdapter(objGestionarDocumentosGateway, objGestionarArchivos);
    }

    @Bean    public GestionarArchivosCUAdapter crearGestionarArchivosCUInt(GestionarArchivosGatewayIntPort objGestionarArchivos){
        return new GestionarArchivosCUAdapter(objGestionarArchivos);
    }

    @Bean
    public GestionarDocentesCUAdapter crearGestionarDocentesCUInt(
            GestionarDocenteGatewayIntPort docentesGateway) {
        return new GestionarDocentesCUAdapter(docentesGateway);
    }

    @Bean
    public GestionarProgramasCUAdapter crearGestionarProgramasCUInt(
            GestionarProgramaGatewayIntPort programasGateway) {
        return new GestionarProgramasCUAdapter(programasGateway);
    }

    @Bean
    public GestionarRolesCUAdapter crearGestionarRolesCUInt(
            GestionarRolGatewayIntPort rolesGateway) {
        return new GestionarRolesCUAdapter(rolesGateway);
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
    // ServicioGeneracionDocumentos ya está definido como @Service, no necesita definición adicional
}
