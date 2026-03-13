package com.inventario.models;

import java.util.Objects;

/**
 * Representa un cassette de aire acondicionado en el sistema de inventario.
 * <p>
 * Esta entidad se corresponde con la hoja Cassette del archivo Excel Inventario AA V2.xlsx.
 * Cada instancia almacena los datos técnicos, de ubicación y estado de un cassette,
 * incluyendo su relación con una condensadora y posibles averías.
 … *   <li>Columna H: MARCA/MODELO</li>
 *   <li>Columna I: NUM_SERIE_CAS</li>
 *   <li>Columna J: CONDENSADORA (clave foránea a hoja Condensadoras)</li>
 *   <li>Columna K: LOCALIZACIÓN_CONDENSADORA</li>
 *   <li>Columna L: GAS (R-410A, R-32, etc.)</li>
 *   <li>Columnas M-O: FECHAS (instalación, baja, revisión)</li>
 *   <li>Columna P: AVERIA (NUM_AVERIA si está averiado, ej. "0001")</li>
 *   <li>Columnas Q-R: FOTO y OBSERVACIONES</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class Cassette {
    /** Estados válidos según hoja PARAM_ESTADO */
    public static final String ESTADO_ACTIVA = "ACTIVA";
    public static final String ESTADO_BAJA = "BAJA";
    public static final String ESTADO_AVERIADO = "AVERIADO";

    /** Campos que corresponden a las columnas de la hoja Cassette */
    private String numCassette;
    private Integer numSecuencia;
    private String estado;
    private String planta;
    private String nombre;
    private Double potenciaCalor;
    private Double potenciaFrio;
    private String marcaModelo;
    private String numSerieCas;
    private String condensadora;
    private String localizacionCondensadora;
    private String gas;
    private String fechaInstalacion;
    private String fechaBaja;
    private String fechaRevision;
    private String averia;
    private String foto;
    private String observaciones;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Cassette() {}

    /**
     * Constructor completo para crear un nuevo cassette.
     *
     * @param numCassette               Identificador del cassette (no puede ser null/empty)
     * @param numSecuencia             Número de secuencia (≥ 1)
     * @param estado                   Estado actual (debe ser válido)
     * @param planta                   Planta de ubicación
     * @param nombre                   Nombre descriptivo
     * @param potenciaCalor            Potencia en modo calor (puede ser null)
     * @param potenciaFrio             Potencia en modo frío (puede ser null)
     * @param marcaModelo              Marca y modelo
     * @param numSerieCas              Número de serie
     * @param condensadora             Condensadora asociada
     * @param localizacionCondensadora Ubicación de la condensadora
     * @param gas                      Tipo de gas refrigerante
     * @param fechaInstalacion         Fecha de instalación (dd/MM/yyyy)
     * @param fechaBaja                Fecha de baja (dd/MM/yyyy)
     * @param fechaRevision            Fecha de revisión (dd/MM/yyyy)
     * @param averia                   Número de averia (puede estar vacío)
     * @param foto                     Referencia a foto
     * @param observaciones            Observaciones adicionales
     */
    public Cassette(String numCassette, Integer numSecuencia, String estado, String planta, String nombre, Double potenciaCalor, Double potenciaFrio, String marcaModelo, String numSerieCas, String condensadora, String localizacionCondensadora, String gas, String fechaInstalacion,String fechaBaja, String fechaRevision, String averia, String foto, String observaciones) {
        this.numCassette = numCassette;
        this.numSecuencia = numSecuencia;
        this.estado = estado;
        this.planta = planta;
        this.nombre = nombre;
        this.potenciaCalor = potenciaCalor;
        this.potenciaFrio = potenciaFrio;
        this.marcaModelo = marcaModelo;
        this.numSerieCas = numSerieCas;
        this.condensadora = condensadora;
        this.localizacionCondensadora = localizacionCondensadora;
        this.gas = gas;
        this.fechaInstalacion = fechaInstalacion;
        this.fechaBaja = fechaBaja;
        this.fechaRevision= fechaRevision;
        this.averia = averia;
        this.foto = foto;
        this.observaciones = observaciones;
    }


    /** Getters y setters para cada campo */
    public String getNumCassette() {
        return numCassette;
    }

    public void setNumCassette(String numCassette) {
        this.numCassette = numCassette;
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

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPotenciaCalor() {
        return potenciaCalor;
    }

    public void setPotenciaCalor(Double potenciaCalor) {
        this.potenciaCalor = potenciaCalor;
    }

    public Double getPotenciaFrio() {
        return potenciaFrio;
    }

    public void setPotenciaFrio(Double potenciaFrio) {
        this.potenciaFrio = potenciaFrio;
    }

    public String getMarcaModelo() {
        return marcaModelo;
    }

    public void setMarcaModelo(String marcaModelo) {
        this.marcaModelo = marcaModelo;
    }

    public String getNumSerieCas() {
        return numSerieCas;
    }

    public void setNumSerieCas(String numSerieCas) {
        this.numSerieCas = numSerieCas;
    }

    public String getCondensadora() {
        return condensadora;
    }

    public void setCondensadora(String condensadora) {
        this.condensadora = condensadora;
    }

    public String getLocalizacionCondensadora() {
        return localizacionCondensadora;
    }

    public void setLocalizacionCondensadora(String localizacionCondensadora) {
        this.localizacionCondensadora = localizacionCondensadora;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(String fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(String fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getAveria() {
        return averia;
    }

    public void setAveria(String averia) {
        this.averia = averia;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /** Equals y hashCode basados en numCassette y numSecuencia, que juntos identifican un cassette único */
    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) return false;
        Cassette cassette = (Cassette) o;
        return numCassette.equals(cassette.numCassette) && numSecuencia.equals(cassette.numSecuencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numCassette, numSecuencia);
    }

    @Override
    public String toString() {
        return "Cassette{" +
                "numCassette='" + numCassette + '\'' +
                ", numSecuencia=" + numSecuencia +
                ", estado='" + estado + '\'' +
                '}';
    }

}
