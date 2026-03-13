package com.inventario.models;


import java.util.Objects;

/**
 * Representa una marca de equipos de aire acondicionado en el sistema de inventario.
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_MARCAS del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Marca {
    private String marca;
    private String descripcion;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Marca() {}

    /**
     * Constructor que inicializa una marca con su nombre y descripción.
     *
     * @param marca       nombre de la marca (debe coincidir con los valores de PARAM_MARCAS)
     * @param descripcion descripción opcional (puede ser null o vacía)
     */
    public Marca(String marca, String descripcion){
        this.marca = marca;
        this.descripcion = descripcion;
    }

    /** Getters y setters*/
    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Equals y hashCode basados en el parametro Marca. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Marca marca1 = (Marca) o;
        return Objects.equals(marca, marca1.marca);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marca);
    }

    @Override
    public String toString() {
        return "Marca{" +
                "marca='" + marca + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
