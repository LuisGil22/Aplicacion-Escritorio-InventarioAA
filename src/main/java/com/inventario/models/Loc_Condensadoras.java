package com.inventario.models;


import java.util.Objects;

/**
 * Representa una ubicación física de una condensadora en el edificio.
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_LOC_COND del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Loc_Condensadoras {
    private  String localizacionCondensadoras;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Loc_Condensadoras() {}

    /**
     * Constructor que inicializa la ubicación de la condensadora.
     *
     * @param localizacionCondensadoras ubicación física (debe coincidir con los valores de PARAM_LOC_COND)
     */
    public Loc_Condensadoras(String localizacionCondensadoras){
        this.localizacionCondensadoras = localizacionCondensadoras;
    }

    /** Getters y setters */
    public String getLocalizacionCondensadoras() {
        return localizacionCondensadoras;
    }

    public void setLocalizacionCondensadoras(String localizacionCondensadoras) {
        this.localizacionCondensadoras = localizacionCondensadoras;
    }

    /** Equals y hashCode basados en el parametro PARAM_LOC_COND. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loc_Condensadoras that = (Loc_Condensadoras) o;
        return Objects.equals(localizacionCondensadoras, that.localizacionCondensadoras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localizacionCondensadoras);
    }

    @Override
    public String toString() {
        return "LocalizacionCondensadora{" +
                "localizacionCondensadora='" + localizacionCondensadoras + '\'' +
                '}';
    }
}
