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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


public class PromelaTest
{

  @SuppressWarnings("unused")
  public static void main(final String[] args)
  {
    try {
      for (final String arg : args) {
        final PromelaTools promelaTools = new PromelaTools();
        promelaTools.parseFile(arg);
        final Hashtable<String, ChanInfo> ch = promelaTools.getchan();
        final CreateAutomaton cr = new CreateAutomaton();
      //  cr.createEvent(n, a)
        final Enumeration<String> e = ch.keys();

        while(e.hasMoreElements()){
          final String name = (String) e.nextElement();
          final ChanInfo c = ch.get(name);
          final int length = c.getDataLength();
          final List<List<String>> a1 = c.getValue();
          for(int i=0;i<a1.size();i++){
            cr.createEvent(name, a1.get(i));
          }
        }


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
