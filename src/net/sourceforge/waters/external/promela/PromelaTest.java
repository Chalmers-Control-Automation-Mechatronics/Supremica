//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PromelaTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.promela;

import java.io.IOException;


public class PromelaTest
{

  public static void main(final String[] args)
  {
    try {
      for (final String arg : args) {
        final PromelaTools promelaTools = new PromelaTools();
        promelaTools.parseFile(arg);



        if (promelaTools.isSyntacticallyCorrect()) {
          System.out.println("Promela file is syntactically correct.");
        } else {
          int counter = 1;
          for (final PromelaParserError error : promelaTools.getErrors()) {
            System.out.println("Report " + counter + ": " + error.toString());
            counter++;
          }
        }
      }
    } catch (final IOException e) {
      System.err.println("FATAL ERROR: " + e.getMessage());
      e.printStackTrace(System.err);
    }

  }

}
