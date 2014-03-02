//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.compiler
//# CLASS:   CompilationWorker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.compiler;

import javax.swing.SwingUtilities;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;

public class CompilationWorker implements Runnable
{

  //#########################################################################
  //# Constructors
  public CompilationWorker(final BackgroundCompiler owner,
                           final ModuleCompiler compiler,
                           final String name)
  {
    mOwner = owner;
    mCompiler = compiler;
    final String nameWithoutSpaces = name.replace(' ', '-');
    final String threadName = "BackgroundCompilation-" + nameWithoutSpaces;
    new Thread(this, threadName).start();
  }


  //#########################################################################
  //# Interface java.lang.Runnable
  @Override
  public void run()
  {
    while (hasWork()) {
      try {
        final ProductDESProxy compiledDES = mCompiler.compile();
        notifyOwner(compiledDES, null);
      } catch (final EvalException evalException) {
        notifyOwner(null, evalException);
      }
    }
  }


  //#########################################################################
  //# Invocation
  public synchronized void compile()
  {
    mHasWork = true;
    this.notify();
  }

  public synchronized void abort()
  {
    mCompiler.requestAbort();
    this.notify();
  }

  public synchronized void terminate()
  {
    mTerminate = true;
    abort();
  }


  //#########################################################################
  //# Auxiliary Methods
  private synchronized boolean hasWork()
  {
    while (!mHasWork && !mTerminate) {
      try {
        this.wait();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
    mHasWork = false;
    return !mTerminate;
  }

  private void notifyOwner(final ProductDESProxy compiledDES,
                           final EvalException evalException)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        if (!mTerminate) {
          mOwner.compilationFinished(compiledDES, evalException);
        }
      }
    });
  }


  //#########################################################################
  //# Data Members
  private final BackgroundCompiler mOwner;
  private final ModuleCompiler mCompiler;

  private boolean mHasWork = false;
  private boolean mTerminate = false;

}
