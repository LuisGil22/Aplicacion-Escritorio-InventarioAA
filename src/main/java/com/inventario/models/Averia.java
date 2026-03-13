package com.inventario.models;

import java.util.Objects;

/**
 * Representa una avería registrada en el sistema de inventario de aire acondicionado.
 * <p>
 * Esta entidad se corresponde con la hoja AVERIAS del archivo Excel Inventario AA V2.xlsx.
 * Cada instancia almacena los datos de una avería, incluyendo el equipo afectado,
 * su ubicación, fecha y destinatarios de notificación.
 … *   <li>Columna G: MAIL_ENVIADO (dos correos separados por salto de línea)</li>
 *   <li>Columna H: OBSERVACIONES</li>
 * </ul>
 *
 * @author Luis Gil
 */

public class Averia {

    /** Valores permitidos para {@link #equipoAveriado} */
    public static final String EQUIPO_CASSETTE = "CASSETTE";
    public static final String EQUIPO_CONDENSADORA = "CONDENSADORA";

    /** Campos de la entidad */
    private String numAveria;
    private String equipoAveriado;
    private String codigo;
    private String estado;
    private String planta;
    private String localizacion;
    private String fechaAveria;
    private String mail;
    private String observaciones;

    /**
     * Constructor completo para crear una nueva avería.
     *
     * @param numAveria       Número único de avería con formato de 4 dígitos (ej. "0001")
     * @param equipoAveriado  Tipo de equipo averiado ("CASSETTE" o "CONDENSADORA")
     * @param codigo          Código del equipo afectado
     * @param estado          Estado del equipo afectado
     * @param planta          Planta del equipo (solo relevante para cassettes, puede ser null o vacio para condensadoras)
     * @param localizacion    Ubicación física del equipo
     * @param fechaAveria     Fecha de la avería en formato dd/MM/yyyy
     * @param mail            Correos electrónicos de notificación (separados por \n)
     * @param observaciones   Observaciones adicionales (puede ser null o vacío)
     */
    public Averia(String numAveria, String equipoAveriado, String codigo,String estado, String planta, String localizacion, String fechaAveria, String mail, String observaciones) {
        this.numAveria = numAveria;
        this.equipoAveriado = equipoAveriado;
        this.codigo = codigo;
        this.estado = estado;
        this.planta = planta;
        this.localizacion = localizacion;
        this.fechaAveria = fechaAveria;
        this.mail = mail;
        this.observaciones = observaciones;
    }

    /** Getters y setters para cada campo con validación básica */
    public String getNumAveria() {
        return numAveria;
    }

    public void setNumAveria(String numAveria) {
        this.numAveria = numAveria;
    }

    public String getEquipoAveriado() {
        return equipoAveriado;
    }

    public void setEquipoAveriado(String equipoAveriado) {
        if (!EQUIPO_CASSETTE.equals(equipoAveriado) && !EQUIPO_CONDENSADORA.equals(equipoAveriado)) {
            throw new IllegalArgumentException("El equipo averiado debe ser 'CASSETTE' o 'CONDENSADORA'");
        }
        this.equipoAveriado = equipoAveriado;
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

    public String getFechaAveria() {
        return fechaAveria;
    }

    public void setFechaAveria(String fechaAveria) {
        this.fechaAveria = fechaAveria;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /** Sobrescritura de equals y hashCode para comparar averias por su numero único */
    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Averia averia = (Averia) o;
        return Objects.equals(numAveria, averia.numAveria);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numAveria);
    }

    @Override
    public String toString() {
        return "Averia{" +
                "numAveria='" + numAveria + '\'' +
                ", equipoAveriado='" + equipoAveriado + '\'' +
                ", codigo='" + codigo + '\'' +
                '}';
    }
}
