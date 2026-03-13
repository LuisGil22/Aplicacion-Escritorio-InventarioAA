package com.inventario.models;


import java.util.Objects;

/**
 * Representa una planta física del edificio donde se ubican los equipos de aire acondicionado.
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_PLANTAS del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Planta {

    private String planta;
    private String descripcion;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Planta() {}

    /**
     * Constructor que inicializa una planta con su nombre y descripción.
     *
     * @param planta      nombre de la planta (debe coincidir con los valores de PARAM_PLANTAS)
     * @param descripcion descripción opcional (puede ser null o vacía)
     */
    public Planta(String planta, String descripcion){
        this.planta = planta;
        this.descripcion = descripcion;
    }

    /** Getters y setters */
    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Equals y hashCode basados en el parametro Planta. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Planta planta1 = (Planta) o;
        return Objects.equals(planta, planta1.planta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planta);
    }

    @Override
    public String toString() {
        return "Planta{" +
                "planta='" + planta + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
