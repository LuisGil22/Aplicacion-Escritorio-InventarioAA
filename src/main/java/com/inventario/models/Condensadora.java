package com.inventario.models;


import java.util.Objects;

/**
 * Representa una condensadora de aire acondicionado en el sistema de inventario.
 * <p>
 * Esta entidad se corresponde con la hoja Condensadoras del archivo Excel Inventario AA V2.xlsx.
 * Cada instancia almacena los datos técnicos, de ubicación y estado de una condensadora,
 * incluyendo su relación con cassettes y posibles averías.
 … *   <li>Columna H: GAS (R-410A, R-32, etc.)</li>
 *   <li>Columnas I-K: FECHAS (instalación, baja, revisión)</li>
 *   <li>Columna L: AVERIA (NUM_AVERIA si está averiada, ej. "0001")</li>
 *   <li>Columna M: OBSERVACIONES</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class Condensadora {
    /** Estados válidos según hoja PARAM_ESTADO */
    public static final String ESTADO_ACTIVA = "ACTIVA";
    public static final String ESTADO_BAJA = "BAJA";
    public static final String ESTADO_AVERIADO = "AVERIADO";

    /** Campos de la Entidad */
    private String condensadora;
    private Integer numSecuencia;
    private String estado;
    private String marca;
    private String modelo;
    private Long numSerieCond;
    private String loc_condensadora;
    private String gas;
    private String fechaInstalacion;
    private String fechaBaja;
    private String fechaRevision;
    private String averia;
    private String observaciones;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Condensadora() {}

    /**
     * Constructor completo para crear una nueva condensadora.
     *
     * @param condensadora       Identificador único de la condensadora
     * @param numSecuencia      Número de secuencia (≥ 1)
     * @param estado            Estado actual (debe ser válido)
     * @param marca             Marca del equipo
     * @param modelo            Modelo del equipo
     * @param numSerieCond      Número de serie (puede ser null)
     * @param loc_condensadora  Ubicación física
     * @param gas               Tipo de gas refrigerante
     * @param fechaInstalacion  Fecha de instalación (dd/MM/yyyy)
     * @param fechaBaja         Fecha de baja (dd/MM/yyyy)
     * @param fechaRevision     Fecha de revisión (dd/MM/yyyy)
     * @param averia            Número de avería (puede estar vacío)
     * @param observaciones     Observaciones adicionales
     */
    public Condensadora(String condensadora, Integer numSecuencia,String estado, String marca, String modelo, Long numSerieCond, String loc_condensadora, String gas, String fechaInstalacion,String fechaBaja, String fechaRevision, String averia, String observaciones) {
        this.condensadora = condensadora;
        this.numSecuencia = numSecuencia;
        this.estado = estado;
        this.marca = marca;
        this.modelo = modelo;
        this.numSerieCond = numSerieCond;
        this.loc_condensadora = loc_condensadora;
        this.gas = gas;
        this.fechaInstalacion = fechaInstalacion;
        this.fechaBaja = fechaBaja;
        this.fechaRevision = fechaRevision;
        this.averia = averia;
        this.observaciones = observaciones;
    }

    /** Getters y setters */
    public String getCondensadora() {
        return condensadora;
    }

    public void setCondensadora(String condensadora) {
        this.condensadora = condensadora;
    }

    public Integer getNumSecuencia() {
        return numSecuencia;
    }

    public void setNumSecuencia(Integer numSecuencia) {
        this.numSecuencia =  (numSecuencia != null && numSecuencia > 0) ? numSecuencia : 1;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!ESTADO_ACTIVA.equals(estado) &&
                !ESTADO_BAJA.equals(estado) &&
                !ESTADO_AVERIADO.equals(estado)) {
            throw new IllegalArgumentException("El estado debe ser 'ACTIVA', 'BAJA' o 'AVERIADO'");
        }
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getAveria() {
        return averia;
    }

    public void setAveria(String averia) {
        this.averia = averia;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(String fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getLoc_condensadora() {
        return loc_condensadora;
    }

    public void setLoc_condensadora(String loc_condensadora) {
        this.loc_condensadora = loc_condensadora;
    }

    public Long getNumSerieCond() {
        return numSerieCond;
    }

    public void setNumSerieCond(Long numSerieCond) {
        this.numSerieCond = numSerieCond;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(String fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    /** Equals y hashCode basados en condensadora y numSecuencia, juntos identifican una condensadora única */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condensadora that = (Condensadora) o;
        return Objects.equals(condensadora, that.condensadora) &&
                Objects.equals(numSecuencia, that.numSecuencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condensadora, numSecuencia);
    }

    @Override
    public String toString() {
        return "Condensadora{" +
                "condensadora='" + condensadora + '\'' +
                ", numSecuencia=" + numSecuencia +
                ", estado='" + estado + '\'' +
                '}';
    }
}
