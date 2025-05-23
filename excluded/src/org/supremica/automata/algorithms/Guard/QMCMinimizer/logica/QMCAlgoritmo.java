/**
 * Paquete que contiene las clases de la logica de la aplicaci�n
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilFormateo;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilLogica;




/**
 *  Clase que contiene los algoritmos de cada paso del metodo Quine-McCluskey
 *  @author Pedro Sanz
 *  Fecha 13/02/2006
 *
 */

public class QMCAlgoritmo {



    private ArrayList<ArrayList<QMCBinarioBean>> arrayListasAdyacencias;
    private ArrayList<QMCBinarioBean> listaAdyacenciasActual;
    private ArrayList<QMCBinarioBean> listaAdyacenciasAnterior;

    private ArrayList<QMCImplicanteBean> listaImplicantesPrimos;
    private ArrayList<QMCImplicanteBean> listaImplicantesReducida;

    private ArrayList<QMCBinarioBean> listaTerminosImplicantes;
    private ArrayList<QMCBinarioBean> listaTerminosNoCubiertos;

    private ArrayList<String> listaSolucionesMinimas;
    private ArrayList<QMCImplicanteBean> listaImplicantesSolucion;

    private Object [][] matrizImplicantesEsenciales;
    private Object [][] matrizImplicantesReducida;

    private ArrayList<QMCImplicanteBean> listaImplicantesInteractivos;
    private ArrayList<QMCBinarioBean> listaTerminosInteractivos;

    private boolean terminosTodosCubiertos;
    private boolean minimizable;






