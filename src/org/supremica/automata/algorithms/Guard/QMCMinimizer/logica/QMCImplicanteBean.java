package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import java.util.ArrayList;

/**
 * Clase tipo bean que encapsula todas las propiedades de los implicantes
 * @author Pedro Sanz
 *
 */
public class QMCImplicanteBean implements Cloneable
{
    private String valorBin;
    private String terminos;
    private int [] posicionesTerminos;
    private int orden;
    private char nombre;
    boolean esencial;

     /**
     * Devuelve si es esencial
     * @return Devuelve  esencial.
     */
    public boolean isEsencial() {
        return esencial;
    }
    /**
     * Devuelve  el nombre del implicante
     * @return nombre
     */
    public char getNombre() {
        return nombre;
    }
    /**
     * Devuelve  el orden (adyacencia)
     * @return  orden
     */
    public int getOrden() {
        return orden;
    }
    /**
     * Devuelve  la cadena de terminos.
     * @return terminos
     */
    public String getTerminos() {
        return terminos;
    }
    /**
     * Devuelve la cadena correspondiente al valor binario
     * @return valorBin.
     */
    public String getValorBin() {
        return valorBin;
    }
    /**
     * Establece el termino como esencial
     * @param esencial
     */
    public void setEsencial(boolean esencial) {
        this.esencial = esencial;
    }
    /**
     * Introduce el nombre del implicante
     * @param nombre
     */
    public void setNombre(char nombre) {
        this.nombre = nombre;
    }
    /**
     * Introduce el orden del implicante (adyacencia)
     * @param orden
     */
    public void setOrden(int orden) {
        this.orden = orden;
    }
    /**
     * Introduce la cadena de terminos cubiertos por el implicante
     * @param terminos
     */
    public void setTerminos(String terminos) {
        this.terminos = terminos;
    }
    /**
     * Introduce la cadena representativa del valor binario del implicante
     * @param valorBin  valorBin a introducir.
     */
    public void setValorBin(String valorBin) {
        this.valorBin = valorBin;
    }
    /**
     * Devuelve las posiciones de los terminos relativas a la tabla de Implicantes esenciales
     * @return posicionesTerminos.
     */
    public int[] getPosicionesTerminos() {
        return posicionesTerminos;
    }
    /**
     * Introduce las posiciones de los terminos
     * @param posicionesTerminos
     */
    public void setPosicionesTerminos(int[] posicionesTerminos) {
        this.posicionesTerminos = posicionesTerminos;
    }

    /**
     * Método que marca todos los términos cubiertos por este objeto implicante
     * @param terminos
     * @param cubierto
     */
    public void marcaTerminosCubiertos(ArrayList terminos, boolean cubierto)
    {
        QMCBinarioBean terminoCubierto;
        int x = 0;
        for(int i=0; i<terminos.size() && x<posicionesTerminos.length;i++)
        {
            if(i==posicionesTerminos[x])
            {
                terminoCubierto = (QMCBinarioBean)terminos.get(i);
                terminoCubierto.setCubierta(cubierto);
                terminos.set(i,terminoCubierto);
                x++;
            }
        }
    }

    /**
     * Implementacion del metodo clone
     *
     * @return  una copia de la instancia de tipo QMCImplicanteBean
     */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // No deberia suceder ya que implementa la interfaz cloneable
            throw new InternalError();
        }
    }

}
