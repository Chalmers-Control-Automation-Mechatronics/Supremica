//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import java.io.PrintWriter;
import java.util.Collection;

import net.sourceforge.waters.model.analysis.DefaultProxyResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * The standard implementation of the {@link ProductDESResult} interface.
 * The default product DES result provides read/write access to all the data
 * provided by the interface.
 *
 * @author Robi Malik
 */

public class DefaultProductDESResult
  extends DefaultProxyResult<ProductDESProxy>
  implements ProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a product DES result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public DefaultProductDESResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a product DES result representing an incomplete run.
   * @param  clazz    The class of the model analyser creating this result.
   */
  public DefaultProductDESResult(final Class<?> clazz)
  {
    super(clazz);
  }


  //#########################################################################
  //# Simple Access Methods
  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  @Override
  public Collection<AutomatonProxy> getComputedAutomata()
  {
    final ProductDESProxy des = getComputedProductDES();
    if (des == null) {
      return null;
    } else {
      return des.getAutomata();
    }
  }

  @Override
  public void setComputedProductDES(final ProductDESProxy des)
  {
    setComputedProxy(des);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.DefaultAnalysisResult
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    final Collection<AutomatonProxy> automata = getComputedAutomata();
    if (automata != null) {
      final int numAutomata = automata.size();
      writer.println("Number of computed automata: " + numAutomata);
      long total = 0;
      int max = 0;
      for (final AutomatonProxy aut : automata) {
        final int numStates = aut.getStates().size();
        total += numStates;
        if (numStates > max) {
          max = numStates;
        }
      }
      writer.println("Total number of computed states: " + total);
      writer.println("Maximum number of computed states: " + max);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(',');
    writer.print("NumOutputAutmata");
    writer.print(',');
    writer.print("TotalOutputStates");
    writer.print(',');
    writer.print("MaxOutputStates");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    final Collection<AutomatonProxy> automata = getComputedAutomata();
    if (automata == null) {
      writer.print(",,,");
    } else {
      final int numAutomata = automata.size();
      writer.print(",");
      writer.print(numAutomata);
      long total = 0;
      int max = 0;
      for (final AutomatonProxy aut : automata) {
        final int numStates = aut.getStates().size();
        total += numStates;
        if (numStates > max) {
          max = numStates;
        }
      }
      writer.print(",");
      writer.print(total);
      writer.print(",");
      writer.print(max);
    }
  }

}
