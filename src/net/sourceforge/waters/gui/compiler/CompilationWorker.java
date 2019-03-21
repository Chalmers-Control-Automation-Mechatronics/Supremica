//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.compiler;

import javax.swing.SwingUtilities;

import net.sourceforge.waters.model.compiler.EvalOverflowException;
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
      } catch (final OutOfMemoryError error) {
        System.gc();
        final EvalException exception = new EvalOverflowException(error);
        notifyOwner(null, exception);
      } catch (final StackOverflowError error) {
        final EvalException exception = new EvalOverflowException(error);
        notifyOwner(null, exception);
      } catch (final EvalException exception) {
        notifyOwner(null, exception);
      }
    }
  }


  //#########################################################################
  //# Invocation
  public synchronized void compile()
  {
    mHasWork = true;
    notify();
  }

  public synchronized void abort()
  {
    mCompiler.requestAbort();
  }

  public synchronized void terminate()
  {
    mTerminate = true;
    abort();
    notify();
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
    if (mTerminate) {
      return false;
    } else {
      mCompiler.resetAbort();
      return true;
    }
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
