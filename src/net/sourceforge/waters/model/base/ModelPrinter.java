//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   ModelPrinter
//###########################################################################
//# $Id: ModelPrinter.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import net.sourceforge.waters.model.base.ElementProxy;


public class ModelPrinter {

  //#########################################################################
  //# Constructors
  public ModelPrinter(final Writer writer)
  {
    this(writer, 2);
  }

  public ModelPrinter(final Writer writer, final int indentwidth)
  {
    mWriter = writer;
    mIndentWidth = indentwidth;
  }


  //#########################################################################
  //# Printing
  public void print(final String msg)
    throws IOException
  {
    if (msg.length() > 0) {
      indent();
      mWriter.write(msg);
    }
  }

  public void print(final char msg)
    throws IOException
  {
    indent();
    mWriter.write(msg);
  }

  public void print(final int msg)
    throws IOException
  {
    print(Integer.toString(msg));
  }

  public void println()
    throws IOException
  {
    if (mWriter instanceof PrintWriter) {
      final PrintWriter pwriter = (PrintWriter) mWriter;
      pwriter.println();
    } else {
      mWriter.write('\n');
    }
    mAtLineStart = true;
  }

  public void println(final String msg)
    throws IOException
  {
    print(msg);
    println();
  }

  public void println(final char msg)
    throws IOException
  {
    print(msg);
    println();
  }

  public void println(final int msg)
    throws IOException
  {
    print(msg);
    println();
  }

  public void flush()
    throws IOException
  {
    mWriter.flush();
  }


  //#########################################################################
  //# Indentation
  public int setIndent(int indent)
  {
    final int oldindent = mIndent;
    mIndent = indent;
    return oldindent;
  }

  public int indentIn()
  {
    return mIndent++;
  }

  public int indentOut()
  {
    return mIndent--;
  }

  private void indent()
    throws IOException
  {
    if (mAtLineStart) {
      for (int i = mIndent * mIndentWidth; i > 0; i--) {
	mWriter.write(' ');
      }
      mAtLineStart = false;
    }
  }


  //#########################################################################
  //# Data Members
  private final Writer mWriter;
  private final int mIndentWidth;

  private int mIndent = 0;
  private boolean mAtLineStart = true;

}