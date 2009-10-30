/**
 * 
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;



/**
 *  Clase tipo bean que define las caracteristicas de una funcion de conmutacion en forma canónica
 *  @author Pedro Sanz
 *  Fecha 20/11/2005
 *
 */
public class QMCFuncion {
	
	
    int numVariables;
    private char forma;
    
    private String [] variables;
    private String [] terminos;
    private String [] indiferencias;
        
    private ArrayList<QMCBinarioBean> listaTerminos;
    private ArrayList<QMCBinarioBean> listaBinarios;
    private ArrayList<QMCBinarioBean> listaBinariosInteractivos;
    private boolean tieneTerminos;
    
      
   

    /**
     * Devuelve la forma canónica de la función
     * @return   forma puede ser 's' o 'p'
	 */
	public char getForma() {
		return forma;
	}
	
	/**
	 * establece la forma.
	 * @param forma puede ser 's' o 'p'
	 */
	public void setForma(char forma) {
		this.forma = forma;
	}
	
	/**
	 * Devuelve el numero de variables de la funcion
	 * @return  numVariables.
	 */
	public int getNumVariables() {
		return numVariables;
	}
	/**
	 * Devuelve el numero de variables a introducir.
	 * @param numVariables 
	 */
	public void setNumVariables(int numVariables) {
		this.numVariables = numVariables;
	}
   
    /**
     * Devuelve array de variables
     * @return variables
     */
    public String [] getVariables()
    {
        return variables;
        
    }
    
    /**
     * Devuelve array de terminos
     * @return terminos
     */
    public String[] getTerminos()
    {
        return terminos;
    }
    
    /**
     * Devuelve array de indiferencias
     * @return indiferencias
     */
    public String[] getIndiferencias()
    {
        return indiferencias;
    }
    
    /**
     * Primera forma del método que establece las variables en caso de tabla nueva
     * @param numVariables
     */
    public void setVariables(int numVariables)
    {
                  
        char variable = 97;
        variables = new String[numVariables];
        this.numVariables = numVariables;
        
        // Creo los valores de la cabecera
        for(int i=0; i<numVariables; i++)
        {
            // Si la variable es f se la salta
            if(variable==102)
            {                
                variable+=1;
            }
            variables[i] = String.valueOf(variable);
            variable +=1;
        }
              
    }
    
    /**
     * Segunda forma del método que establece las variables, en caso de función nueva
     * @param cadenaVariables
     */
    public void setVariables (String cadenaVariables)
    {
        StringTokenizer stVariables;
        int i;
        
        stVariables = new StringTokenizer(cadenaVariables,",");
        numVariables = stVariables.countTokens();
        variables = new String[numVariables];
        i=0;
        while(stVariables.hasMoreElements())
        {
            variables[i]=stVariables.nextToken();
            i++;
        }
                     
    }
    /**
     * Método que introduce los términos en un array simple
     * @param cadenaTerminos
     */
    public void setTerminos (String cadenaTerminos)
    {
        
        StringTokenizer stTerminos;
        int i;
        
        stTerminos = new StringTokenizer(cadenaTerminos,",");
        terminos = new String[stTerminos.countTokens()];
        i=0;
        while(stTerminos.hasMoreElements())
        {
            terminos[i]=stTerminos.nextToken();
            i++;
        }
        
        
    }
    /**
     * Método que introduce las indiferencias en la función.
     * @param cadenaIndiferencias
     */
    public void setIndiferencias (String cadenaIndiferencias)
    {
        StringTokenizer stIndiferencias;
        int i;
        
        stIndiferencias = new StringTokenizer(cadenaIndiferencias,",");
        indiferencias = new String[stIndiferencias.countTokens()];
        i=0;
        while(stIndiferencias.hasMoreElements())
        {
            indiferencias[i]=stIndiferencias.nextToken();
            i++;
        }
    }
          
    
    /**
     * Devuelve  la lista de binarios terminos
     * @return listaTerminos
     */
    public ArrayList<QMCBinarioBean> getListaTerminos() {
        return listaTerminos;
    }
    
