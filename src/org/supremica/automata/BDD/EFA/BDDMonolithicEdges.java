
package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */

import net.sf.javabdd.*;
import net.sourceforge.waters.model.module.VariableComponentProxy;


public class BDDMonolithicEdges
    implements BDDEdges
{
    private BDDExtendedAutomata bddExAutomata;
    private BDDExtendedManager manager;
    private BDD edgesForwardBDD = null;
    private BDD edgesBackwardBDD = null;

    private BDD edgesForwardWithEventsBDD = null;
    private BDD edgesBackwardWithEventsBDD = null;

    private BDD uncontrollableEdgesForwardBDD = null;
    private BDD uncontrollableEdgesBackwardBDD = null;
    private BDD plantUncontrollableEdgesForwardBDD = null;
    private BDD specUncontrollableEdgesForwardBDD = null;
    private BDD plantUncontrollableEdgesBackwardBDD = null;
    private BDD specUncontrollableEdgesBackwardBDD = null;
    private boolean systemHasNoPlants = true;
    private boolean systemHasNoSpecs = true;

    /** Creates a new instance of BDDMonolithicEdges */
    public BDDMonolithicEdges(BDDExtendedAutomata bddExAutomata)
    {
        this.bddExAutomata = bddExAutomata;
        manager = bddExAutomata.getBDDManager();

        edgesForwardBDD = manager.getOneBDD();
        edgesBackwardBDD = manager.getOneBDD();
        uncontrollableEdgesForwardBDD = manager.getZeroBDD();
        uncontrollableEdgesBackwardBDD = manager.getZeroBDD();
        plantUncontrollableEdgesForwardBDD = manager.getOneBDD();
        specUncontrollableEdgesForwardBDD = manager.getOneBDD();

        plantUncontrollableEdgesBackwardBDD = manager.getZeroBDD();
        specUncontrollableEdgesBackwardBDD = manager.getZeroBDD();
        
        for (BDDExtendedAutomaton currAutomaton : bddExAutomata)
        {
//            System.out.println(currAutomaton.getExAutomaton().getName());
//            currAutomaton.getEdgeForwardBDD().printDot();

            edgesForwardBDD = edgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());

            if(currAutomaton.getExAutomaton().isSpecification())
            {
                specUncontrollableEdgesForwardBDD = specUncontrollableEdgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());
                systemHasNoSpecs = false;
            }
            else
            {
                plantUncontrollableEdgesForwardBDD = plantUncontrollableEdgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());
                systemHasNoPlants = false;
            }
        }
        if(systemHasNoPlants)
            plantUncontrollableEdgesForwardBDD = manager.getZeroBDD();

        if(systemHasNoSpecs)
            specUncontrollableEdgesForwardBDD = manager.getZeroBDD();


        BDD actionsBDD = computeSynchronizedActions(bddExAutomata.forwardTransWhereVisUpdated, bddExAutomata.forwardTransAndNextValsForV);
//        System.err.println("actions synchronized");

        edgesForwardBDD = edgesForwardBDD.and(actionsBDD);
//        edgesForwardBDD.printDot();
        
        specUncontrollableEdgesForwardBDD = specUncontrollableEdgesForwardBDD.and(actionsBDD);
        plantUncontrollableEdgesForwardBDD = plantUncontrollableEdgesForwardBDD.and(actionsBDD);

        System.err.println("BDD represeting forward edges with events is created.");

        specUncontrollableEdgesForwardBDD = specUncontrollableEdgesForwardBDD.and(bddExAutomata.uncontrollableEventsBDD);
//            specUncontrollableEdgesForwardBDD = specUncontrollableEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());
/*
        if(!specUncontrollableEdgesForwardBDD.isZero())
        {
            specUncontrollableEdgesBackwardBDD = sourceTooTdest(specUncontrollableEdgesForwardBDD);
        }
*/
        plantUncontrollableEdgesForwardBDD = plantUncontrollableEdgesForwardBDD.and(bddExAutomata.uncontrollableEventsBDD);
//            plantUncontrollableEdgesForwardBDD = plantUncontrollableEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());
/*
        if(!plantUncontrollableEdgesForwardBDD.isZero())
        {
            plantUncontrollableEdgesBackwardBDD = sourceTooTdest(plantUncontrollableEdgesForwardBDD);
        }
*/
        uncontrollableEdgesForwardBDD = edgesForwardBDD.and(bddExAutomata.uncontrollableEventsBDD);
        uncontrollableEdgesForwardBDD = uncontrollableEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());

        if(!uncontrollableEdgesForwardBDD.isZero())
        {
            uncontrollableEdgesBackwardBDD = sourceTooTdest(uncontrollableEdgesForwardBDD);
        }

        edgesBackwardBDD = sourceTooTdest(edgesForwardBDD);
        System.err.println("BDD represeting backward edges with events is created.");

        edgesForwardWithEventsBDD = edgesForwardBDD;
        edgesBackwardWithEventsBDD = edgesBackwardBDD;

        edgesForwardBDD = edgesForwardBDD.exist(bddExAutomata.getEventVarSet());
        System.err.println("BDD represeting forward edges without events is created.");
        System.err.println("The number of nodes in the forward edge BDD: "+edgesForwardBDD.nodeCount());
