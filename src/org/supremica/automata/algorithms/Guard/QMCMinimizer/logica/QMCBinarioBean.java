/**
 * 
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

/**
 * Clase tipo bean que encapsula los datos de las adyacencias 
 * @author Pedro Sanz
 *  Fecha 17/03/2006 
 *
 */
public class QMCBinarioBean implements Cloneable
{
    String valorDec;
    String coordenadasVacuas;
    String valorBin;    
    int indice;
    
    String posicion;
    boolean usada;
    boolean cubierta;
    boolean indiferencia;
    boolean termino;
    
    /**
     * Devuelve true si es termino
     * @return termino
     */
    public boolean isTermino() {
        return termino;
    }
    /**
     * Establece si el binario es termino.
     * @param termino
     */
    public void setTermino(boolean termino) {
        this.termino = termino;
    }
    /**
     * Devuelve true si la variables es usada
     * @return Devuelve variable usada.
     */
    public boolean isUsada() {
        return usada;
    }
    /**
     * Establece si se ha usado el termino o adyacencia que representa durante el proceso de minimización
     * @param usado 
     */
    public void setUsado(boolean usada) {
        this.usada = usada;
    }
    
    /**
     * Devuelve true si el termino o adyacencia esta cubierta por algun implicante
     * @return Devuelve variable cubierta.
     */
    public boolean isCubierta() {
        return cubierta;
    }
    /**
     * Establece si el binario esta cubierto por algun implicante
     * @param cubierta 
     */
    public void setCubierta(boolean cubierta) {
        this.cubierta = cubierta;
    }
    /**
     * Devuelve el valor binario
     * @return valorBin.
     */
    public String getValorBin() {
        return valorBin;
    }
    /**
     * Introduce el valor binario
     * @param valorBin
     */
    public void setValorBin(String valorBin) {
        this.valorBin = valorBin;
    }
    /**
     * Devuelve la cadena de coordenadas vacuas
     * @return coordenadasVacuas.
     */
    public String getCoordenadasVacuas() {
        return coordenadasVacuas;
    }
    /**
     * Introduce las coordenadas vacuas
     * @param coordenadasVacuas 
     */
    public void setCoordenadasVacuas(String coordenadasVacuas) {
        this.coordenadasVacuas = coordenadasVacuas;
    }
    /**
     * Devuelve el valor decimal
     * @return valorDec.
     */
    public String getValorDec() {
        return valorDec;
    }
    /**
     * Introduce el valor decimal
     * @param valorDec 
     */
    public void setValorDec(String valorDec) {
        this.valorDec = valorDec;
    }
    /**
     * Devuelve el indice (numero de unos)
     * @return indice.
     */
    public int getIndice() {
        return indice;
    }
    /**
     * Introduce el indice (numero de unos)
     * @param indice
     */
    public void setIndice(int indice) {
        this.indice = indice;
    }   
    
    /**
     * Implementación del método toString
     */
    public String toString()
    {
        return valorDec;
    }
    /**
     * Implementacion del metodo clone
     * 
     * @return  una copia de la instancia de tipo QMCAdyacenciaBean
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
    /**
     * Devuelve true si es indiferencia
     * @return indiferencia.
     */
    public boolean isIndiferencia() {
        return indiferencia;
    }
    /**
     * Establece si es indiferencia
     * @param indiferencia
     */
    public void setIndiferencia(boolean indiferencia) {
        this.indiferencia = indiferencia;
    }
    /**
     * Devuelve la posicion de la adyacencia que representa en la lista de adyacencias
     * @return posicion.
     */
    public String getPosicion() {
        return posicion;
    }
    /**
     * Introduce la posicion de la adyacencia que representa en la lista de adyacencias
     * @param posicion variable posicion a introducir.
     */
    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }
   
}
