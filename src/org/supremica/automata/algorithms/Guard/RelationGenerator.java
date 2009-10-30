/*
 * RelationGenerator.java
 *
 * Created on June 30, 2008, 2:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard;

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.model.des.StateProxy;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCModeloTablaVerdad;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCAlgoritmo;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCFuncion;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilFormateo;


/**
 *
 * @author Sajed
 */
public class RelationGenerator {
    
    Automata input;
    Automaton supervisor;
    TreeMap<String,String>[] stateBinr;    
    TreeMap<String,String>[] binrState;
    String sepTerm = ",";
    String nameStateSep = ".";
    
    String allwMinTerm = "";
    String allwMaxTerm = "";
    String frbMinTerm = "";
    String frbMaxTerm = "";
    StringBuffer DCs = new StringBuffer();
    
    String variables = "";
    
    MinMaxDC mmdc = new MinMaxDC();
    
    int binCodeLength = 0;
    
    String [][] boolVars;
    /** Creates a new instance of RelationGenerator */
    @SuppressWarnings("unchecked")
    public RelationGenerator(Automata input) {
        this.input = input;
        try{ this.supervisor = getSupervisor();}catch(Exception e){}
        int nbrOfAutomata = input.size();
        stateBinr = new TreeMap[nbrOfAutomata];
        binrState = new TreeMap[nbrOfAutomata];
        boolVars = new String[nbrOfAutomata][];
        for(int i = 0; i < nbrOfAutomata; i++)
        {
            stateBinr[i] = new TreeMap<String,String>();
            binrState[i] = new TreeMap<String,String>();
            Automaton currAut = input.getAutomatonAt(i);
            Iterator<State> stItr = currAut.stateIterator();
            int nbrOfBits = (int)(Math.ceil((Math.log(currAut.nbrOfStates())/Math.log(2))));
            boolVars[i] = new String[nbrOfBits];
            for(int j=0;j<boolVars[i].length;j++)
            {
                boolVars[i][j] = currAut.getName()+"_"+j+"#";
                variables += boolVars[i][j]+sepTerm;
            }
            
            int index = 0;
            while(stItr.hasNext())
            {
                String currStateName = stItr.next().getName();
                String binIndex = zeroPad(Integer.toBinaryString(index),nbrOfBits);
                stateBinr[i].put(currStateName,binIndex);
                binrState[i].put(binIndex,currStateName);
                index++;
            }
        }
        variables = variables.substring(0,variables.length()-1);
    }
    
