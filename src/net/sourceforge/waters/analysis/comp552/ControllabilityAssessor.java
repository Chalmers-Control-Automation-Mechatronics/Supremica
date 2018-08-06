//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.logging.log4j.LogManager;

import org.xml.sax.SAXException;


/**
 * <P>The assessor for  explicit controllability checker programming
 * assignments. The assessor reads a test suite description,
 * passes all tests contained to a {@link ControllabilityChecker},
 * and prints a result with recommended grades.</P>
 *
 * @author Robi Malik
 */

public class ControllabilityAssessor extends AbstractAssessor
{

  //#########################################################################
  //# Constructor
  private ControllabilityAssessor()
    throws JAXBException, SAXException, IOException
  {
  }

  //#########################################################################
  //# Hooks
  @Override
  ControllabilityChecker createChecker(final ProductDESProxy des)
  {
    final ProductDESProxyFactory factory = getFactory();
    return new ControllabilityChecker(des, factory);
  }

  @Override
  ControllabilityCounterExampleChecker createCounterExampleChecker()
  {
    final ControllabilityCounterExampleChecker checker =
      new ControllabilityCounterExampleChecker(false);
    checker.setEndStateEnabled(true);
    return checker;
  }

  @Override
  SafetyCounterExampleProxy createAlternateCounterExample
    (final String name,
     final ProductDESProxy des,
     final List<EventProxy> events)
  {
    final ProductDESProxyFactory factory = getFactory();
    return factory.createSafetyCounterExampleProxy(name, des, events);
  }

  @Override
  String getResultText(final boolean result)
  {
    return result ? "controllable" : "not controllable";
  }


  //#########################################################################
  //# Counterexample Verification
  @Override
  boolean isHalfCorrectCounterExample(final ProductDESProxy des,
                                      final CounterExampleProxy counter,
                                      final AbstractCounterExampleChecker checker)
    throws AnalysisException
  {
    final ControllabilityCounterExampleChecker cChecker =
      (ControllabilityCounterExampleChecker) checker;
    final Map<AutomatonProxy,StateProxy> endState = cChecker.getEndState();
    if (endState != null && isUncontrollableState(des, endState)) {
      printMalformedCounterExample
        (counter, "is missing the final uncontrollable event");
      return true;
    }
    return super.isHalfCorrectCounterExample(des, counter, checker);
  }

  private boolean isUncontrollableState(final ProductDESProxy des,
                                        final Map<AutomatonProxy,StateProxy> tuple)
  {
    final int numAutomata = des.getAutomata().size();
    final Collection<AutomatonProxy> plants = new ArrayList<>(numAutomata);
    final Collection<AutomatonProxy> specs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (aut.getKind()) {
      case PLANT:
        plants.add(aut);
        break;
      case SPEC:
        specs.add(aut);
        break;
      default:
        break;
      }
    }
    events:
    for (final EventProxy event : des.getEvents()) {
      if (event.getKind() == EventKind.UNCONTROLLABLE) {
        for (final AutomatonProxy plant : plants) {
          if (!isEventEnabled(plant, tuple, event)) {
            continue events;
          }
        }
        for (final AutomatonProxy spec : specs) {
          if (!isEventEnabled(spec, tuple, event)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isEventEnabled(final AutomatonProxy aut,
                                 final Map<AutomatonProxy,StateProxy> tuple,
                                 final EventProxy event)
  {
    if (aut.getEvents().contains(event)) {
      final StateProxy state = tuple.get(aut);
      return AutomatonTools.getFirstSuccessorState(aut, state, event) != null;
    } else {
      return true;
    }
  }


  //#########################################################################
  //# Main Method for Invocation
  public static void main(final String[] args)
  {
    try {
      QuietLogConfigurationFactory.install();
      LogManager.getLogger(); // avoid trouble with security manager later
      final ControllabilityAssessor assessor = new ControllabilityAssessor();
      assessor.processCommandLine(args);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() +
                         " caught in main()!");
      exception.printStackTrace(System.err);
      System.exit(1);
    }
  }

}
