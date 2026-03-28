package com.inventario.models;

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
     */
    public Revision(String numRevision, String equipo, String codigo, String estado, String planta, String localizacion, String fechaRevision, String revision, String observaciones) {
        this.numRevision = numRevision;
        this.equipo = equipo;
        this.codigo = codigo;
        this.estado = estado;
        this.planta = planta;
        this.localizacion = localizacion;
        this.fechaRevision = fechaRevision;
        this.revision = revision;
        this.observaciones = observaciones;
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
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
}
