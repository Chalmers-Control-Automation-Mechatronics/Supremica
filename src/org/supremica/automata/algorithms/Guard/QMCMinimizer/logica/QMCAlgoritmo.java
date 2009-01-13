/**
 * Paquete que contiene las clases de la logica de la aplicación
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilLogica;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilFormateo;




/**
 *  Clase que contiene los algoritmos de cada paso del metodo Quine-McCluskey
 *  @author Pedro Sanz
 *  Fecha 13/02/2006
 *
 */
public class QMCAlgoritmo {



    private ArrayList arrayListasAdyacencias;
    private ArrayList listaAdyacenciasActual;
    private ArrayList listaAdyacenciasAnterior;

    private ArrayList listaImplicantesPrimos;
    private ArrayList listaImplicantesReducida;

    private ArrayList listaTerminosImplicantes;
    private ArrayList listaTerminosNoCubiertos;

    private ArrayList listaSolucionesMinimas;
    private ArrayList listaImplicantesSolucion;

    private Object [][] matrizImplicantesEsenciales;
    private Object [][] matrizImplicantesReducida;

    private ArrayList listaImplicantesInteractivos;
    private ArrayList listaTerminosInteractivos;

    private boolean terminosTodosCubiertos;
    private boolean minimizable;






    /**
     * Metodo que establece un array con las listas de adyacencias marcadas de todos los ordenes
     * @param terminos lista de terminos a partir de los cuales se generaran las diversas listas de adyacencias.
     */
    public void setArrayListasAdyacencias(ArrayList terminos)
    {
        ArrayList adyacencias, adyacenciasOrdenSuperior;

        QMCBinarioBean adyacenciaIndMenor, adyacenciaIndMayor;
        QMCBinarioBean adyacenciaNueva;


        String adyacenciaBinTemp, adyacenciaBin;
        String terminosAdyacenciaMayor, terminosAdyacenciaMenor, terminosAdyacenciaNueva, coordenadasVacuas;
        StringTokenizer termsMenor, termsMayor, cv;
        int primerTerminoMayor, primerTerminoMenor, ultimoTerminoMenor, coordenadaVacua;
        int j,x;
        boolean existenAdyacenciasSuperiores;

        arrayListasAdyacencias = new ArrayList();
        existenAdyacenciasSuperiores = true;
        x = 0;
        adyacencias = new ArrayList(QMCUtilLogica.ordenaArrayListTerminos(terminos));


        while(existenAdyacenciasSuperiores)
        {

            adyacenciasOrdenSuperior = new ArrayList();
            existenAdyacenciasSuperiores = false;
            for(int i=0;i<adyacencias.size();i++)
            {
                adyacenciaIndMenor = (QMCBinarioBean)adyacencias.get(i);
                j = i+1;
                while(j<adyacencias.size())
                {
                    adyacenciaIndMayor = (QMCBinarioBean)adyacencias.get(j);

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
            // añade la lista de adyacencias de orden superior
            arrayListasAdyacencias.add(adyacenciasOrdenSuperior);
            // actualiza la lista de adyacencias anteriores usadas (checks)
            arrayListasAdyacencias.set(x,adyacencias);
            adyacencias = adyacenciasOrdenSuperior;
            adyacenciasOrdenSuperior = new ArrayList();
            x++;
        }
        minimizable = true;
        if(arrayListasAdyacencias.size()==1)
        {
            minimizable = false;
        }

    }

    /**
     * Método que introduce la lista de adyacenciasActual.
     * @param orden El orden de la lista de adyacencias a establecer como actual.
     */
    public void setListaAdyacenciasActual(int orden)
    {
        listaAdyacenciasActual = (ArrayList)arrayListasAdyacencias.get(orden);
    }

    /**
     * @param listaAdyacenciasAnterior The listaAdyacenciasAnterior to set.
     */
    public void setListaAdyacenciasAnterior(ArrayList listaAdyacenciasAnterior) {
        this.listaAdyacenciasAnterior = listaAdyacenciasAnterior;
    }



    /**
     * Método que estable la lista de implicantes primos
     * @param array de terminos.
     */
    public void setListaImplicantesPrimos(String[] terminos)
    {
        ArrayList listaAdyacencias;
        QMCBinarioBean adyacencia;
        QMCImplicanteBean implicante;

        listaAdyacencias = new ArrayList();
        listaImplicantesPrimos = new ArrayList();
        listaImplicantesSolucion = new ArrayList();
        char nombreImplicante = 65;

        //Object [] posiciones;
        int x, contador;
        boolean esencial;



        // CREACION DE LISTA DE IMPLICANTES PRIMOS
        // Recorre las listas empezando por las adyacencias mayores
        for(int i=arrayListasAdyacencias.size()-1;i>=0; i--)
        {
            listaAdyacencias =(ArrayList)arrayListasAdyacencias.get(i);
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
                implicante = (QMCImplicanteBean)listaImplicantesPrimos.get(x);
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
            implicante = (QMCImplicanteBean)listaImplicantesPrimos.get(i);
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
    public void setListaTerminosImplicantes(ArrayList terminos)
    {
        listaTerminosImplicantes = new ArrayList();
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
     * Método que establece la lista de términos no cubiertos
     * @param listaTerminosNoCubiertos The listaTerminosNoCubiertos to set.
     */
    public void setListaTerminosNoCubiertos()
    {
        listaTerminosNoCubiertos = new ArrayList();
        QMCBinarioBean termino;
        int x = 0;
        for(int i=0;i<listaTerminosImplicantes.size();i++)
        {
            // Construir un termino nuevo
            termino = (QMCBinarioBean)((QMCBinarioBean)listaTerminosImplicantes.get(i)).clone();

            if(!termino.isCubierta())
            {
                listaTerminosNoCubiertos.add(x,termino);
                x++;
            }
        }

    }

    /**
     * Método que establece la lista de implicantes reducida
     * @param listaImplicantesReducida The listaImplicantesReducida to set.
     */
    public void setListaImplicantesReducida()
    {
        listaImplicantesReducida = new ArrayList();
        QMCImplicanteBean implicante;
        int x = 0;
        for(int i=0;i<listaImplicantesPrimos.size();i++)
        {
            implicante = (QMCImplicanteBean)((QMCImplicanteBean)listaImplicantesPrimos.get(i)).clone();
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
        ArrayList listaGruposImplicantes, grupoImplicantes, resultado;
        listaGruposImplicantes = new ArrayList();

        for(int j=0;j<matrizImplicantesReducida[0].length;j++)
        {
            // cambio de columna (termino)
            grupoImplicantes = new ArrayList();
            for(int i=0;i<matrizImplicantesReducida.length; i++)
            {
                if(matrizImplicantesReducida[i][j]=="X")
                {
                    // Añade el implicante(posicion) a la lista
                    grupoImplicantes.add(String.valueOf(((QMCImplicanteBean)listaImplicantesReducida.get(i)).getNombre()));
                }
            }
            listaGruposImplicantes.add(grupoImplicantes);
        }

        // Paso 2: Multiplicacion booleana de todos los grupos (obtencion de soluciones)
        resultado = (ArrayList)listaGruposImplicantes.get(0);
        for(int i=1; i<listaGruposImplicantes.size();i++)
        {
            resultado = (ArrayList)QMCUtilLogica.multiplicaSumasBooleanas(resultado,(ArrayList)listaGruposImplicantes.get(i));
        }

        // Paso 3: Análisis de la lista resultante en busca de las soluciones mínimas
        // Establece el tamaño de las soluciones minimas
        int tamanoMin, tamano;
        tamanoMin = ((String)resultado.get(0)).length();
        for(int i=1;i<resultado.size();i++)
        {
            tamano = ((String)resultado.get(i)).length();
            if(tamanoMin>tamano)
            {
                tamanoMin = tamano;
            }
        }
        listaSolucionesMinimas = new ArrayList();
        for(int i=0;i<resultado.size();i++)
        {
            tamano = ((String)resultado.get(i)).length();
            if(tamano==tamanoMin)
            {
                listaSolucionesMinimas.add(resultado.get(i));
            }
        }
        // Marca tabla Implicantes reducida (solucion por defecto)
        String solucion = (String)listaSolucionesMinimas.get(0);
        for(int i=0; i<solucion.length();i++)
        {
            implicante = QMCUtilLogica.buscaImplicante(listaImplicantesReducida,solucion.charAt(i));
            implicante.setEsencial(true);
            implicante.marcaTerminosCubiertos(listaTerminosNoCubiertos, true);
        }
    }



    /**
     * Para una única Solucion minima (Modo interactivo)
     * @param listaSolucionesMinimas The listaSolucionesMinimas to set.
     */
    public void setListaSolucionesMinimas(String cadenaSolucionMinima)
    {
        listaSolucionesMinimas = new ArrayList();
        listaSolucionesMinimas.add(cadenaSolucionMinima);
    }

    /**
     * Añade los implicantes de la cadena a la solucion final para marcar los terminos que cubren y generar la expresión algebraica
     * @param listaImplicantesSolucion The listaImplicantesSolucion to set.
     */
    public void setListaImplicantesSolucion(String cadenaImplicantesSolucion)
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
            implicante = (QMCImplicanteBean)listaImplicantesReducida.get(i);
            implicante.setEsencial(false);
            implicante.marcaTerminosCubiertos(listaTerminosNoCubiertos, false);
        }

        // Marca y carga los nuevos implicantes
        while(j<cadenaImplicantesSolucion.length())
        {
            implicante = QMCUtilLogica.buscaImplicante(listaImplicantesReducida, cadenaImplicantesSolucion.charAt(j));
            implicante.setEsencial(true);
            implicante.marcaTerminosCubiertos(listaTerminosNoCubiertos,true);
            // Añade los implicantes no esenciales
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
     * Método que genera la lista de Implicantes interactivos
     * @param listaImplicantesInteractivos The listaImplicantesInteractivos to set.
     */
    public void setListaImplicantesInteractivos(ArrayList listaImplicantes)
    {

        QMCImplicanteBean implicante, implicanteInteract;
        listaImplicantesInteractivos = new ArrayList();
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
     * Método que genera un ArrayList de términos interactivos
     * @param listaTerminosInteractivos The listaTerminosInteractivos to set.
     */
    public void setListaTerminosInteractivos(ArrayList listaTerminos)
    {

        QMCBinarioBean termino, terminoInteract;
        listaTerminosInteractivos = new ArrayList();
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
    public ArrayList getArrayListasAdyacencias() {
        return arrayListasAdyacencias;
    }


    /**
     * Devuelve un ArrayList de adyacencias actual
     * @return listaAdyacenciasActual.
     */
    public ArrayList getListaAdyacenciasActual() {
        return listaAdyacenciasActual;
    }

    /**
     * Devuelve un ArrayList de implicantes primos
     * @return listaImplicantes.
     */
    public ArrayList getListaImplicantesPrimos() {
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
    public ArrayList getListaTerminosImplicantes(){
        return listaTerminosImplicantes;
    }

    /**
     * Devuelve un ArrayList de terminos no cubiertos
     * @return listaTerminosNoCubiertos.
     */
    public ArrayList getListaTerminosNoCubiertos() {
        return listaTerminosNoCubiertos;
    }


    /**
     * Devuelve un ArrayList de implicantes reducida
     * @return listaImplicantesReducida.
     */
    public ArrayList getListaImplicantesReducida() {
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
    public ArrayList getListaSolucionesMinimas() {
        return listaSolucionesMinimas;
    }

    /**
     * Devueve un arrayList de implicantes pertenecientes a la solucion
     * @return  listaImplicantesSolucion.
     */
    public ArrayList getListaImplicantesSolucion() {
        return listaImplicantesSolucion;
    }

    /**
     * Devuelve un ArrayList de objetos implicantes vacios
     * @return listaImplicantesInteractivos.
     */
    public ArrayList getListaImplicantesInteractivos() {
        return listaImplicantesInteractivos;
    }

    /**
     * Devuelve un ArrayList de objetos termino vacios
     * @return listaTerminosInteractivos.
     */
    public ArrayList getListaTerminosInteractivos() {
        return listaTerminosInteractivos;
    }

    /**
     * Devuelve un ArrayList de adyacencias de orden anterior
     * @return listaAdyacenciasAnterior.
     */
    public ArrayList getListaAdyacenciasAnterior() {
        return listaAdyacenciasAnterior;
    }











}
