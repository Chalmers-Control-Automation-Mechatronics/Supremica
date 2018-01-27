//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
import java.util.List;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.des.ConflictKind;

import org.xml.sax.SAXException;


/**
 * <P>The assessor for the COMP552 conflict checker programming
 * assignments. The assessor reads a test suite description,
 * passes all tests contained to a {@link ConflictChecker},
 * and prints a result with recommended grades.</P>
 *
 * @author Robi Malik
 */

public class ConflictAssessor extends AbstractAssessor
{

  //#########################################################################
  //# Constructor
  private ConflictAssessor()
    throws JAXBException, SAXException, IOException
  {
  }

  //#########################################################################
  //# Hooks
  @Override
  ConflictChecker createChecker(final ProductDESProxy des)
  {
    final ProductDESProxyFactory factory = getFactory();
    return new ConflictChecker(des, factory);
  }

  @Override
  ConflictCounterExampleChecker createCounterExampleChecker()
  {
    final ProductDESProxyFactory factory = getFactory();
    return new ConflictCounterExampleChecker(factory, false);
  }

  @Override
  ConflictTraceProxy createAlternateTrace(final String name,
                                          final ProductDESProxy des,
                                          final List<EventProxy> events)
  {
    final ProductDESProxyFactory factory = getFactory();
    return factory.createConflictTraceProxy
      (name, des, events, ConflictKind.CONFLICT);
  }

  @Override
  String getResultText(final boolean result)
  {
    return result ? "nonconflicting" : "conflicting";
  }


  //#########################################################################
  //# Main Method for Invocation
  public static void main(final String[] args)
  {
    try {
      final ConflictAssessor assessor = new ConflictAssessor();
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
