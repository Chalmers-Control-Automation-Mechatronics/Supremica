/**
 * 
 */

package org.supremica.automata.algorithms.Guard.QMCMinimizer.util;

import java.util.ArrayList;
import java.util.Enumeration;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCBinarioBean;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCImplicanteBean;


/**
 * Clase que contiene utilidades para el tabulado de listas en forma de matrices y formateo de expresiones.
 * @author Pedro Sanz
 * Fecha creación 19-jun-2006
 */
public class QMCUtilFormateo 
{
    
    /**
     * Genera los datos binarios de la tabla de verdad
     * @param cadenaTerminos
     * @param cadenaIndiferencas
     */
    public static Object[][] generaDatosTablaVerdad(int numVariables, char forma, String[] terminos, String[] indiferencias)
    {
       
        Object [][] binarios;
        char[] arrayBin;
        int indiceComienzoArrayBin;
        int filas;
        int columnas; 
        int x;
        
        columnas = numVariables+1;
        filas = (int)Math.pow(2,numVariables);
        binarios = new Object[filas][columnas];
        
        
        // Genera los valores de binarios (terminos y binarios correspondientes)
        for(int i=0; i<filas; i++)
        {
           arrayBin = Integer.toBinaryString(i).toCharArray();
           indiceComienzoArrayBin = numVariables-arrayBin.length;
                      
           for(int j=0; j<columnas-1; j++)
           {
               
               // Binarios
               if(j < indiceComienzoArrayBin)
               {
                   // Rellena con ceros
                   binarios[i][j] = Integer.valueOf(0);
               }
               else
               {
                   // Empieza numero binario
                   binarios[i][j] = Integer.valueOf(String.valueOf(arrayBin[j-indiceComienzoArrayBin]));
               }
                       
           }
                   
         }
        // Introduce los términos especificados en la tabla
        if(terminos!=null)
        {
            // Minterms, seleccion por defecto
            int digito = 1;
            int relleno = 0;
           
            // Maxterms
            if(forma=='p')
            {
                digito = 0;
                relleno = 1;
            
            }                            
            
            x = 0;         
            for (int i=0; i<filas; i++)
            {                
                if(i == Integer.parseInt(terminos[x]))
                {
                    binarios[i][columnas-1]=Integer.toString(digito);
                     if (x<terminos.length-1)
                    {
                        x++;
                    }
                }
                else
                {
                    binarios[i][columnas-1]=Integer.toString(relleno);
                }                               
            }
            
            // Introduce las indiferencias
            if (indiferencias!=null)
            {
                x = 0;                 
                for(int i=0; i<filas; i++)
                {
                    if(i == Integer.parseInt(indiferencias[x]))
                    {
                        binarios[i][columnas-1]="X";
                        if (x<indiferencias.length-1)
                        {
                            x++;
                        }
                    }
                }                       
            }                
        }
        return binarios;
    }
    /**
     * Formatea el array de variables de la funcion en un array como cabecera de la tabla de verdad
     * @param variables
     * @return cabecera
     */
    public static String[] generaCabeceraTablaVerdad(String [] variables)
    {
        String []cabecera;
        cabecera = new String[variables.length+1];
        
        cabecera[variables.length] = "f";
        
        for(int i=0; i<variables.length; i++)
        {
            cabecera[i]=variables[i];
        }        
        return cabecera;
        
    }
    
    
    
    /**
     * Formatea el arrayList del primer paso en una matriz compatible con la tabla destino
     * @param terminos 
     */
    public static String[][] generaMatrizTerminos(ArrayList terminos, boolean minimizacionAuto) {
        
        int numFilas;
        String matrizTerminos [][];
        QMCBinarioBean termino;
               
        numFilas = terminos.size();
        matrizTerminos = new String [numFilas][3];
                        
        for(int i=0;i<numFilas; i++)
        {
            // Valor decimal del termino
            termino = (QMCBinarioBean)terminos.get(i);
            if(minimizacionAuto)
            {
                matrizTerminos[i][0] = termino.getValorDec();
                matrizTerminos[i][1] = termino.getValorBin();
                matrizTerminos[i][2] = String.valueOf(termino.getIndice());                 
            }
            else
            {
                matrizTerminos[i][0] = "";
                matrizTerminos[i][1] = "";
                matrizTerminos[i][2] = ""; 
            }
           
        }
         
        return matrizTerminos;
    }
    