    /**
     * Metodo que establece un array con las listas de adyacencias marcadas de todos los ordenes
     * @param terminos lista de terminos a partir de los cuales se generaran las diversas listas de adyacencias.
     */
    public void setArrayListasAdyacencias(final ArrayList<QMCBinarioBean> terminos)
    {
        ArrayList<QMCBinarioBean> adyacencias, adyacenciasOrdenSuperior;

        QMCBinarioBean adyacenciaIndMenor, adyacenciaIndMayor;
        QMCBinarioBean adyacenciaNueva;


        String adyacenciaBinTemp, adyacenciaBin;
        String terminosAdyacenciaMayor, terminosAdyacenciaMenor, terminosAdyacenciaNueva, coordenadasVacuas;
        StringTokenizer termsMenor, termsMayor, cv;
        int primerTerminoMayor, primerTerminoMenor, ultimoTerminoMenor, coordenadaVacua;
        int j,x;
        boolean existenAdyacenciasSuperiores;

        arrayListasAdyacencias = new ArrayList<ArrayList<QMCBinarioBean>>();
        existenAdyacenciasSuperiores = true;
        x = 0;
        adyacencias = new ArrayList<QMCBinarioBean>(QMCUtilLogica.ordenaArrayListTerminos(terminos));


        while(existenAdyacenciasSuperiores)
        {

            adyacenciasOrdenSuperior = new ArrayList<QMCBinarioBean>();
            existenAdyacenciasSuperiores = false;
            for(int i=0;i<adyacencias.size();i++)
            {
                adyacenciaIndMenor = adyacencias.get(i);
                j = i+1;
                while(j<adyacencias.size())
                {
                    adyacenciaIndMayor = adyacencias.get(j);

                    terminosAdyacenciaMenor = adyacenciaIndMenor.getValorDec();
                    terminosAdyacenciaMayor = adyacenciaIndMayor.getValorDec();

                    termsMenor = new StringTokenizer(terminosAdyacenciaMenor,"-");
                    termsMayor = new StringTokenizer(terminosAdyacenciaMayor,"-");

                    primerTerminoMenor = Integer.parseInt(termsMenor.nextToken());
                    ultimoTerminoMenor = primerTerminoMenor;
                    while(termsMenor.hasMoreTokens())
                    {
                        ultimoTerminoMenor = Integer.parseInt(termsMenor.nextToken());
                    }
                    primerTerminoMayor = Integer.parseInt(termsMayor.nextToken());

                    // Condicion diferencia de indices 1
                    if(adyacenciaIndMayor.getIndice()-adyacenciaIndMenor.getIndice() == 1)
                    {
                        // Condiciones valor decimal mayor, diferencia potencia de 2, misma coordenada vacua
                        if((primerTerminoMayor>primerTerminoMenor) && (QMCUtilLogica.esPotencia2(primerTerminoMayor-primerTerminoMenor)) && (QMCUtilLogica.esMismaCoordenadaVacua(adyacenciaIndMayor,adyacenciaIndMenor)))
                        {
                            // marca adyacencias usadas del orden inferior
                            adyacenciaIndMenor.setUsado(true);
                            adyacenciaIndMayor.setUsado(true);
                            adyacencias.set(i,adyacenciaIndMenor);
                            adyacencias.set(j,adyacenciaIndMayor);
                            // Evita las adyacencias repetidas
                            if(primerTerminoMayor>ultimoTerminoMenor)
                            {
                                // CREA DATOS ADYACENCIA NUEVA
                                // genera coordenadas vacuas
                                coordenadaVacua = primerTerminoMayor-primerTerminoMenor;
                                if(adyacenciaIndMayor.getCoordenadasVacuas()==null)
                                {
                                    coordenadasVacuas = String.valueOf(coordenadaVacua);
                                }
                                else
                                {
                                    coordenadasVacuas = adyacenciaIndMayor.getCoordenadasVacuas() +","+ String.valueOf(coordenadaVacua);
                                }

                                // genera adyacencias
                                terminosAdyacenciaNueva = terminosAdyacenciaMenor+"-"+terminosAdyacenciaMayor;
                                // genera representacion binaria
                                adyacenciaBinTemp = adyacenciaIndMayor.getValorBin();
                                adyacenciaBin = "";
                                // busca la nueva coordenada vacua a marcar
                                cv = new StringTokenizer(coordenadasVacuas, ",");
                                while(cv.hasMoreTokens())
                                {
                                    coordenadaVacua = Integer.parseInt(cv.nextToken());
                                }
                                for(int p=adyacenciaBinTemp.length()-1;p>=0;p--)
                                {
                                    // Posicion de la coordenada vacua
                                    if(p == QMCUtilLogica.log2(coordenadaVacua))
                                    {
                                        adyacenciaBin += "-";
                                    }
                                    else
                                    {
                                        adyacenciaBin += adyacenciaBinTemp.charAt(adyacenciaBinTemp.length()-1-p);
                                    }
                                }
                                // CREA NUEVO OBJETO ADYACENCIA
                                adyacenciaNueva = new QMCBinarioBean();
                                adyacenciaNueva.setValorDec(terminosAdyacenciaNueva);
                                adyacenciaNueva.setCoordenadasVacuas(coordenadasVacuas);
                                adyacenciaNueva.setValorBin(adyacenciaBin);
                                adyacenciaNueva.setIndice(adyacenciaIndMenor.getIndice());
                                // posiciones de los terminos/adyacencias anteriores que generan la adyacencia
                                adyacenciaNueva.setPosicion(String.valueOf(i)+","+String.valueOf(j));
                                // Introduce la nueva adyacencia en la lista
                                adyacenciasOrdenSuperior.add(adyacenciaNueva);
                                // Se ha creado nueva adyacencia
                                existenAdyacenciasSuperiores = true;
                            }


                        }
                        j++;
                    }
                    else if(adyacenciaIndMayor.getIndice()-adyacenciaIndMenor.getIndice() == 0)
                    {
                        j++;
                    }
                    else
                    {
                        break;
                    }
                }

            }
            // a�ade la lista de adyacencias de orden superior
            arrayListasAdyacencias.add(adyacenciasOrdenSuperior);
            // actualiza la lista de adyacencias anteriores usadas (checks)
            arrayListasAdyacencias.set(x,adyacencias);
            adyacencias = adyacenciasOrdenSuperior;
            adyacenciasOrdenSuperior = new ArrayList<QMCBinarioBean>();
            x++;
        }
        minimizable = true;
        if(arrayListasAdyacencias.size()==1)
        {
            minimizable = false;
        }

    }

