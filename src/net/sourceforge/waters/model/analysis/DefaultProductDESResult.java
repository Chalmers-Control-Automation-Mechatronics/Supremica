//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultProductDESResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintWriter;
import java.util.Collection;
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
   * Creates an automaton result representing an incomplete run.
   */
  public DefaultProductDESResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  public Collection<AutomatonProxy> getComputedAutomata()
  {
    final ProductDESProxy des = getComputedProductDES();
    if (des == null) {
      return null;
    } else {
      return des.getAutomata();
    }
  }

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
