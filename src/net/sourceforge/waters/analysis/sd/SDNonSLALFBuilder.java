//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SDNonSLALFBuilder
//###########################################################################


package net.sourceforge.waters.analysis.sd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
* A converter to translate models to a model for checking Non-Self loop ALF.
*
* @author Mahvash Baloch
*/

public class SDNonSLALFBuilder
extends SDActivityLoopChecker
{

//#########################################################################
//# Constructors
public SDNonSLALFBuilder(final ProductDESProxy model,
                          final ProductDESProxyFactory factory)
{
  super (model,factory);
}

public boolean run() throws AnalysisException
{
  setUp();
  try {
    final ProductDESProxy model = getModel();
    LoopTraceProxy counterexample = null;
    final Collection<AutomatonProxy> oldAutomata = model.getAutomata();
     boolean check = true;
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut);
    ProductDESProxy newModel = null;
    SDActivityLoopChecker checker = new SDActivityLoopChecker(newModel, getFactory());;
   // final Collection<EventProxy> allEvents = model.getEvents();

    for (final AutomatonProxy oldAut : oldAutomata)
    {
      final AutomatonProxy newAut;
      newAutomata.clear();
      if (oldAut.getKind()== ComponentKind.SUPERVISOR) {
       newAut = modifyAut(oldAut);
       final Collection <EventProxy> allEvents = newAut.getEvents();
       newAutomata.add(newAut);
       newModel =
           getFactory().createProductDESProxy(model.getName(), model.getComment(), null,
                                          allEvents, newAutomata);
       checker= new SDActivityLoopChecker(newModel, getFactory());
       final VerificationResult result;
       try {
          checker.run();
       } finally {
         result = checker.getAnalysisResult();
        // System.out.println(result);
       }
      if (!result.isSatisfied()) {
         counterexample = checker.getCounterExample();
        check = false;
        System.out.println("Supervisor  " + newAut.getName() + "  is not ALF ");


      }
    }
      }
    if(!check)
    { return setFailedResult(counterexample); }
    else
    return setSatisfiedResult();
    }
   finally {
    tearDown();
    }
}

/**
 * Modifies the Supervisor component by removing all Activity event selfloops
 */

private AutomatonProxy modifyAut (final AutomatonProxy aut)
{
  final List<TransitionProxy> newTransitions =
    new ArrayList<TransitionProxy>();

final Collection<EventProxy> allEvents = aut.getEvents();

final Collection<TransitionProxy> alltransitions = aut.getTransitions();
final Collection<StateProxy> allStates = aut.getStates();

for (final TransitionProxy transition : alltransitions) {

    final EventProxy ev = transition.getEvent();
    final StateProxy sourceState = transition.getSource();
    final StateProxy targetState = transition.getTarget();
    final String name = ev.getName();

    if((!name.equals("tick")))
    {
      if(sourceState.equals(targetState))
       {}
      else
        newTransitions.add(transition);
    }
    else
   newTransitions.add(transition);

}

return getFactory().createAutomatonProxy(aut.getName() , aut.getKind(),
                                     allEvents, allStates, newTransitions);
}


//#########################################################################
//# Data Members

}