    public String getGuard(LabeledEvent event, boolean AllwFrbd)
    {
        String output = "";
        
        computeMinMaxTerms(event);
        if((AllwFrbd && mmdc.getMinTerm().size()>0) || (!AllwFrbd && mmdc.getMaxTerm().size()>0))
        {
            computeDontCares(mmdc.getMinTerm(),mmdc.getMaxTerm());
            
            String minVarExpression = QM_Minimizer(variables,generateMinimizerInput(mmdc.getMaxTerm()),generateMinimizerInput(mmdc.getDCs()));
            
            int nbrAutomata = input.size();
            String[][][] varToStateName = new String[2][nbrAutomata][];
            for(int i=0;i<nbrAutomata;i++)
            {
                varToStateName[0][i] = new String[boolVars[i].length];
                varToStateName[1][i] = new String[boolVars[i].length];
                for(int j=0;j<boolVars[i].length;j++)
                {
                    Iterator<String> it = binrState[i].keySet().iterator();
                    String key = "";
                    varToStateName[0][i][j] = "";
                    varToStateName[1][i][j] = "";
                    while(it.hasNext())
                    {
                        key = it.next();

                        if(key.charAt(j) == '0')
                            varToStateName[0][i][j] += (binrState[i].get(key) + "+");
                        else
                            varToStateName[1][i][j] += (binrState[i].get(key) + "+");
                    }
                    varToStateName[0][i][j] = varToStateName[0][i][j].substring(0,varToStateName[0][i][j].length()-1);
                    varToStateName[1][i][j] = varToStateName[1][i][j].substring(0,varToStateName[1][i][j].length()-1);
                }     
            }
            StringTokenizer st = new StringTokenizer(minVarExpression,"+");
            while(st.hasMoreTokens())
            {
                String stateTerm = "";
                String currTerm = st.nextToken().trim();
                while(!currTerm.equals(""))
                {
                    int nextIndex = currTerm.indexOf("#")+1;
                    String termToken = currTerm.substring(0,nextIndex);
                    int[] aIDvID = AutID_VarID(termToken);
                    if(nextIndex != termToken.length())
                    {
                        if((termToken.charAt(nextIndex)+"").equals("'"))
                        {
                            nextIndex++;
                             if(AllwFrbd)
                                 stateTerm += "(Q"+input.getAutomatonAt(aIDvID[0]).getName()+"=="+varToStateName[0][aIDvID[0]][aIDvID[1]]+")";
                             else
                                 stateTerm += "(Q"+input.getAutomatonAt(aIDvID[0]).getName()+"!="+varToStateName[0][aIDvID[0]][aIDvID[1]]+")";
                        }
                        else
                        {
                             if(AllwFrbd)
                                 stateTerm += "(Q"+input.getAutomatonAt(aIDvID[0]).getName()+"=="+varToStateName[1][aIDvID[0]][aIDvID[1]]+")";
                             else
                                 stateTerm += "(Q"+input.getAutomatonAt(aIDvID[0]).getName()+"!="+varToStateName[1][aIDvID[0]][aIDvID[1]]+")";
                        }
                    }
                    else
                    {
                         if(AllwFrbd)
                             stateTerm += "(Q"+input.getAutomatonAt(aIDvID[0]).getName()+"=="+varToStateName[1][aIDvID[0]][aIDvID[1]]+")";
                         else
                             stateTerm += "(Q"+input.getAutomatonAt(aIDvID[0]).getName()+"!="+varToStateName[1][aIDvID[0]][aIDvID[1]]+")";
                    }
                    currTerm = currTerm.substring(nextIndex);
                }
                output += stateTerm + "+";
            }
            output = output.substring(0,output.length()-1);
            
        }
        else
        {
            output = "No guard required for event "+event.getName();
        }
        
        return output;
    }
    
