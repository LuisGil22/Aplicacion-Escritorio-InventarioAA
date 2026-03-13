package com.inventario.models;


import java.util.Objects;

/**
 * Representa un tipo de gas refrigerante utilizado en los equipos de aire acondicionado.
 * <p>
 * Esta entidad se corresponde con la hoja PARAM_GASES del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * <h2>Estructura de la hoja PARAM_GASES</h2>
 *   <ul>
 *     <li>Columna A: GAS (ej. "R-410A", "R-32")</li>
 *     <li>Columna B: PCG/PCA (valor numérico, ej. 2088)</li>
 *     <li>Columna C: FECHA_CADUCIDAD (no siempre presente)</li>
 *     <li>Columna D: FECHA_REVISION (no siempre presente)</li>
 *     <li>Columna E: OBSERVACIONES (notas adicionales)</li>
 *   </ul>
 *
 * @author Luis Gil
 */
public class Gas {
    private String gas;
    private Double pcg_pca;
    private String fechaCaducidad;
    private String fechaRevision;
    private String observaciones;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Gas() {}

    /**
     * Constructor completo para crear un nuevo registro de gas.
     *
     * @param gas               Tipo de gas refrigerante
     * @param pcg_pca            Valor PCG/PCA (puede ser null)
     * @param fechaCaducidad    Fecha estimada de caducidad (formato dd/MM/yyyy, puede estar vacía)
     * @param fechaRevision     Fecha de revisión normativa (formato dd/MM/yyyy, puede estar vacía)
     * @param observaciones     Observaciones adicionales (puede estar vacío)
     */
    public Gas(String gas, Double pcg_pca, String fechaCaducidad, String fechaRevision, String observaciones) {
        this.gas = gas;
        this.pcg_pca = pcg_pca;
        this.fechaCaducidad = fechaCaducidad;
        this.fechaRevision = fechaRevision;
        this.observaciones = observaciones;
    }

    /** Getters y setters */
    public String getGas(){
        return gas;
    }

    public void setGas(String gas){
        this.gas = gas;
    }

    public Double getPcg_pca() {
        return pcg_pca;
    }

    public void setPcg_pca(Double pcg_pca) {
        this.pcg_pca = pcg_pca;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /** Equals y hashCode basados en el parametro Gas. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gas gas1 = (Gas) o;
        return Objects.equals(gas, gas1.gas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gas);
    }

    @Override
    public String toString() {
        return "Gas{" +
                "gas='" + gas + '\'' +
                ", pcgPca=" + pcg_pca +
                '}';
    }
}
