import org.apache.poi.xwpf.usermodel.*;
import java.io.FileOutputStream;
import java.io.IOException;

public class CrearPlantillaWord {
    public static void main(String[] args) {
        try {
            // Crear nuevo documento Word
            XWPFDocument document = new XWPFDocument();
            
            // Crear párrafos con el contenido
            crearParrafo(document, "UNIVERSIDAD DEL CAUCA", true, true, 14);
            crearParrafo(document, "FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES", true, true, 12);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "RESOLUCIÓN DE HOMOLOGACIÓN", true, true, 12);
            crearParrafo(document, "No. [NUMERO_DOCUMENTO]", false, true, 11);
            crearParrafo(document, "Fecha: [FECHA_DOCUMENTO]", false, true, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "Por la cual se resuelve la solicitud de homologación de asignaturas", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "EL DECANO DE LA FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES DE LA UNIVERSIDAD DEL CAUCA", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "En uso de sus facultades legales y reglamentarias, y", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "CONSIDERANDO:", false, true, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "1. Que el estudiante [NOMBRE_ESTUDIANTE] identificado con cédula de ciudadanía N° [CEDULA_ESTUDIANTE] y código estudiantil N° [CODIGO_ESTUDIANTE], realizó solicitud de homologación de asignaturas, con fecha [FECHA_SOLICITUD], para el Programa de [PROGRAMA], el cual es administrado por la Facultad de Ingeniería Electrónica y Telecomunicaciones.", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "2. Que la historia académica del estudiante [NOMBRE_ESTUDIANTE] fue revisada en virtud de lo estipulado en el Acuerdo 004 del 5 de marzo de 2003, y el Coordinador del Programa en fecha [FECHA_CONCEPTO], conceptúo que el (la) estudiante NO ha perdido el derecho a continuar estudios en el programa de [PROGRAMA].", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "3. Que el Consejo de Facultad de Ingeniería Electrónica y Telecomunicaciones, en sesión del [FECHA_SESION_CONSEJO], Acta No. [NUMERO_ACTA_CONSEJO], aprobó la homologación de las asignaturas relacionadas en el Artículo Primero de la presente Resolución.", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "En mérito de lo expuesto,", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "RESUELVE:", false, true, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "ARTÍCULO PRIMERO. - Homologar al estudiante [NOMBRE_ESTUDIANTE] identificado con cédula de ciudadanía N° [CEDULA_ESTUDIANTE] y código estudiantil N° [CODIGO_ESTUDIANTE], las siguientes asignaturas:", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            
            // Crear tabla
            crearTabla(document);
            
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "ARTÍCULO SEGUNDO. - Notificar personalmente o mediante correo electrónico al estudiante del contenido de la presente resolución, advirtiéndole que contra el presente Acto Administrativo procede el recurso de reposición ante el Consejo de Facultad de Ingeniería Electrónica y Telecomunicaciones, el cual deberá ser interpuesto en la diligencia de notificación o dentro de los diez (10) días hábiles siguientes a la notificación.", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "ARTÍCULO TERCERO. - Enviar la presente Resolución a la Coordinación del Programa y a la División de Admisiones, Registro y Control Académico - DARCA, para que sea registrada en la historia académica del (la) estudiante.", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "Para constancia se firma en Popayán, a los [DIA_FIRMA] ([DIA_NUMERO]) días del mes de [MES_FIRMA] del año [AÑO_FIRMA].", false, false, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "COMUNÍQUESE, NOTIFÍQUESE Y CÚMPLASE", false, true, 11);
            crearParrafo(document, "", false, false, 11);
            crearParrafo(document, "ALEJANDRO TOLEDO TOVAR", false, false, 11);
            crearParrafo(document, "Presidente Consejo de Facultad", false, false, 11);
            
            // Guardar documento
            FileOutputStream out = new FileOutputStream("src/main/resources/templates/oficio-homologacion.docx");
            document.write(out);
            out.close();
            document.close();
            
            System.out.println("Plantilla Word creada exitosamente!");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void crearParrafo(XWPFDocument document, String texto, boolean centrado, boolean negrita, int tamaño) {
        XWPFParagraph paragraph = document.createParagraph();
        if (centrado) {
            paragraph.setAlignment(ParagraphAlignment.CENTER);
        }
        
        XWPFRun run = paragraph.createRun();
        run.setText(texto);
        run.setBold(negrita);
        run.setFontSize(tamaño);
        run.setFontFamily("Arial");
    }
    
    private static void crearTabla(XWPFDocument document) {
        XWPFTable table = document.createTable(4, 4);
        
        // Encabezados
        table.getRow(0).getCell(0).setText("ASIGNATURA CURSADA");
        table.getRow(0).getCell(1).setText("CÓDIGO Y PLAN INGENIERÍA EN AUTOMATICA INDUSTRIAL");
        table.getRow(0).getCell(2).setText("Asignatura a homologar en el [PERIODO_ACADEMICO]");
        table.getRow(0).getCell(3).setText("NOTA");
        
        // Filas de datos
        table.getRow(1).getCell(0).setText("---- (ASIGNATURA 1 A DEFINIR)");
        table.getRow(1).getCell(1).setText("---- (CÓDIGO 1 A DEFINIR)");
        table.getRow(1).getCell(2).setText("---- (ASIGNATURA HOMOLOGAR 1 A DEFINIR)");
        table.getRow(1).getCell(3).setText("---- (NOTA 1 A DEFINIR)");
        
        table.getRow(2).getCell(0).setText("---- (ASIGNATURA 2 A DEFINIR)");
        table.getRow(2).getCell(1).setText("---- (CÓDIGO 2 A DEFINIR)");
        table.getRow(2).getCell(2).setText("---- (ASIGNATURA HOMOLOGAR 2 A DEFINIR)");
        table.getRow(2).getCell(3).setText("---- (NOTA 2 A DEFINIR)");
        
        table.getRow(3).getCell(0).setText("---- (ASIGNATURA 3 A DEFINIR)");
        table.getRow(3).getCell(1).setText("---- (CÓDIGO 3 A DEFINIR)");
        table.getRow(3).getCell(2).setText("---- (ASIGNATURA HOMOLOGAR 3 A DEFINIR)");
        table.getRow(3).getCell(3).setText("---- (NOTA 3 A DEFINIR)");
    }
}

