/**
 * 
 */

package org.supremica.automata.algorithms.Guard.QMCMinimizer.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCBinarioBean;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCImplicanteBean;

/**
 *  Clase que contiene metodos de utilidades logicas
 *  @author Pedro Sanz
 *  Fecha 5/04/2006
 *
 */
@SuppressWarnings("unchecked")
public class QMCUtilLogica {
    
   
    
    /**
     * Motodo que calcula si un nomero es potencia de dos o no
     * @param valorDec 
     */
    public static boolean esPotencia2(int diferencia)
    {
                
        boolean potenciaDe2;
         
        potenciaDe2 = true;
        
        if (diferencia <=0) return false;
        
        double mitadDouble = (double) diferencia;
        long mitadLong = (long) diferencia;
        
        while (true){

            if (mitadLong == 1) break;
            
            mitadDouble = ( (double) mitadDouble ) / 2.0;
            mitadLong = ( (long) mitadLong ) / 2l;
            
            if ( mitadDouble != (double) mitadLong ) {
                potenciaDe2 = false;
                break;
            }
            
        }
        return potenciaDe2;
    }
    
    /**
     * Motodo que calcula el logaritmo en base 2 de un nomero dado
     * @param valorDec 
     */
    public static int log2(int valorDec) {
        
        
        double mitadDouble = (double) valorDec;
        long mitadLong = (long) valorDec;
        int i = 0;
        while (true){
            if (mitadLong == 1) break;
            mitadDouble = ( (double) mitadDouble ) / 2.0;
            mitadLong = ( (long) mitadLong ) / 2l;
            i++;
        }
        return i;
    }
   
    /**
     * Motodo que comprueba si dos adyacencias tienen la misma coordenada vacua
     * @param adyacenciaIndMayor
     * @param adyacenciaIndMenor
     * 
     */
    public static boolean esMismaCoordenadaVacua (QMCBinarioBean adyacenciaIndMayor, QMCBinarioBean adyacenciaIndMenor)
    {
        boolean misma;
        String coordenadasVacuasMayor, coordenadasVacuasMenor;
        StringTokenizer cvMayor, cvMenor;
                        
        coordenadasVacuasMayor = adyacenciaIndMayor.getCoordenadasVacuas();
        coordenadasVacuasMenor = adyacenciaIndMenor.getCoordenadasVacuas();
        misma = true;
        
        if(coordenadasVacuasMayor == null)
        {
            return misma;
        }
        cvMayor = new StringTokenizer(coordenadasVacuasMayor,",");
        cvMenor = new StringTokenizer(coordenadasVacuasMenor,",");
        misma = true;
        while (cvMayor.hasMoreTokens())
        {
            if(!cvMayor.nextToken().equals(cvMenor.nextToken()))
            {
                misma = false;            
                break;
            }
        }
        return misma;          
    }

    /**
     * Motodo que ordena el arrayList de terminos en funcion del indice 
     * @param terminos Los terminos a ordenar.
     * @return terminosOrdenados Los terminos ordenados.
     */
    public static ArrayList ordenaArrayListTerminos(ArrayList terminos)
    {
        // Ordenacion por insercion de la lista de terminos
        ArrayList terminosOrdenados;
        int i, j, numFilas; 
        QMCBinarioBean termino;
                     
        numFilas = terminos.size();
        terminosOrdenados = new ArrayList(terminos);
                       
        for(i=1; i<numFilas;i++)
        {
            termino = (QMCBinarioBean)terminosOrdenados.get(i);
            j = i - 1;
            while ((j >= 0) && (termino.getIndice() < ((QMCBinarioBean)terminosOrdenados.get(j)).getIndice()))
            {
                terminosOrdenados.set(j + 1,terminosOrdenados.get(j));
                j--;
                if(j == -1)
                {
                    break;  
                }
            }
            terminosOrdenados.set(j + 1, termino);               
        }
            
        return terminosOrdenados;
    }
    
    
    /**
     * Motodo que genera un array de las posiciones que ocupan los terminos de un array dado en otro array de terminos base
     * @param terminos array que contiene todos los terminos de la funcion
     * @param adyacencia String que contiene los terminos de una adyacencia
     * @return posiciones array de las posiciones relativas
     */
    public static int[] generaArrayPosiciones(Object [] terminos, String adyacencia)
    {
        StringTokenizer terminosAdyacencia;
        int x;
        int [] posiciones;
        String termino;
     
        terminosAdyacencia = new StringTokenizer(adyacencia,"-");
        posiciones = new int[terminosAdyacencia.countTokens()];
        x = 0;
        // Inicializo el array de posiciones
        for(int i=0;i<posiciones.length; i++)
        {
            posiciones[i] = -1;
        }
        // Recorre los terminos del implicante
        while(terminosAdyacencia.hasMoreTokens())
        {
            termino = terminosAdyacencia.nextToken();
            // Recorre la lista de terminos base
            for(int pos = 0; pos<terminos.length; pos++)       
            {                       
                if(String.valueOf(terminos[pos]).equals(termino))
                {
                    // Array de posiciones del implicante
                    posiciones[x]=pos;
                    x++;
                    break;
                }                
            }        
        }           
        return posiciones;
    }
    