//        System.err.println("Number of states in the closed-loop system: "+bddExAutomata.nbrOfStatesBDD(edgesForwardBDD.exist(bddExAutomata.getDestStatesVarSet())));
        edgesBackwardBDD = edgesBackwardBDD.exist(bddExAutomata.getEventVarSet());
        System.err.println("BDD represeting backward edges without events is created.");

        //create backward overflow
        bddExAutomata.setBackwardOverflows(sourceTooTdest(bddExAutomata.getForwardOverflows()));

    }

    public BDD sourceTooTdest(BDD bdd)
    {
        BDD newBDD = bdd.id();
        
        newBDD.replaceWith(bddExAutomata.sourceToTempLocationPairing);
        newBDD.replaceWith(bddExAutomata.destToSourceLocationPairing);
        newBDD.replaceWith(bddExAutomata.tempToDestLocationPairing);
        newBDD.replaceWith(bddExAutomata.sourceToTempVariablePairing);
        newBDD.replaceWith(bddExAutomata.destToSourceVariablePairing);
        newBDD.replaceWith(bddExAutomata.tempToDestVariablePairing);

        return newBDD;
    }

    public BDD computeSynchronizedActions(BDD[][] inTransWhereVisUpdated, BDD[][] inTransAndNextValsForV)
    {
        BDD[] autTransWhereVisUpdated = inTransWhereVisUpdated[0];
        BDD[] autTransAndNextValsForV = inTransAndNextValsForV[0];

        if(bddExAutomata.orgExAutomata.size() == 1)
        {
            for(VariableComponentProxy var:bddExAutomata.orgExAutomata.getVars())
            {
                int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                BDD noneUpdateVar = bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
                autTransAndNextValsForV[varIndex] = autTransWhereVisUpdated[varIndex].ite(autTransAndNextValsForV[varIndex],noneUpdateVar);
            }
        }

        for(int i = 1 ; i< (bddExAutomata.orgExAutomata.size());i++)
        {
            BDD[] currAutTransWhereVisUpdated = inTransWhereVisUpdated[i];
            BDD[] currAutTransAndNextValsForV = inTransAndNextValsForV[i];
            
            for(VariableComponentProxy var:bddExAutomata.orgExAutomata.getVars())
            {
                int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                BDD tUpdate = autTransWhereVisUpdated[varIndex];
                BDD ctUpdate = currAutTransWhereVisUpdated[varIndex];
                BDD tNextVal = autTransAndNextValsForV[varIndex];
                BDD ctNextVal = currAutTransAndNextValsForV[varIndex];

                BDD firstUpdated = tUpdate.and(ctUpdate.not());
                BDD nextTransAndNextValsForV_01 = firstUpdated.and(tNextVal);

                BDD secondUpdated = tUpdate.not().and(ctUpdate);
                BDD nextTransAndNextValsForV_10 = secondUpdated.and(ctNextVal);

                autTransWhereVisUpdated[varIndex] = tUpdate.or(ctUpdate);

                BDD bothUpdated = tNextVal.and(ctNextVal);//.and(tUpdate).and(ctUpdate);                

                BDD noneUpdated = (bothUpdated.or(firstUpdated).or(secondUpdated)).not();
                BDD noChange = bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
                BDD noneUpdateVar = noneUpdated.and(noChange);

                autTransAndNextValsForV[varIndex] = nextTransAndNextValsForV_01.or(nextTransAndNextValsForV_10).or(bothUpdated).or(noneUpdateVar);
            }
        }

        BDD newEdgesBDD = manager.getOneBDD();
        for(int i = 0; i < bddExAutomata.orgExAutomata.getVars().size(); i++)
        {

            String varName = bddExAutomata.theIndexMap.getVariableAt(i).getName();
            autTransAndNextValsForV[i] = autTransAndNextValsForV[i].and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName).lte(bddExAutomata.getMaxBDDBitVecOf(varName)));
            autTransAndNextValsForV[i] = autTransAndNextValsForV[i].and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName).lte(bddExAutomata.getMaxBDDBitVecOf(varName)));
            autTransAndNextValsForV[i] = autTransAndNextValsForV[i].and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName).gte(bddExAutomata.getMinBDDBitVecOf(varName)));
            autTransAndNextValsForV[i] = autTransAndNextValsForV[i].and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName).gte(bddExAutomata.getMinBDDBitVecOf(varName)));

            newEdgesBDD.andWith(autTransAndNextValsForV[i]);
        }
        
        return newEdgesBDD;

    }

    public BDD getMonolithicEdgesForwardBDD()
    {
        return edgesForwardBDD;
    }

    public BDD getMonolithicEdgesBackwardBDD()
    {
        return edgesBackwardBDD;
    }

    public BDD getMonolithicEdgesForwardWithEventsBDD()
    {
        return edgesForwardWithEventsBDD;
    }

    public BDD getMonolithicEdgesBackwardWithEventsBDD()
    {
        return edgesBackwardWithEventsBDD;
    }

    public BDD getMonolithicUncontrollableEdgesBackwardBDD()
    {
        return uncontrollableEdgesBackwardBDD;
    }

    public BDD getMonolithicUncontrollableEdgesForwardBDD()
    {
        return uncontrollableEdgesForwardBDD;
    }

    public BDD getPlantMonolithicUncontrollableEdgesForwardBDD()
    {
        return plantUncontrollableEdgesForwardBDD;
    }

    public BDD getSpecMonolithicUncontrollableEdgesForwardBDD()
    {
        return specUncontrollableEdgesForwardBDD;
    }

    public BDD getPlantMonolithicUncontrollableEdgesBackwardBDD()
    {
        return plantUncontrollableEdgesBackwardBDD;
    }

    public BDD getSpecMonolithicUncontrollableEdgesBackwardBDD()
    {
        return specUncontrollableEdgesBackwardBDD;
    }

    public boolean plantOrSpecDoesNotExist()
    {
        return systemHasNoPlants | systemHasNoSpecs;
    }

}
