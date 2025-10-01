# Script para crear plantilla Word real con codificación UTF-8 correcta
Add-Type -AssemblyName System.Windows.Forms

# Crear un nuevo documento Word
$word = New-Object -ComObject Word.Application
$word.Visible = $false

try {
    # Crear nuevo documento
    $doc = $word.Documents.Add()
    
    # Leer el archivo de texto con codificación UTF-8
    $content = Get-Content -Path "temp-homologacion.txt" -Encoding UTF8 -Raw
    
    # Insertar el contenido en el documento
    $doc.Content.Text = $content
    
    # Configurar formato básico
    $doc.Content.Font.Name = "Arial"
    $doc.Content.Font.Size = 11
    
    # Centrar el título principal
    $range = $doc.Range(0, 0)
    $range.Find.Execute("UNIVERSIDAD DEL CAUCA")
    if ($range.Find.Found) {
        $range.Select()
        $word.Selection.ParagraphFormat.Alignment = 1  # wdAlignParagraphCenter
        $word.Selection.Font.Bold = $true
        $word.Selection.Font.Size = 14
    }
    
    # Centrar el subtítulo
    $range = $doc.Range(0, 0)
    $range.Find.Execute("FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES")
    if ($range.Find.Found) {
        $range.Select()
        $word.Selection.ParagraphFormat.Alignment = 1  # wdAlignParagraphCenter
        $word.Selection.Font.Bold = $true
        $word.Selection.Font.Size = 12
    }
    
    # Centrar el título del documento
    $range = $doc.Range(0, 0)
    $range.Find.Execute("RESOLUCIÓN DE HOMOLOGACIÓN")
    if ($range.Find.Found) {
        $range.Select()
        $word.Selection.ParagraphFormat.Alignment = 1  # wdAlignParagraphCenter
        $word.Selection.Font.Bold = $true
        $word.Selection.Font.Size = 12
    }
    
    # Guardar como documento Word
    $outputPath = (Resolve-Path ".").Path + "\src\main\resources\templates\oficio-homologacion.docx"
    $doc.SaveAs2($outputPath, 16)  # wdFormatXMLDocument
    
    Write-Host "Plantilla Word creada exitosamente en: $outputPath"
    
} catch {
    Write-Error "Error al crear la plantilla Word: $($_.Exception.Message)"
} finally {
    # Cerrar Word
    if ($doc) { $doc.Close() }
    if ($word) { $word.Quit() }
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
}

# Limpiar archivo temporal
Remove-Item "temp-homologacion.txt" -Force

