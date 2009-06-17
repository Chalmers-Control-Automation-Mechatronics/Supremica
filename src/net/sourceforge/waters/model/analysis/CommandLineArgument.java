//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgument
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.Iterator;

import net.sourceforge.waters.model.compiler.ModuleCompiler;


/**
 * A command line argument passed to a {@link ModelVerifierFactory}.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgument
  implements Comparable<CommandLineArgument>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-marking&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgument(final String name,
                                final String description)
  {
    this(name, description, false);
  }

  /**
   * Creates a command line argument.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-marking&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgument(final String name,
                                final String description,
                                final boolean required)
  {
    mName = name;
    mDescription = description;
    mIsRequired = required;
  }


  //#########################################################################
  //# Interface java.util.Comparable
  public int compareTo(final CommandLineArgument arg)
  {
    return mName.compareTo(arg.getName());
  }


  //#########################################################################
  //# Simple Access
  protected String getName()
  {
    return mName;
  }

  protected String getArgumentTemplate()
  {
    return null;
  }

  protected String getDescription()
  {
    return mDescription;
  }

  /**
   * <P>Determines whether this is a required command line argument.</P>
   * <P>An argument must be required or optional for all model verifiers
   * of its factory; if more elaborate conditions on arguments are
   * needed, they have to be implemented by the individual model verifiers.</P>
   * <P>After parsing all command line arguments, the {@link
   * ModelVerifierFactory} checks whether all required arguments have been
   * specified, and if this is not the case, it causes configuration to
   * fail by calling the {@link #fail(String) fail()} method of the
   * unspecified required argument.</P>
   */
  protected boolean isRequired()
  {
    return mIsRequired;
  }


  //#########################################################################
  //# Parsing
  protected abstract void parse(Iterator<String> iter);

  protected void configure(final ModuleCompiler compiler)
  {
  }

  protected void configure(final ModelVerifier verifier)
  {
  }


  //#########################################################################
  //# Printing
  protected void dump(final PrintStream stream)
  {
    final String name = getName();
    final String template = getArgumentTemplate();
    stream.print(name);
    int len = name.length();
    if (template != null) {
      stream.print(' ');
      stream.print(template);
      len += template.length() + 1;
    }
    doIndent(stream, INDENT - len);
    final String description = getDescription();
    int start = 0;
    int end = description.indexOf('\n');
    while (end > 0) {
      final String line = description.substring(start, end);
      stream.println(line);
      if (description.length() == end) {
        return;
      }
      doIndent(stream, INDENT);
      start = end + 1;
      end = description.indexOf('\n', start);
    }
    final String rest = description.substring(start);
    stream.println(rest);
  }


  protected void doIndent(final PrintStream stream, final int spaces)
  {
    for (int i = 0; i < spaces; i++) {
      stream.print(' ');
    }
  }


  //#########################################################################
  //# Exception Handling
  protected void fail(final String msg)
  {
    System.err.println(msg);
    System.exit(1);
  }

  protected void failMissingValue()
  {
    fail("No value specified for command line argument " + getName() + "!");
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final String mDescription;
  private final boolean mIsRequired;


  //#########################################################################
  //# Class Constants
  protected static final int INDENT = 20;

}
