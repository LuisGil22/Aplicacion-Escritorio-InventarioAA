package com.inventario.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Representa una revisión programada de un equipo (Cassette o Condensadora) en el sistema de inventario.
 * <p>
 * Las revisiones se generan automáticamente 4 meses después de la fecha de instalación del equipo.
 * El estado inicial de la revisión es NO y cambia a SI cuando se marca como completada.
 * </p>
 *
 * @author Luis Gil
 */
public class Revision {
    private String numRevision;
    private String equipo;
    private String codigo;
    private String estado;
    private String planta;
    private String localizacion;
    private String fechaRevision;
    private String revision;
    private String observaciones;
    private String enviarMail;
    private long diasRestantes;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Revision() {
    }

    /**
     * Constructor completo para inicializar todos los campos de una revisión.
     *
     * @param numRevision número único de revisión con formato 0000
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo (NUM_CASSETTE o CONDENSADORA)
     * @param estado estado actual del equipo
     * @param planta planta del equipo (solo para Cassette, vacío para Condensadora)
     * @param localizacion ubicación física del equipo
     * @param fechaRevision fecha programada de revisión (instalación + 4 meses)
     * @param revision estado de la revisión ("NO" o "SI")
     * @param observaciones notas adicionales
     * @param enviarMail estado de envío de correo ("ENVIADO" o "NO ENVIADO")
     */
    public Revision(String numRevision, String equipo, String codigo, String estado, String planta, String localizacion, String fechaRevision, String revision, String observaciones, String enviarMail) {
        this.numRevision = numRevision;
        this.equipo = equipo;
        this.codigo = codigo;
        this.estado = estado;
        this.planta = planta;
        this.localizacion = localizacion;
        this.fechaRevision = fechaRevision;
        this.revision = revision;
        this.observaciones = observaciones;
        this.enviarMail = enviarMail != null ? enviarMail : "NO ENVIADO";
        this.diasRestantes = -999;
        calcularDiasRestantesRevision();
    }

    /** Getters y Setters */
    public String getNumRevision() {
        return numRevision;
    }

    public void setNumRevision(String numRevision) {
        this.numRevision = numRevision;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
        calcularDiasRestantesRevision();
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEnviarMail() {
        return enviarMail;
    }

    public void setEnviarMail(String enviarMail) {
        this.enviarMail = enviarMail;
    }

    /**
     * Metodo para obtener una representación legible de los días restantes para la revisión.
     */
    public String getDiasRestantes(){
        if(diasRestantes == -999){
            return "";
        }
        if(diasRestantes == 0){
            return "HOY";
        }else if (diasRestantes == 1){
            return (diasRestantes + " Dia");
        }else if(diasRestantes > 1){
            return (diasRestantes + " Dias");
        }else{
            return ("REVISION CADUCADA");
        }
    }


    /** Equals, HashCode y ToString */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Revision revision = (Revision) o;
        return Objects.equals(numRevision, revision.numRevision);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numRevision);
    }

    @Override
    public String toString() {
        return "Revision{" +
                "numRevision='" + numRevision + '\'' +
                ", equipo='" + equipo + '\'' +
                ", codigo='" + codigo + '\'' +
                ", estado='" + estado + '\'' +
                ", planta='" + planta + '\'' +
                ", localizacion='" + localizacion + '\'' +
                ", fechaRevision='" + fechaRevision + '\'' +
                ", revision='" + revision + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }

    /**
     * Calcula los días restantes para la revisión basándose en la fecha de revisión programada.
     * Si la revisión ya se ha completado (revision = "SI"), se establece diasRestantes a -999 para indicar que no es relevante.
     * Si la fecha de revisión no es válida o no se puede parsear, también se establece diasRestantes a -999.
     */
    public void calcularDiasRestantesRevision(){
        if("SI".equals(this.revision)){
            this.diasRestantes = -999;
            return;
        }
        try{
            if(this.fechaRevision == null || this.fechaRevision.trim().isEmpty()){
                this.diasRestantes = -999;
                return;
            }

            String [] partesFecha = this.fechaRevision.split("\n");
            if(partesFecha.length < 2){
                this.diasRestantes = -999;
                return;
            }

            String hastaParte = partesFecha[1].trim();
            if(!hastaParte.startsWith("hasta: ")){
                this.diasRestantes = -999;
                return;
            }

            String fechaStr = hastaParte.substring("hasta: ".length()).trim();
            LocalDate fechaHasta = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            this.diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaHasta);
        } catch (Exception e) {
            this.diasRestantes = -999;
        }
    }
}