    /**
     * Motodo que devuelve el producto de dos sumas booleanas
     * @param factor1 primer ArrayList de sumandos
     * @param factor2 segundo ArrayList de sumandos
     * @return producto ArrayList de sumandos resultantes del producto de los factores
     */     
    public static ArrayList multiplicaSumasBooleanas(ArrayList factor1, ArrayList factor2)
    {
        ArrayList producto = new ArrayList();
        String cadena = "";
        for(int i=0; i<factor1.size();i++)
        {
            for(int j=0; j<factor2.size(); j++)
            {
                cadena = multiplicaProductosBooleanos((String)factor1.get(i),(String)factor2.get(j));                
                producto.add(cadena);
            }            
        }
        // Realiza simplificacion booleana entre los sumandos del producto resultante
        int i,j;
        boolean absorcion;
        
        i = 0;
        j = 1;
       
        
        while(i<producto.size())
        {
            absorcion = false;
            while(j<producto.size())
            {
                // CADENA CONTENIDA ABSORVE A CADENA CONTENEDORA (Absorcion suma)
                // Absorve la segunda cadena
                if(contieneTerminos((String)producto.get(j),(String)producto.get(i))==1)
                {                    
                    producto.remove(j);
                }
                // Absorve la primera
                else if(contieneTerminos((String)producto.get(j),(String)producto.get(i))==0)
                {                    
                    producto.remove(i);
                    absorcion = true;
                }
                // No hay absorcion, avanza
                else
                {                
                    j++;
                }                
            }       
            if(!absorcion)
            {
                i++;
                j=i+1;
            }            
        }
       
        return producto;
    }
    
    /**
     * Motodo que compara dos cadenas de terminos e indica si una de las dos cadenas contiene todos los terminos de la otra
     * @param cadena1
     * @param cadena2
     * @return cadenaContenida numero de la cadena que es contenida en la otra. Devuelve -1 si ninguna de las dos se contienen
     */
    public static int contieneTerminos(String cadena1, String cadena2)
    {
        int cadenaContenida;        
        String cadenaPequeoa, cadenaGrande;
        
        if(cadena1.length()>cadena2.length())
        {
            cadenaGrande = cadena1;
            cadenaPequeoa = cadena2;
            cadenaContenida = 1; 
        }
        else
        {
            cadenaGrande = cadena2;
            cadenaPequeoa = cadena1;
            cadenaContenida = 0;
        }       
        for(int i=0; i<cadenaPequeoa.length() && cadenaContenida!=-1; i++)
        {
            if(cadenaGrande.indexOf(cadenaPequeoa.charAt(i))==-1)
            {
                cadenaContenida = -1;
            }
        }
        
        return cadenaContenida;
    }
    /**
     * Multiplica dos cadenas de forma booleana
     * @param cadena1 producto de variables
     * @param cadena2 producto de variables
     * @return
     */
    public static String multiplicaProductosBooleanos(String cadena1, String cadena2)
    {
        String cadena;
        // Elimina todos los elementos de la cadena1 que aparezcan en la cadena2
        for (int i=0;i<cadena2.length();i++)
        {
           cadena1 = cadena1.replace(String.valueOf(cadena2.charAt(i)),"");
        }
        cadena = cadena1+cadena2;
        return cadena;
    }
    
    /**
     * Metodo que extrae un implicante de la lista dado su nombre
     * @param implicantes
     * @param nombre
     * @return implicante
     */
    public static QMCImplicanteBean buscaImplicante(ArrayList implicantes, char nombre)
    {
        QMCImplicanteBean implicante = new QMCImplicanteBean();
        
        for(int j=0;j<implicantes.size();j++)
        {            
            implicante = (QMCImplicanteBean)implicantes.get(j);            
            if(nombre==implicante.getNombre())
            {                     
                return implicante;
            }
        }
        return implicante;
    }
    