    /**
     * M�todo que introduce la lista de adyacenciasActual.
     * @param orden El orden de la lista de adyacencias a establecer como actual.
     */
    public void setListaAdyacenciasActual(final int orden)
    {
        listaAdyacenciasActual = arrayListasAdyacencias.get(orden);
    }

    /**
     * @param listaAdyacenciasAnterior The listaAdyacenciasAnterior to set.
     */
    public void setListaAdyacenciasAnterior
      (final ArrayList<QMCBinarioBean> listaAdyacenciasAnterior)
    {
        this.listaAdyacenciasAnterior = listaAdyacenciasAnterior;
    }



    /**
     * M�todo que estable la lista de implicantes primos
     * @param terminos array de terminos.
     */
    public void setListaImplicantesPrimos(final String[] terminos)
    {
        ArrayList<?> listaAdyacencias;
        QMCBinarioBean adyacencia;
        QMCImplicanteBean implicante;

        listaAdyacencias = new ArrayList<Object>();
        listaImplicantesPrimos = new ArrayList<QMCImplicanteBean>();
        listaImplicantesSolucion = new ArrayList<QMCImplicanteBean>();
        char nombreImplicante = 65;

        //Object [] posiciones;
        int x, contador;
        boolean esencial;



        // CREACION DE LISTA DE IMPLICANTES PRIMOS
        // Recorre las listas empezando por las adyacencias mayores
        for(int i=arrayListasAdyacencias.size()-1;i>=0; i--)
        {
            listaAdyacencias =arrayListasAdyacencias.get(i);
            for(int j=listaAdyacencias.size()-1;j>=0;j--)
            {
                adyacencia = (QMCBinarioBean)listaAdyacencias.get(j);
                if(adyacencia.isUsada()==false)
                {
                    implicante = new QMCImplicanteBean();
                    implicante.setNombre(nombreImplicante);
                    implicante.setTerminos(adyacencia.getValorDec());
                    implicante.setPosicionesTerminos(QMCUtilLogica.generaArrayPosiciones(terminos,adyacencia.getValorDec()));
                    implicante.setValorBin(adyacencia.getValorBin());
                    implicante.setOrden(i);

                    listaImplicantesPrimos.add(implicante);
                    if(nombreImplicante < 90 || nombreImplicante > 223)
                    {
                    	nombreImplicante+=1;
                    }
                    // Aumenta alfabeto
                    else
                    {
                    	nombreImplicante = 224;
                    }

                }
            }
        }

        // MARCA IMPLICANTES PRIMOS ESENCIALES

        // Crea una matriz de implicantes primos que cubren los terminos
        matrizImplicantesEsenciales = QMCUtilFormateo.generaMatrizImplicantesEsenciales(listaImplicantesPrimos,terminos);

        // Determina los implicantes primos esenciales inspeccionando las columnas de la matriz
        for(int j=0;j<terminos.length && !terminosTodosCubiertos; j++)
        {
            // Inicializa el contador (cambio de columna)
            contador = 0;
            x = 0;
            esencial = true;
            for(int i=0;i<listaImplicantesPrimos.size()&& esencial;i++)
            {
                if(matrizImplicantesEsenciales[i][j]=="X")
                {
                    contador++;
                    x = i;
                }
                if(contador>1)
                {
                    esencial = false;
                }
            }
            if(esencial)
            {
                // Marca el implicante y actualiza la lista
                implicante = listaImplicantesPrimos.get(x);
                implicante.setEsencial(esencial);
                // Marca los terminos que quedan cubiertos
                implicante.marcaTerminosCubiertos(listaTerminosImplicantes, true);
                listaImplicantesPrimos.set(x,implicante);
                terminosTodosCubiertos = QMCUtilLogica.compruebaTerminosCubiertos(listaTerminosImplicantes);
            }
        }
        // INTRODUCE LOS IMPLICANTES ESENCIALES EN LA LISTA DE IMPLICANTES SOLUCION FINAL
        for(int i=0;i<listaImplicantesPrimos.size(); i++)
        {
            implicante = listaImplicantesPrimos.get(i);
            if(implicante.isEsencial())
            {
                listaImplicantesSolucion.add(implicante);
            }
        }

    }
    /**
     * Genera la lista de terminos sin indiferencias
     * @param terminos the listaTerminosImplicantes to set.
     */
    public void setListaTerminosImplicantes(final ArrayList<?> terminos)
    {
        listaTerminosImplicantes = new ArrayList<QMCBinarioBean>();
        QMCBinarioBean termino;
        for(int i=0;i<terminos.size(); i++)
        {
            termino = (QMCBinarioBean)terminos.get(i);
            if(termino.isIndiferencia() == false)
            {
                listaTerminosImplicantes.add(termino);
            }
        }
    }

