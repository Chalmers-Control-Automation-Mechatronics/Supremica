//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Import/Export
//# PACKAGE: net.sourceforge.waters.external.susyna
//# CLASS:   SusynaParseException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.susyna;

import java.io.File;

/**
 * @author Robi Malik
 */

public class SusynaParseException extends Exception
{

  //#########################################################################
  //# Constructors
  SusynaParseException(final String msg, final File file, final int lineno)
  {
    super("Parse error in file " + file + ", line " + lineno +
          ": " + msg + ".");
  }

  SusynaParseException(final SusynaToken token,
                       final File file,
                       final int lineno)
  {
    this("Unexpected token " + token, file, lineno);
  }

  SusynaParseException(final SusynaToken token,
                       final SusynaToken.Type expected,
                       final File file,
                       final int lineno)
  {
    this("Unexpected token " + token + ", expected " + expected, file, lineno);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