    /**
     * Formatea la lista de terminos ordenados en una matriz compatible con la tabla destino
     * @param terminos
     */
    public static Object[][] generaMatrizTerminosOrdenados(ArrayList terminos, boolean minimizacionAuto)
    {
        //Boolean usado;
        QMCBinarioBean termino, terminoAnt;
        Object [][] matrizTerminosOrdenados = new Object [terminos.size()][3];
        terminoAnt = null;
        
        for(int i=0; i<terminos.size(); i++)
        {
            termino = (QMCBinarioBean)terminos.get(i);
            
            if(terminoAnt == null || termino.getIndice()!=terminoAnt.getIndice())
            {
                matrizTerminosOrdenados[i][0] = String.valueOf(termino.getIndice());
            }
            if(minimizacionAuto)
            {
                matrizTerminosOrdenados[i][1] = termino.getValorDec();
                matrizTerminosOrdenados[i][2] = new Boolean(termino.isUsada());          
            }
            else
            {
                matrizTerminosOrdenados[i][1] = "";
                matrizTerminosOrdenados[i][2] = new Boolean(false);     
            }
            terminoAnt = termino;
        }
        return matrizTerminosOrdenados;
    }
    
    /**
     * Formatea la lista de adyacencias en una matriz compatible con la tabla destino
     * @param adyacencias
     */
    public static Object[][] generaMatrizAdyacencias (ArrayList adyacencias, boolean minimizacionAuto)
    {
        
        QMCBinarioBean adyacencia, adyacenciaAnt;
        Object [][] matrizAdyacencias = new Object [adyacencias.size()][4];
        adyacenciaAnt = null;
        
        for(int i=0; i<adyacencias.size(); i++)
        {
            adyacencia = (QMCBinarioBean)adyacencias.get(i);
            
            if(adyacenciaAnt == null || adyacencia.getIndice()!=adyacenciaAnt.getIndice())
            {
                matrizAdyacencias [i][0] = String.valueOf(adyacencia.getIndice());
            }
            if(minimizacionAuto)
            {
                matrizAdyacencias [i][1] = adyacencia.getValorDec()+" ("+adyacencia.getCoordenadasVacuas()+")" ;
                matrizAdyacencias [i][2] = adyacencia.getValorBin();
                matrizAdyacencias [i][3] = new Boolean(adyacencia.isUsada());  
            }
            else
            {
                matrizAdyacencias [i][1] = ""; 
                matrizAdyacencias [i][2] = "";
                matrizAdyacencias [i][3] = new Boolean(false); 
            }                                 
            adyacenciaAnt = adyacencia;
          
        }
        return matrizAdyacencias;
    }
    /**
     * Formatea la lista de implicantes primos en una matriz compatible con la tabla destino
     * @return implicantes
     */
    public static Object[][] generaMatrizImplicantes (ArrayList implicantes)
    {
        Object [][] matrizImplicantesPrimos = new Object [implicantes.size()][4];
        QMCImplicanteBean implicante;
        
        for(int i=0; i<implicantes.size(); i++)
        {
            implicante = (QMCImplicanteBean)implicantes.get(i);
            
            matrizImplicantesPrimos[i][0] = String.valueOf(implicante.getOrden());
            matrizImplicantesPrimos[i][1] = implicante.getTerminos();
            matrizImplicantesPrimos[i][2] = implicante.getValorBin();
            matrizImplicantesPrimos[i][3] = String.valueOf(implicante.getNombre());            
        }
        
        return matrizImplicantesPrimos;
    }
    /**
     * Genera los datos de la matriz de Implicantes Esenciales 
     * @param listaImplicantes
     * @param terminos
     * @return matrizImplicantes
     */
    