    public String generateMinimizerInput(Set<Integer> input)
    {
        Iterator<Integer> it = input.iterator();
        StringBuffer output = new StringBuffer();
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
    
    public int[] AutID_VarID(String currTerm)
    {
        int[] output = new int[2];
        for(int i=0;i<boolVars.length;i++)
        {
            for(int j=0;j<boolVars[i].length;j++)
            {
                if(boolVars[i][j].equals(currTerm))
                {
                    output[0] = i;
                    output[1] = j;
                    return output; 
                }    
            }
        }
        
        return output;
    }
    
    public String zeroPad(String s, int nbrBits)
    {
        while(s.length() < nbrBits)
            s = "0"+s;
        
        return s;
    }
    
    public void computeMinMaxTerms(LabeledEvent event)
    {
        TreeSet<Integer> minTerms = new TreeSet<Integer>();
        TreeSet<Integer> maxTerms = new TreeSet<Integer>();
        
        Set<StateProxy> allwStates = new TreeSet<StateProxy>();
        Set<StateProxy> frbStates = new TreeSet<StateProxy>();
        Set<StateProxy>[] AFStates = getMustAFStates(event);
        
        allwStates = AFStates[0];
        frbStates = AFStates[1];
        
        Iterator<StateProxy> allwIt = allwStates.iterator();
        Iterator<StateProxy> frbIt = frbStates.iterator();
        while(allwIt.hasNext())
        {
            String currState = allwIt.next().getName();
            String binCode = buildBinaryCode(currState);
            binCodeLength = binCode.length();
            minTerms.add(binary2Decimal(binCode));
            
        }
        while(frbIt.hasNext())
        {
            String currState = frbIt.next().getName();
            String binCode = buildBinaryCode(currState);
            maxTerms.add(binary2Decimal(binCode));
        }
        
        mmdc.setMinTerm(minTerms);
        mmdc.setMaxTerm(maxTerms);
    }
    
    public void computeDontCares(TreeSet<Integer> minTerms, TreeSet<Integer> maxTerms)
    {
        TreeSet<Integer> dntCrs = new TreeSet<Integer>();
        for(int i=0;i<(int)Math.pow(2,binCodeLength);i++)
        {
            if(!minTerms.contains(i) && !maxTerms.contains(i))
            {
                dntCrs.add(i);
            }
        }
        
        mmdc.setDCs(dntCrs);
    }
    
    public String QM_Minimizer(String vars, String minTerms, String dontCares)
    {
        String output = "";
        int contador = 0;
	QMCAlgoritmo algoritmo = new QMCAlgoritmo();
	QMCFuncion funcion = new QMCFuncion();

	funcion.setVariables(vars);
	funcion.setTerminos(minTerms);
	funcion.setIndiferencias(dontCares);
	funcion.setForma('s');

	QMCModeloTablaVerdad modeloTablaVerdad = new QMCModeloTablaVerdad(QMCUtilFormateo.generaCabeceraTablaVerdad(funcion.getVariables()),QMCUtilFormateo.generaDatosTablaVerdad(funcion.getNumVariables(),funcion.getForma(),funcion.getTerminos(),funcion.getIndiferencias()));
	funcion.setListaBinarios(modeloTablaVerdad.getDataVector());

	algoritmo.setArrayListasAdyacencias(funcion.getListaTerminos());
	algoritmo.setListaAdyacenciasAnterior(algoritmo.getListaAdyacenciasActual());
	algoritmo.setListaAdyacenciasActual(contador);

        if(!algoritmo.isMinimizable())
        {
                System.out.println("No existen adyacencias para esta función de conmutación, no se puede minimizar");
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
    
    public int binary2Decimal(String bin)
    {
        int decimal=0;
        int binLength = bin.length();
        for(int i=0;i<binLength;i++)
        {
            decimal += (int)(Integer.valueOf(""+bin.charAt(i))*Math.pow(2,binLength-i-1));
        }
        
        return decimal;
    }
    
    public String buildBinaryCode(String state)
    {
        String binaryCode = "";
        StringTokenizer st = new StringTokenizer(state,nameStateSep);
        while(st.hasMoreTokens())
        {
            String subState = st.nextToken();
            binaryCode += getBinaryValue(subState);   
        }
        
        return binaryCode;
    }
    
    public String getBinaryValue(String subState)
    {
        String bv = "";
        for(int i=0;i<stateBinr.length;i++)
        {
           String value = stateBinr[i].get(subState);
           if(value != null)
           {
               bv = value;
               break;
           }
        }
        
        return bv;
    }
    public Automaton getSupervisor() throws Exception
    {
        SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
        synthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKINGCONTROLLABLE);
        synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC);
        synthesizerOptions.setPurge(false);
        synthesizerOptions.setMaximallyPermissive(true);
        synthesizerOptions.setMaximallyPermissiveIncremental(true);
        
        AutomataSynthesizer synthesizer;
        synthesizer = new AutomataSynthesizer(input, SynchronizationOptions.getDefaultSynthesisOptions(), synthesizerOptions);
        Automaton supervisor = synthesizer.execute().getFirstAutomaton();
        
        return supervisor;
    }
    
    @SuppressWarnings("unchecked")
    public Set<StateProxy>[] getMustAFStates(LabeledEvent event)
    {
        TreeSet<StateProxy>[] output = new TreeSet[2];
        TreeSet<StateProxy> allowed = new TreeSet<StateProxy>();
        TreeSet<StateProxy> forbidden = new TreeSet<StateProxy>();
        for(Iterator<Arc> arcIt = supervisor.arcIterator(); arcIt.hasNext(); )
        {
            Arc arc = arcIt.next();
            if(arc.getEvent().getName().equals(event.getName()) && !arc.getSource().isForbidden())
            {
                if(arc.getTarget().isForbidden())
                {
                    forbidden.add(arc.getSource());
                }
                else
                {
                    allowed.add(arc.getSource());
                }
            }
        }
        output[0] = allowed;
        output[1] = forbidden;
     
        return output;
    }

}