    /**
     * M�todo que establece la lista de t�rminos no cubiertos
     */
    public void setListaTerminosNoCubiertos()
    {
        listaTerminosNoCubiertos = new ArrayList<QMCBinarioBean>();
        QMCBinarioBean termino;
        int x = 0;
        for(int i=0;i<listaTerminosImplicantes.size();i++)
        {
            // Construir un termino nuevo
            termino = (QMCBinarioBean)listaTerminosImplicantes.get(i).clone();

            if(!termino.isCubierta())
            {
                listaTerminosNoCubiertos.add(x,termino);
                x++;
            }
        }

    }

    /**
     * M�todo que establece la lista de implicantes reducida
     */
    public void setListaImplicantesReducida()
    {
        listaImplicantesReducida = new ArrayList<QMCImplicanteBean>();
        QMCImplicanteBean implicante;
        int x = 0;
        for(int i=0;i<listaImplicantesPrimos.size();i++)
        {
            implicante = (QMCImplicanteBean)listaImplicantesPrimos.get(i).clone();
            if(!implicante.isEsencial())
            {
                implicante.setPosicionesTerminos(QMCUtilLogica.generaArrayPosiciones(listaTerminosNoCubiertos.toArray(),implicante.getTerminos()));
                listaImplicantesReducida.add(x,implicante);
                x++;
            }
        }

        // Genera la matriz de implicantes reducida
        matrizImplicantesReducida = QMCUtilFormateo.generaMatrizImplicantesEsenciales(listaImplicantesReducida, listaTerminosNoCubiertos.toArray());

        // METODO DE PETRICK

        // Paso 1: Genera para cada termino la lista de los implicantes que los cubren
        ArrayList<ArrayList<String>> listaGruposImplicantes;
        ArrayList<String> grupoImplicantes;
        ArrayList<String> resultado;
        listaGruposImplicantes = new ArrayList<ArrayList<String>>();

        for(int j=0;j<matrizImplicantesReducida[0].length;j++)
        {
            // cambio de columna (termino)
            grupoImplicantes = new ArrayList<String>();
            for(int i=0;i<matrizImplicantesReducida.length; i++)
            {
                if(matrizImplicantesReducida[i][j]=="X")
                {
                    // A�ade el implicante(posicion) a la lista
                    grupoImplicantes.add(String.valueOf(listaImplicantesReducida.get(i).getNombre()));
                }
            }
            listaGruposImplicantes.add(grupoImplicantes);
        }

        // Paso 2: Multiplicacion booleana de todos los grupos (obtencion de soluciones)
        resultado = listaGruposImplicantes.get(0);
        for(int i=1; i<listaGruposImplicantes.size();i++)
        {
            resultado = QMCUtilLogica.multiplicaSumasBooleanas(resultado,
                                                               listaGruposImplicantes.get(i));
        }

        // Paso 3: An�lisis de la lista resultante en busca de las soluciones m�nimas
        // Establece el tama�o de las soluciones minimas
        int tamanoMin, tamano;
        tamanoMin = resultado.get(0).length();
        for(int i=1;i<resultado.size();i++)
        {
            tamano = resultado.get(i).length();
            if(tamanoMin>tamano)
            {
                tamanoMin = tamano;
            }
        }
        listaSolucionesMinimas = new ArrayList<String>();
        for(int i=0;i<resultado.size();i++)
        {
            tamano = resultado.get(i).length();
            if(tamano==tamanoMin)
            {
                listaSolucionesMinimas.add(resultado.get(i));
            }
        }
        // Marca tabla Implicantes reducida (solucion por defecto)
        final String solucion = listaSolucionesMinimas.get(0);
        for(int i=0; i<solucion.length();i++)
        {
            implicante = QMCUtilLogica.buscaImplicante(listaImplicantesReducida,solucion.charAt(i));
            implicante.setEsencial(true);
            implicante.marcaTerminosCubiertos(listaTerminosNoCubiertos, true);
        }
    }



