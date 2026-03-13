package com.inventario.models;


import java.util.Objects;

/**
 * Representa un modelo de condensadora de aire acondicionado en el sistema de inventario.
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_MODELOS_COND del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Model_Condensadora {

    private String modelo;
    private String descripcion;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Model_Condensadora() {}

    /**
     * Constructor que inicializa un modelo de condensadora con su código y descripción.
     *
     * @param modelo   código del modelo (debe coincidir con los valores de PARAM_MODELOS_CAS)
     * @param descripcion descripción opcional (puede ser null o vacía)
     */
    public Model_Condensadora(String modelo, String descripcion){
        this.modelo = modelo;
        this.descripcion = descripcion;
    }

    /** Getters y setters */
    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Equals y hashCode basados en el parametro Modelos_Cond. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model_Condensadora that = (Model_Condensadora) o;
        return Objects.equals(modelo, that.modelo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelo);
    }

    @Override
    public String toString() {
        return "ModeloCassette{" +
                "modeloCas='" + modelo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