    public static Object [][] generaMatrizImplicantesEsenciales(ArrayList listaImplicantes, Object [] terminos)
    {
        Object [][] matrizImplicantes = new Object[listaImplicantes.size()][terminos.length];
        int [] posiciones;
        int x;
        QMCImplicanteBean implicante;
        
        for(int i=0;i<listaImplicantes.size();i++)
        {
            implicante = (QMCImplicanteBean)listaImplicantes.get(i);
            posiciones = implicante.getPosicionesTerminos();
            x = 0;
            for(int j=0;j<terminos.length;j++)
            {
                if(posiciones[x]==j)
                {
                    matrizImplicantes[i][j]= "X";
                    
                    if( x<posiciones.length-1)
                    {
                        x++;  
                    }                    
                }  
                else
                {
                    matrizImplicantes[i][j]= "";  
                }
            }
        }
        return matrizImplicantes;
    }

    /**
     * Método que convierte un array de elementos String en una cadena
     * @param array
     * @return cadena
     */
    public static String convierteArray2cadena(String array[])
    {
        String cadena = "";
        for(int i=0;i<array.length;i++)
        {
            cadena += array[i];
        }
        return cadena;        
    }
   
    /**
     * Metodo que formatea la lista de implicantes esenciales en una cadena
     * @param implicantesPrimos
     * @return cadena
     */
    public static String generaCadenaImplicantesEsenciales(ArrayList implicantesPrimos, char forma)
    {
        String cadena = "f = ";
        QMCImplicanteBean implicante;
        String union;    
        if(forma != 'n')
        {
            if(forma == 's')
            {
                union = " + ";
            }
            else
            {
                union = " * ";
            }
        }
        else
        {
            cadena ="";
            union="";
        }        
        
        for(int i=0;i<implicantesPrimos.size();i++)
        {
            implicante = (QMCImplicanteBean)implicantesPrimos.get(i);
            if(implicante.isEsencial())
            {
                cadena+=implicante.getNombre()+union;
                
            }            
        }       
        return cadena;
    }
    /**
     * Método que que formatea la lista de grupos de implicantes no esenciales en un array de cadenas
     * @param solucionesMinimas
     * @return
     */
    public static String[] generaArrayImplicantesNoEsenciales(ArrayList solucionesMinimas, char forma)
    {
        String [] gruposImplicantesNoEsenciales;
        String grupo;
        String union;
        
        gruposImplicantesNoEsenciales = new String [solucionesMinimas.size()];
        if(forma == 's')
        {
            union = "+";
        }
        else
        {
            union = "*";
        }
        
        for(int i=0;i<solucionesMinimas.size();i++)
        {
            grupo = (String)solucionesMinimas.get(i);
            gruposImplicantesNoEsenciales[i] = "";
            
            for(int j=0; j<grupo.length();j++)
            {
                gruposImplicantesNoEsenciales[i] += String.valueOf(grupo.charAt(j))+" "+union+" ";                          
            }
            // Elimina el ultimo simbolo de suma
            gruposImplicantesNoEsenciales[i] = gruposImplicantesNoEsenciales[i].substring(0,gruposImplicantesNoEsenciales[i].length()-2);
        }
        return gruposImplicantesNoEsenciales;
    }
    