    /**
     * Para una �nica Solucion minima (Modo interactivo)
     * @param cadenaSolucionMinima The listaSolucionesMinimas to set.
     */
    public void setListaSolucionesMinimas(final String cadenaSolucionMinima)
    {
        listaSolucionesMinimas = new ArrayList<String>();
        listaSolucionesMinimas.add(cadenaSolucionMinima);
    }

    /**
     * A�ade los implicantes de la cadena a la solucion final para marcar los terminos que cubren y generar la expresi�n algebraica
     * @param cadenaImplicantesSolucion The listaImplicantesSolucion to set.
     */
    public void setListaImplicantesSolucion(final String cadenaImplicantesSolucion)
    {
        QMCImplicanteBean implicante;
        int j,x, numEsenciales;
        boolean sobreescribe;

        j = 0;
        x = 0;
        numEsenciales = listaImplicantesPrimos.size()-listaImplicantesReducida.size();
        sobreescribe = false;
        if(numEsenciales<listaImplicantesSolucion.size())
        {
            sobreescribe = true;
        }

        // Resetea los valores de la solucion anterior
        for(int i=0;i<listaImplicantesReducida.size();i++)
        {
            implicante = listaImplicantesReducida.get(i);
            implicante.setEsencial(false);
            implicante.marcaTerminosCubiertos(listaTerminosNoCubiertos, false);
        }

        // Marca y carga los nuevos implicantes
        while(j<cadenaImplicantesSolucion.length())
        {
            implicante = QMCUtilLogica.buscaImplicante(listaImplicantesReducida, cadenaImplicantesSolucion.charAt(j));
            implicante.setEsencial(true);
            implicante.marcaTerminosCubiertos(listaTerminosNoCubiertos,true);
            // A�ade los implicantes no esenciales
            if(!sobreescribe)
            {
                listaImplicantesSolucion.add(implicante);
            }
            // Sobreescribe los implicantes no esenciales anteriores
            else
            {
                listaImplicantesSolucion.set(numEsenciales+x,implicante);
            }
            j+=4;
            x++;
        }
    }

    /**
     * M�todo que genera la lista de Implicantes interactivos
     */
    public void setListaImplicantesInteractivos(final ArrayList<?> listaImplicantes)
    {

        QMCImplicanteBean implicante, implicanteInteract;
        listaImplicantesInteractivos = new ArrayList<QMCImplicanteBean>();
        for(int i=0;i<listaImplicantes.size();i++)
        {
            implicante = (QMCImplicanteBean)listaImplicantes.get(i);
            implicanteInteract = new QMCImplicanteBean();
            implicanteInteract.setNombre(implicante.getNombre());
            implicanteInteract.setPosicionesTerminos(implicante.getPosicionesTerminos());
            listaImplicantesInteractivos.add(implicanteInteract);
        }
    }

