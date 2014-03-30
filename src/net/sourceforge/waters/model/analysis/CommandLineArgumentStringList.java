//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentStringList
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;


/**
 * A command line argument passed to a {@link ModelAnalyzerFactory} to specify
 * multiple strings. String list command line arguments can be used several
 * times in the command line, each time specifying a string value. The
 * list of parsed values is stored in the
 * <CODE>CommandLineArgumentStringList</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentStringList
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of string list type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-marking&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentStringList(final String name,
                                          final String description)
  {
    super(name, description);
    mValues = new LinkedList<String>();
  }

  /**
   * Creates a command line argument of string list type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;marking&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentStringList(final String name,
                                          final String description,
                                          final boolean required)
  {
    super(name, description, required);
    mValues = new LinkedList<String>();
  }


  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<name>";
  }

  protected List<String> getValues()
  {
    return mValues;
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      final String value = iter.next();
      mValues.add(value);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Data Members
  private final List<String> mValues;

}
