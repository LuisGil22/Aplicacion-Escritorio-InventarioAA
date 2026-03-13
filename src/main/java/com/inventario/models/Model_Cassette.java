package com.inventario.models;


import java.util.Objects;

/**
 * Representa un modelo de cassette de aire acondicionado en el sistema de inventario.
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_MODELOS_CAS del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Model_Cassette {

    private String modeloCas;
    private String descripcion;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Model_Cassette() {}

    /**
     * Constructor que inicializa un modelo de cassette con su código y descripción.
     *
     * @param modeloCas   código del modelo (debe coincidir con los valores de PARAM_MODELOS_CAS)
     * @param descripcion descripción opcional (puede ser null o vacía)
     */
    public Model_Cassette(String modeloCas, String descripcion){
        this.modeloCas = modeloCas;
        this.descripcion = descripcion;
    }

    /** Getters y setters */
    public String getModeloCas() {
        return modeloCas;
    }

    public void setModeloCas(String modeloCas) {
        this.modeloCas = modeloCas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Equals y hashCode basados en el parametro Modelos_Cas. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model_Cassette that = (Model_Cassette) o;
        return Objects.equals(modeloCas, that.modeloCas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modeloCas);
    }

    @Override
    public String toString() {
        return "ModeloCassette{" +
                "modeloCas='" + modeloCas + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