    /**
     * M�todo que genera un ArrayList de t�rminos interactivos
     */
    public void setListaTerminosInteractivos(final ArrayList<?> listaTerminos)
    {

        QMCBinarioBean termino, terminoInteract;
        listaTerminosInteractivos = new ArrayList<QMCBinarioBean>();
        for(int i=0;i<listaTerminos.size();i++)
        {
            termino = (QMCBinarioBean)listaTerminos.get(i);
            terminoInteract = new QMCBinarioBean();
            terminoInteract.setValorDec(termino.getValorDec());
            listaTerminosInteractivos.add(terminoInteract);
        }
    }

    /**
     * Devuelve un ArrayList de ArrayList de adyacencias
     * @return arrayListasAdyacencias.
     */
    public ArrayList<ArrayList<QMCBinarioBean>> getArrayListasAdyacencias() {
        return arrayListasAdyacencias;
    }


    /**
     * Devuelve un ArrayList de adyacencias actual
     * @return listaAdyacenciasActual.
     */
    public ArrayList<QMCBinarioBean> getListaAdyacenciasActual() {
        return listaAdyacenciasActual;
    }

    /**
     * Devuelve un ArrayList de implicantes primos
     * @return listaImplicantes.
     */
    public ArrayList<QMCImplicanteBean> getListaImplicantesPrimos() {
        return listaImplicantesPrimos;
    }

    /**
     * Metodo que devuelve la matriz de implicantes primos
     * @return matrizImplicantesEsenciales
     */
    public Object [][] getMatrizImplicantesEsenciales()
    {
        return matrizImplicantesEsenciales;
    }

    /**
     * Devuelve un ArrayList de terminos objeto de implicantes
     * @return listaTerminosImplicantes
     */
    public ArrayList<QMCBinarioBean> getListaTerminosImplicantes(){
        return listaTerminosImplicantes;
    }

    /**
     * Devuelve un ArrayList de terminos no cubiertos
     * @return listaTerminosNoCubiertos.
     */
    public ArrayList<QMCBinarioBean> getListaTerminosNoCubiertos() {
        return listaTerminosNoCubiertos;
    }


    /**
     * Devuelve un ArrayList de implicantes reducida
     * @return listaImplicantesReducida.
     */
    public ArrayList<QMCImplicanteBean> getListaImplicantesReducida() {
        return listaImplicantesReducida;
    }


    /**
     * Devuelve la matriz de implicantes reducida
     * @return matrizImplicantesReducida.
     */
    public Object[][] getMatrizImplicantesReducida() {
        return matrizImplicantesReducida;
    }

    /**
     * devuelve el estado de los terminos
     * @return terminosTodosCubiertos
     */
    public boolean isTerminosTodosCubiertos()
    {
        return terminosTodosCubiertos;
    }

    /**
     * devuelve el estado determinado de capacidad de minimizacion
     * @return minimizable
     */
    public boolean isMinimizable()
    {
        return minimizable;
    }

    /**
     * Devuelve un ArrayList de soluciones minimas
     * @return listaSolucionesMinimas.
     */
    public ArrayList<String> getListaSolucionesMinimas() {
        return listaSolucionesMinimas;
    }

    /**
     * Devueve un arrayList de implicantes pertenecientes a la solucion
     * @return  listaImplicantesSolucion.
     */
    public ArrayList<QMCImplicanteBean> getListaImplicantesSolucion() {
        return listaImplicantesSolucion;
    }

    /**
     * Devuelve un ArrayList de objetos implicantes vacios
     * @return listaImplicantesInteractivos.
     */
    public ArrayList<QMCImplicanteBean> getListaImplicantesInteractivos() {
        return listaImplicantesInteractivos;
    }

    /**
     * Devuelve un ArrayList de objetos termino vacios
     * @return listaTerminosInteractivos.
     */
    public ArrayList<QMCBinarioBean> getListaTerminosInteractivos() {
        return listaTerminosInteractivos;
    }

    /**
     * Devuelve un ArrayList de adyacencias de orden anterior
     * @return listaAdyacenciasAnterior.
     */
    public ArrayList<QMCBinarioBean> getListaAdyacenciasAnterior() {
        return listaAdyacenciasAnterior;
    }











}
