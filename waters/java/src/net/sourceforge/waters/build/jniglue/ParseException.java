//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ParseException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


/**
 * Thrown when parsing of a glue file has failed.
 * These exceptions are thrown by the {@link GlueFileParser} and
 * {@link GlueFileScanner}. In addition to an error message, they
 * contain information about the location of the error in the
 * file that was parsed.
 *
 * @author Robi Malik
 */

class ParseException extends Exception
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new parse exception with the specified detail message.
   * @param  message  The detail message for the new exception.
   * @param  lineno   The line number in the input file where the error
   *                  was detected.
   */
  ParseException(final String message, final int lineno)
  {
    super(message);
    mLineNo = lineno;
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the location of the error that caused this exception.
   * @return The line number in the input file where the
   *         error was detected. The first line is numbered&nbsp;1.
   */
  int getLineNo()
  {
    return mLineNo;
  }


  //#########################################################################
  //# Data Members
  public static final long serialVersionUID = 1;

  private final int mLineNo;

}