    /**
     * Genera la lista de los datos binarios de la funcion
     * @param listaBinarios  arrayList de binarios
     */
    public void setListaBinarios(Vector<?> datosTabla) 
    {
        Vector<?> fila;
        String valorTermino, valorDec, valorBin;
        QMCBinarioBean binarioBean, termino; 
        int indice, posicion, contadorIndiferencias;
                        
        // forma SOP
        if (forma=='s')
        {
            valorTermino = "1";
        }
        // forma POS
        else
        {
            valorTermino = "0";
        }
                      
        tieneTerminos = false;
        posicion = 0;
        contadorIndiferencias = 0;
        
        // Inicializa las listas
        listaBinarios = new ArrayList<QMCBinarioBean>();
        listaTerminos = new ArrayList<QMCBinarioBean>();
        
        for (int i=0; i<datosTabla.size(); i++)
        {
            fila = (Vector<?>)datosTabla.elementAt(i);
            // Genera los objetos binario a partir de la tabla de verdad 
            binarioBean = new QMCBinarioBean();      
            // Valor decimal 
            valorDec = String.valueOf(i);;
            binarioBean.setValorDec(valorDec);
            indice = 0;
            valorBin = "";
            for (int j=0;j<fila.size()-1;j++)
            {
                // Valor binario
                valorBin+=fila.elementAt(j);
                // Indice del termino (realiza el conteo de unos)
                if (fila.elementAt(j).toString().equals("1"))
                {
                    indice++;
                }                   
             }
             // Introduce los datos en el bean y lo añade a la lista
             binarioBean.setValorBin(valorBin);
             binarioBean.setIndice(indice); 
             if(fila.elementAt(numVariables).equals(valorTermino))
             {
                 binarioBean.setTermino(true); 
                 binarioBean.setPosicion(String.valueOf(posicion));
                 listaTerminos.add(binarioBean);
                 posicion++;
             }
             else if(fila.elementAt(numVariables).toString().equals("X"))
             {
                 binarioBean.setIndiferencia(true);
                 binarioBean.setPosicion(String.valueOf(posicion));
                 listaTerminos.add(binarioBean);
                 posicion++;
                 contadorIndiferencias++;
             }                 
             listaBinarios.add(binarioBean);              
        }
        // Genera la lista sencilla de terminos excluyendo los terminos indiferencia
        int i,j,z;        
        terminos = new String[listaTerminos.size()-contadorIndiferencias];  
        indiferencias = new String[contadorIndiferencias];
        i = 0;
        j = 0;
        z = 0;
        while(i<listaTerminos.size())
        {
            termino = listaTerminos.get(i);
            if(termino.isIndiferencia() == false)
            {
                terminos[j] = termino.getValorDec();
                j++;
            }
            else
            {
            	indiferencias[z] = termino.getValorDec();
            	z++;
            }
            i++;            
        }        
        if(listaTerminos.size()!=0)
        {
            tieneTerminos = true;
        }        
    }
    
    /**
     * Genera un ArrayList de binarios vacios (modo interactivo)
     * @param listaBinariosInteractivos  
     */
    public void setListaBinariosInteractivos(int numVariables)
    {
        QMCBinarioBean binarioInteractivo;
        int numBinarios;
        
        numBinarios = (int)Math.pow(2,numVariables);       
        
        // Inicializa la lista de terminos
        listaBinariosInteractivos = new ArrayList<QMCBinarioBean>();      
        for (int i=0; i<numBinarios; i++)
        {
            binarioInteractivo = new QMCBinarioBean();
            binarioInteractivo.setValorDec(String.valueOf(i));
            binarioInteractivo.setTermino(false);
            listaBinariosInteractivos.add(binarioInteractivo);
        }
    }    
    /**
    * Comprueba si la funcion tiene terminos
    * @return tieneTerminos
    */ 
    public boolean isTieneTerminos()
    {
        return tieneTerminos;
    }
    /**
     * Devuelve un ArrayList de binarios
     * @return listaBinarios.
     */
    public ArrayList<QMCBinarioBean> getListaBinarios() {
        return listaBinarios;
    }
    /**
     * Devuelve un ArrayList de binarios vacios (modo interactivo)
     * @return listaBinariosInteractivos.
     */
    public ArrayList<QMCBinarioBean> getListaBinariosInteractivos() {
        if(listaBinariosInteractivos != null)
        {
            QMCBinarioBean binario;
            for(int i=0;i<listaBinariosInteractivos.size();i++)
            {
                binario = listaBinariosInteractivos.get(i);
                binario.setTermino(false);
            }
        }
        return listaBinariosInteractivos;
    }
   
    
  
    
    
	



}
