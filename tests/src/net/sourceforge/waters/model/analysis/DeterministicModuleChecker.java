//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DeterministicModuleChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/**
 * A debugging tool to check whether a module may contain nondeterminism.
 * This algorithm simply checks the 'deterministic' flag of the model's
 * graphs {@link GraphProxy}. If a graph with the 'deterministic' flag set
 * to <CODE>false</CODE>, the module is assumed to be nondeterministic.
 *
 * @author Robi Malik
 */
public class DeterministicModuleChecker extends AbstractModuleProxyVisitor
{

  //#######################################################################
  //# Singleton Pattern
  public static DeterministicModuleChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final DeterministicModuleChecker INSTANCE =
      new DeterministicModuleChecker();
  }

  private DeterministicModuleChecker()
  {
    mModuleStack = new LinkedList<ModuleProxy>();
  }


  //#######################################################################
  //# Invocation
  public boolean isDeterministic(final ModuleProxy module,
                                 final DocumentManager manager)
  {
    try {
      mDocumentManager = manager;
      mModuleStack.clear();
      return visitModuleProxy(module);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mModuleStack.clear();
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Boolean visitForeachComponentProxy(final ForeachComponentProxy foreach)
    throws VisitorException
  {
    for (final Proxy proxy : foreach.getBody()) {
      final boolean det = (Boolean) proxy.acceptVisitor(this);
      if (!det) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Boolean visitGraphProxy(final GraphProxy graph)
  {
    return graph.isDeterministic();
  }

  @Override
  public Boolean visitInstanceProxy(final InstanceProxy inst)
    throws VisitorException
  {
    try {
      final String name = inst.getModuleName();
      final ProxyMarshaller<ModuleProxy> marshaller =
        mDocumentManager.findProxyMarshaller(ModuleProxy.class);
      final String ext = marshaller.getDefaultExtension();
      final ModuleProxy parent = mModuleStack.get(0);
      final File parentFile = parent.getFileLocation();
      final File dir = parentFile.getParentFile();
      final File file = new File(dir, name + ext);
      final ModuleProxy module = (ModuleProxy) mDocumentManager.load(file);
      return visitModuleProxy(module);
    } catch (final MalformedURLException exception) {
      throw new VisitorException(exception);
    } catch (final WatersUnmarshalException exception) {
      throw new VisitorException(exception);
    } catch (final IOException exception) {
      throw new VisitorException(exception);
    }
  }

  @Override
  public Boolean visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    mModuleStack.add(0, module);
    for (final Proxy proxy : module.getComponentList()) {
      final boolean det = (Boolean) proxy.acceptVisitor(this);
      if (!det) {
        return false;
      }
    }
    mModuleStack.remove(0);
    return true;
  }

  @Override
  public Boolean visitSimpleComponentProxy(final SimpleComponentProxy comp)
  {
    final GraphProxy graph = comp.getGraph();
    return visitGraphProxy(graph);
  }

  @Override
  public Boolean visitVariableComponentProxy(final VariableComponentProxy var)
  {
    return var.isDeterministic();
  }


  //#######################################################################
  //# Data Members
  private DocumentManager mDocumentManager;
  private final List<ModuleProxy> mModuleStack;

}
