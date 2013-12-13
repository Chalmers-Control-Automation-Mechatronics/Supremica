/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard;

import java.util.Iterator;
import java.util.List;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCModeloTablaVerdad;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCAlgoritmo;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCFuncion;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilFormateo;

/**
 *
 * @author Sajed Miremadi
 */
public class QMCMinimizerSupremica
{

    String sepTerm = ",";
    List<String> variables;
    List<Integer> minTerms;
    List<Integer> dontCares;

    public QMCMinimizerSupremica(final List<String> variables, final List<Integer> minTerms, final List<Integer> dontCares)
    {
        this.variables = variables;
        this.minTerms = minTerms;
        this.dontCares = dontCares;
    }

    public String minimize()
    {
        return QM_minimize(generateMinimizerInput(variables), generateMinimizerInput(minTerms),generateMinimizerInput(dontCares));
    }

    public String QM_minimize(final String vars, final String minTerms, final String dontCares)
    {
        String output = "";
        int contador = 0;
	final QMCAlgoritmo algoritmo = new QMCAlgoritmo();
	final QMCFuncion funcion = new QMCFuncion();

	funcion.setVariables(vars);
        String[] terminos = null;
        String[] indiferencias = null;
        if(!minTerms.isEmpty())
        {
            funcion.setTerminos(minTerms);
            terminos = funcion.getTerminos();
        }
        if(!dontCares.isEmpty())
        {
            funcion.setIndiferencias(dontCares);
            indiferencias = funcion.getIndiferencias();
        }
	funcion.setForma('s');

	final QMCModeloTablaVerdad modeloTablaVerdad = new QMCModeloTablaVerdad(QMCUtilFormateo.generaCabeceraTablaVerdad(funcion.getVariables()),QMCUtilFormateo.generaDatosTablaVerdad(funcion.getNumVariables(),funcion.getForma(),terminos,indiferencias));
	funcion.setListaBinarios(modeloTablaVerdad.getDataVector());

	algoritmo.setArrayListasAdyacencias(funcion.getListaTerminos());
	algoritmo.setListaAdyacenciasAnterior(algoritmo.getListaAdyacenciasActual());
	algoritmo.setListaAdyacenciasActual(contador);

        if(!algoritmo.isMinimizable())
        {
                System.out.println("No existen adyacencias para esta funcion de conmutacion, no se puede minimizar");
        }
        contador++;
        while(contador<algoritmo.getArrayListasAdyacencias().size())
        {
                algoritmo.setListaAdyacenciasActual(contador);
                contador++;
        }

        algoritmo.setListaTerminosImplicantes(funcion.getListaTerminos());
        algoritmo.setListaImplicantesPrimos(funcion.getTerminos());

        if(!algoritmo.isTerminosTodosCubiertos())
        {
                algoritmo.setListaTerminosNoCubiertos();
                algoritmo.setListaImplicantesReducida();
        }

        String [] lis ;
        if(!algoritmo.isTerminosTodosCubiertos())
        {
            lis = QMCUtilFormateo.generaArrayImplicantesNoEsenciales(algoritmo.getListaSolucionesMinimas(), funcion.getForma());
            algoritmo.setListaImplicantesSolucion(lis[0]);
	}

        output = QMCUtilFormateo.generaExpresionBooleana(funcion.getVariables(),algoritmo.getListaImplicantesSolucion(),funcion.getForma());
//	System.out.println(output);

        output = output.substring(output.indexOf("=")+1);

        return output;
//        return "R2_0#";
    }

    public String generateMinimizerInput(final List<?> input)
    {
        final Iterator<?> it = input.iterator();
        final StringBuilder output = new StringBuilder();
        boolean needSeparator = false;
        while(it.hasNext())
        {
                if (needSeparator)
                {
                    output.append(sepTerm);
                }
                else
                {
                    needSeparator = true;
                }
                output.append(it.next());
        }
        return output.toString();
    }

}

