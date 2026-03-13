package com.inventario.models;


import java.util.Objects;

/**
 * Representa una ubicación descriptiva de un cassette en el edificio.
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_UBICACIONES_CASSETTES del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 * <p>
 * Esta clase se utiliza para llenar el campo "NOMBRE" en la hoja "Cassette".
 * </p>
 *
 * @author Luis Gil
 */
public class Ubicacion_Cassette {
    private String nombre;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Ubicacion_Cassette() {}

    /**
     * Constructor que inicializa la ubicación con un nombre descriptivo.
     *
     * @param nombre nombre de la ubicación (debe coincidir con los valores de PARAM_UBICACIONES_CASSETTES)
     */
    public Ubicacion_Cassette(String nombre) {
        this.nombre = nombre;
    }

    /** Getters y setters */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Equals y hashCode basados en el parametro Ubicacion_Cassette. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion_Cassette that = (Ubicacion_Cassette) o;
        return Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return "UbicacionCassette{" +
                "nombre='" + nombre + '\'' +
                '}';
    }
}