   /**
    * Metodo que genera la expresion algebraica booleana de una funcion dados sus variables, terminos y forma      
    * @param variables
    * @param listaTerminos
    * @param forma
    * @return expresionAlgebraica
    */
    public static String generaExpresionBooleana(String [] variables, ArrayList listaTerminos, char forma) 
    {
        
        String binarioTermino, variablesFuncion, complemento, noComplemento, concatTerms, simbolo, terminoAlg, expresionAlg;
        char binarioVar, termino;        
        terminoAlg = "";
        variablesFuncion = "";
        
        // Obtiene las variables para la funcion
        for(int i=0; i<variables.length; i++)
        {
            variablesFuncion += variables[i]+",";                 
        }
        variablesFuncion = variablesFuncion.substring(0, variablesFuncion.length()-1);
        
        // forma SOP
        if (forma=='s')
        {
            termino = '1';
            complemento = "'";
            noComplemento = "";
            concatTerms = " + ";  
            expresionAlg = "f ("+variablesFuncion+") = ";
        }
        // forma POS
        else
        {
            termino = '0';
            complemento = "'+";
            noComplemento = "+";
            concatTerms = ")*(";
            expresionAlg = "f ("+variablesFuncion+") = (";
        }
        // Si hay terminos    
        if (listaTerminos.size()!=0)
        {
            // Recorre las filas
            for (int i=0; i<listaTerminos.size();i++)
            {
                
                // Recorre los digitos binarios del termino
                if(listaTerminos.get(i) instanceof QMCBinarioBean)
                {
                    binarioTermino = ((QMCBinarioBean)listaTerminos.get(i)).getValorBin();
                }
                else
                {
                    binarioTermino = ((QMCImplicanteBean)listaTerminos.get(i)).getValorBin();           
                }
                for(int j=0; j<variables.length; j++)
                {
                    // Comprueba si debe complementarse la variable correspondiente
                    binarioVar = binarioTermino.charAt(j);
                    if (binarioVar==termino)
                    {
                        simbolo = noComplemento;
                    }                
                    else
                    {
                        simbolo = complemento;
                    }
                    // Comprueba si la variable ha sido eliminada
                    if(binarioVar!='-')
                    {
                        // Añado la variable al termino algebraico
                        terminoAlg = terminoAlg + variables[j] + simbolo;
                    }
                    
                }
                if (forma=='p')
                {
                   terminoAlg = terminoAlg.substring(0, terminoAlg.length()-1);
                }
                if(terminoAlg == "")
                {
                    expresionAlg += "1";
                }
                // Añade el termino algebraico a la expresion algebraica
                expresionAlg += terminoAlg + concatTerms;
                terminoAlg = "";
                                
            }
            // Elimina los simbolos sobrantes
            expresionAlg = expresionAlg.substring(0, expresionAlg.length()-2);            
        }
        // Si no hay terminos
        else
        {
            expresionAlg += "0";
        }
        
        return expresionAlg;
    }
    /**
     * Método que actualiza los terminos cubiertos por una lista de implicantes dada
     * @param listaImplicantes
     * @param listaTerminos
     */
    @SuppressWarnings("unchecked")
    public static void renuevaMarcas(ArrayList listaImplicantes, ArrayList listaTerminos)
    {
        QMCImplicanteBean implicante;
        for(int i=0;i<listaImplicantes.size();i++)
        {
            implicante = (QMCImplicanteBean)listaImplicantes.get(i);
            if(implicante.isEsencial())
            {
                implicante.marcaTerminosCubiertos(listaTerminos,true);
            }            
        }        
    } 
    /**
     * Convierte un array de String a una cadena string normal uniendo las posiciones del array por el separador dado
     * @param arrayCadenas
     * @param separador
     * @return
     */
    public static String array2String(String[] arrayCadenas, char separador)
    {
        String cadenaSeparada = "";
        for(int i=0;i<arrayCadenas.length;i++)
        {
            cadenaSeparada += arrayCadenas[i] + separador;            
        }
        cadenaSeparada = cadenaSeparada.substring(0,cadenaSeparada.length()-1);
        return cadenaSeparada;
    }
    
    public static String copiaTabla(Enumeration columnas)
    {
        String tablaSerializada = "";
        return tablaSerializada;
        
    }

}
