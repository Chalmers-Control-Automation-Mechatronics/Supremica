//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PromelaParserError
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.promela;

public class PromelaParserError
{
  private int line;
  private int offset;
  private String message;
  private boolean error = false; // warning otherwise

  public PromelaParserError(final int line, final int offset,
                            final String message, final boolean error)
  {
    this.line = line;
    this.offset = offset;
    this.message = message;
    this.error = error;
  }

  public int getLine()
  {
    return line;
  }

  public void setLine(final int line)
  {
    this.line = line;
  }

  public int getOffset()
  {
    return offset;
  }

  public void setOffset(final int offset)
  {
    this.offset = offset;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(final String message)
  {
    this.message = message;
  }

  public String toString()
  {
    if (error)
      return "Error: line " + line + ", Offset " + offset + " - " + message;
    else
      return "Warning: line " + line + ", Offset " + offset + " - " + message;
  }

}