    /**
     * Devuelve la posicion de la adyacencia buscada dentro de su lista contenedora
     * @param listaAdyacencias
     * @param posiciones posiciones de las adyacencias que generan la adyacencia buscada
     * @return posicion
     */
    public static int buscaAdyacencia(ArrayList listaAdyacencias, String posiciones)
    {
        QMCBinarioBean adyacencia;
        int posicion = 0;
        while(posicion<listaAdyacencias.size())
        {
            adyacencia = (QMCBinarioBean)listaAdyacencias.get(posicion);
            if(posiciones.equals(adyacencia.getPosicion()))
            {
                return posicion;
            }
            posicion++;
        }
        // si no se encuentra
        return posicion = -1;
    }
    
    /**
     * Comprueba si todos los terminos estan cubiertos
     * @param terminosLista
     * @return
     */
    public static boolean compruebaTerminosCubiertos(ArrayList terminosLista)
    {        
        boolean terminosTodosCubiertos = true;
        QMCBinarioBean adyacencia;
        for(int i=0;i<terminosLista.size();i++)
        {            
            adyacencia = (QMCBinarioBean)terminosLista.get(i);
            if(!adyacencia.isCubierta())
            {
                terminosTodosCubiertos = false;
                return terminosTodosCubiertos;
            }
        }
        return terminosTodosCubiertos;
    }
    /**
     * Comprueba si existen variables repetidas en la cadena de variables dada
     * @param variables
     * @return repetidas
     */
    public static boolean variablesRepetidas (String variables)
    {
        boolean repetidas;
        char var1, var2;
        int j;
        
        variables = variables.replace(",","");
        repetidas = false;
        
        for(int i=0;i<variables.length();i++)
        {
            var1 = variables.charAt(i);
            j=i+1;
            while (j<variables.length())
            {
                var2 = variables.charAt(j);
                if(var1 == var2)
                {
                    repetidas = true;
                    return repetidas;
                }
                j++;
            }
        }
        return repetidas;       
    }
    /**
     * Metodo que comprueba si la cadena de variables introducida es suficiente para cubrir el numero de terminos dado
     * @param variables
     * @param terminos
     * @return suficientes
     */
    public static boolean variablesSuficientes (String variables, String terminos)
    {
        String ultimoTermino = "";
        boolean suficientes = false;        
        variables = variables.replace(",","");
        StringTokenizer st = new StringTokenizer(terminos,",");
        
        while (st.hasMoreTokens())
        {
            ultimoTermino = st.nextToken();            
        }
        if(Math.pow(2,variables.length())> Integer.parseInt(ultimoTermino))
        {
            suficientes = true;      
        }
        return suficientes;       
    }
    
    /**
     * Comprueba todas las condiciones de adyacencia entre dos terminos o dos adyacencias 
     * @param posicionMenor
     * @param posicionMayor
     * @param listaAdyacencias
     * @return codigo de error devuelto en caso de que se incumpla alguna de las condiciones de adyacencia
     */
    public static String compruebaAdyacencia (int posicionMenor, int posicionMayor, ArrayList listaAdyacencias)
    {
        String comprobacion;
        StringTokenizer st;
        QMCBinarioBean adyacenciaMenor, adyacenciaMayor;
        int termAdyacenciaMenor, termAdyacenciaMayor;
        
        adyacenciaMenor = (QMCBinarioBean)listaAdyacencias.get(posicionMenor);
        adyacenciaMayor = (QMCBinarioBean)listaAdyacencias.get(posicionMayor);
        
        st = new StringTokenizer(adyacenciaMenor.getValorDec(),"-");
        termAdyacenciaMenor = Integer.parseInt(st.nextToken());
        
        st = new StringTokenizer(adyacenciaMayor.getValorDec(),"-");
        termAdyacenciaMayor = Integer.parseInt(st.nextToken());
        
        comprobacion = "Correcta";
        
        if(adyacenciaMayor.getIndice()-adyacenciaMenor.getIndice()==1)
        {
            if(termAdyacenciaMayor>termAdyacenciaMenor)
            {
                if(esPotencia2(termAdyacenciaMayor-termAdyacenciaMenor))
                {
                    if(esMismaCoordenadaVacua(adyacenciaMayor, adyacenciaMenor))
                    {
                        return comprobacion;
                    }
                    else 
                    {
                        comprobacion = "Las coordenadas vacuas no coinciden";
                    }                    
                }
                else
                {
                    comprobacion = "La diferencia entre terminos no es una potencia de 2";
                }
            }
            else 
            {
                // El primer termino es mayor que el segundo
                comprobacion = "El primer termino es mayor que el segundo";
            }
        }
        else
        {             
            comprobacion = "La diferencia entre indices es distinta de 1";
        }
        return comprobacion;        
    }
    
