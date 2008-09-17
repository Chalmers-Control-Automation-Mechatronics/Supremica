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
  protected CommandLineArgument(final String name,
                                final String description)
  {
    mName = name;
    mDescription = description;
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


  //#########################################################################
  //# Parsing
  protected abstract void parse(Iterator<String> iter);

  protected abstract void assign(ModelVerifier verifier);


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
    indent(stream, INDENT - len);
    final String description = getDescription();
    int start = 0;
    int end = description.indexOf('\n');
    while (end > 0) {
      final String line = description.substring(start, end);
      stream.println(line);
      if (description.length() == end) {
        return;
      }
      indent(stream, INDENT);
      start = end + 1;
      end = description.indexOf('\n', start);
    }
    final String rest = description.substring(start);
    stream.println(rest);
  }


  //#########################################################################
  //# Exception Handling
  protected IllegalArgumentException getMissingValueException()
  {
    return new IllegalArgumentException
      ("No value specified for command line argument " + getName() + "!");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void indent(final PrintStream stream, final int spaces)
  {
    for (int i = 0; i < spaces; i++) {
      stream.print(' ');
    }
  }

      
  //#########################################################################
  //# Data Members
  private final String mName;
  private final String mDescription;


  //#########################################################################
  //# Class Constants
  private static final int INDENT = 20;

}
