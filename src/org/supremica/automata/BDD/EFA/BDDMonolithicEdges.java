
package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */
import org.supremica.automata.BDD.EFA.BDDExtendedAutomaton;
import org.supremica.automata.BDD.EFA.BDDExtendedAutomata;
import net.sf.javabdd.*;
import org.supremica.log.*;
import org.supremica.util.SupremicaException;
import java.util.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

public class BDDMonolithicEdges
    implements BDDEdges
{
    BDDExtendedAutomata bddExAutomata;
    BDDExtendedManager manager;
    BDD edgesForwardBDD = null;
    BDD edgesBackwardBDD = null;

    BDD myEdgesForwardBDD = null;
    BDD myEdgesBackwardBDD = null;

    /** Creates a new instance of BDDMonolithicEdges */
    public BDDMonolithicEdges(BDDExtendedAutomata bddExAutomata)
    {
        this.bddExAutomata = bddExAutomata;
        manager = bddExAutomata.getBDDManager();

        edgesForwardBDD = manager.getOneBDD();
        edgesBackwardBDD = manager.getOneBDD();

/*        for (BDDExtendedAutomaton currAutomaton : bddExAutomata)
        {
            edgesForwardBDD = edgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());
            edgesBackwardBDD = edgesBackwardBDD.and(currAutomaton.getEdgeBackwardBDD());
        }
*/
        
        BDD newEdgesForwardBDD = computeSynchronizedEdges(bddExAutomata.forwardTransWhereVisUpdated, bddExAutomata.forwardTransAndNextValsForV);
//        BDD newEdgesBackwardBDD = computeSynchronizedEdges(bddExAutomata.backwardTransWhereVisUpdated, bddExAutomata.backwardTransAndNextValsForV);
        
        BDD newEdgesBackwardBDD = edgesForwardBDD.replace(bddExAutomata.sourceToTempLocationPairing);
        newEdgesBackwardBDD = newEdgesBackwardBDD.replace(bddExAutomata.destToSourceLocationPairing);
        newEdgesBackwardBDD = newEdgesBackwardBDD.replace(bddExAutomata.tempToDestLocationPairing);

        newEdgesBackwardBDD = newEdgesBackwardBDD.replace(bddExAutomata.sourceToTempVariablePairing);
        newEdgesBackwardBDD = newEdgesBackwardBDD.replace(bddExAutomata.destToSourceVariablePairing);
        newEdgesBackwardBDD = newEdgesBackwardBDD.replace(bddExAutomata.tempToDestVariablePairing);

        myEdgesForwardBDD = newEdgesForwardBDD;//edgesForwardBDD;
        myEdgesBackwardBDD = newEdgesBackwardBDD;//edgesBackwardBDD;

        edgesForwardBDD = newEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());//edgesForwardBDD.exist(bddExAutomata.getEventVarSet());
        edgesBackwardBDD = newEdgesBackwardBDD.exist(bddExAutomata.getEventVarSet());//edgesBackwardBDD.exist(bddExAutomata.getEventVarSet());
    }

    public BDD computeSynchronizedEdges(BDD[][] inTransWhereVisUpdated, BDD[][] inTransAndNextValsForV)
    {
        BDD[] transWhereVisUpdated = inTransWhereVisUpdated[0];
        BDD[] transAndNextValsForV = inTransAndNextValsForV[0];

        for(int i = 1 ; i< (bddExAutomata.theExAutomata.size());i++)
        {
            BDD[] currTransWhereVisUpdated = inTransWhereVisUpdated[i];
            BDD[] currTransAndNextValsForV = inTransAndNextValsForV[i];
            for(VariableComponentProxy var:bddExAutomata.orgExAutomata.getVars())
            {
                int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                BDD tUpdate = transWhereVisUpdated[varIndex];
                BDD ctUpdate = currTransWhereVisUpdated[varIndex];
                BDD tNextVal = transAndNextValsForV[varIndex];
                BDD ctNextVal = currTransAndNextValsForV[varIndex];

                BDD noneUpdated = tUpdate.not().and(ctUpdate.not());
                BDD nextTransAndNextValsForV_00 = noneUpdated.and(varEqualToVar(var.getName()));

                BDD firstUpdated = tUpdate.and(ctUpdate.not());
                BDD nextTransAndNextValsForV_01 = firstUpdated.and(ctNextVal);

                BDD secondUpdated = tUpdate.not().and(ctUpdate);
                BDD nextTransAndNextValsForV_10 = secondUpdated.and(tNextVal);

                BDD bothUpdated = tUpdate.and(ctUpdate);
                BDD nextTransAndNextValsForV_11 = tNextVal.and(ctNextVal);

                transWhereVisUpdated[varIndex] = bothUpdated.or(firstUpdated).or(secondUpdated).or(noneUpdated);
                transAndNextValsForV[varIndex] = nextTransAndNextValsForV_00.or(nextTransAndNextValsForV_01).or(nextTransAndNextValsForV_10).or(nextTransAndNextValsForV_11);
            }
        }

        BDD newEdgesBDD = manager.getOneBDD();
        for(int i = 0; i < bddExAutomata.orgExAutomata.getVars().size(); i++)
        {
            newEdgesBDD.andWith(transAndNextValsForV[i]);
        }
        
        return newEdgesBDD;

    }

    public BDD varEqualToVar(String var)
    {
        ExpressionParser parser =  new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
        try
        {
           BinaryExpressionProxy bep = null;
           bep = (BinaryExpressionProxy)parser.parse(var+"="+var,Operator.TYPE_ARITHMETIC);
           return bddExAutomata.manager.action2BDD(bep);
        }
        catch(ParseException pe)
        {
           System.out.println(pe);
        }

        return null;
    }

    public BDD getMonolithicEdgesForwardBDD()
    {
        return edgesForwardBDD;
    }

    public BDD getMonolithicEdgesBackwardBDD()
    {
        return edgesBackwardBDD;
    }

    public BDD getMyMonolithicEdgesForwardBDD()
    {
        return myEdgesForwardBDD;
    }

    public BDD getMyMonolithicEdgesBackwardBDD()
    {
        return myEdgesBackwardBDD;
    }
}