    public static boolean compruebaImplicanteEsencial(ArrayList listaImplicantes, int posicion)
    {       
        QMCImplicanteBean implicante;
        
        implicante = (QMCImplicanteBean)listaImplicantes.get(posicion);
        if(implicante.isEsencial())
        {
            return true;
        }
        return false;   
    }
    
    /**
     * Comprueba si la cadena de implicantes dada existe como parte de la solucion
     * @param listaCadenasImplicantes
     * @param cadenaImplicantesElegidos
     * @return
     */
    public static String compruebaImplicantesNoEsenciales(ArrayList listaCadenasImplicantes, String cadenaImplicantesElegidos)
    {
        String comprobacion, cadena;
        comprobacion = "correcto";        
        
        cadena = (String)listaCadenasImplicantes.get(0);
        cadenaImplicantesElegidos = cadenaImplicantesElegidos.replace("f = ","");
        cadenaImplicantesElegidos = cadenaImplicantesElegidos.replace(" + ","");        
        // Es expresion minima
        if(cadena.length() == cadenaImplicantesElegidos.length())
        {
            for(int i=0;i<listaCadenasImplicantes.size();i++)
            {
                cadena = (String)listaCadenasImplicantes.get(i);
                // Cubre todos los terminos
                if(contieneTerminos(cadenaImplicantesElegidos, cadena)!=-1)
                {
                    comprobacion = "correcto";
                    return comprobacion;                                        
                }                             
                else
                {
                    comprobacion = "No cubren todos los terminos";
                }
            }
        }
        else if(cadena.length() > cadenaImplicantesElegidos.length())
        {
            comprobacion = "No cubren todos los terminos" ;
        }       
        else
        {
            comprobacion = "No es expresion monima";
        }
        return comprobacion;        
    }   
    
    /**
     * Motodo que busca la posicion de la adyacencia formada por los terminos de las filas 1 y 2
     * @param listaAdyacenciasAnterior
     * @param listaAdyacenciasActual
     * @param fila1
     * @param fila2
     * @return posicion
     */
    public static int buscaAdyacenciaCorrespondiente(ArrayList listaAdyacenciasAnterior, ArrayList listaAdyacenciasActual, int fila1, int fila2)
    {
    	int pos;
    	boolean contiene;    	
    	String valorDec1, valorDec2, valorDec12, termino;
    	StringTokenizer stPosibleAdyacencia, stAdyacencia;
    	QMCBinarioBean adyacencia;
    	
    	valorDec1 = ((QMCBinarioBean)listaAdyacenciasAnterior.get(fila1)).getValorDec();
    	valorDec2 = ((QMCBinarioBean)listaAdyacenciasAnterior.get(fila2)).getValorDec();
    	valorDec12 = valorDec1+"-"+valorDec2;    	
    	pos = -1;
    	contiene = false;
    	
    	// Busca en la lista de adyacencias miesntras no encuentre
    	while(pos<listaAdyacenciasActual.size()-1 && !contiene)
    	{
    		pos++;
    		adyacencia = (QMCBinarioBean)listaAdyacenciasActual.get(pos);
    		stPosibleAdyacencia = new StringTokenizer(valorDec12,"-");
    		contiene = true;
    		
    		// Compara adyacencias
    		while(stPosibleAdyacencia.hasMoreTokens() && contiene)
    		{
    			stAdyacencia = new StringTokenizer(adyacencia.getValorDec(),"-");
    			termino = stPosibleAdyacencia.nextToken();
    			
    			// Comprueba un termino con el resto
    			while(stAdyacencia.hasMoreTokens())
    			{
    				if(termino.equals(stAdyacencia.nextToken()))
    				{
    					contiene = true;
    					break;
    				}
    				else
    				{
    					contiene = false;    					
    				}
    			}    			
    		}    		
    	}    	
    	return pos;
    }
    
}




