//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ModuleCompiler
//###########################################################################
//# $Id: ModuleCompiler.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.File;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.base.UnexpectedWatersException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * <P>The main module compiler class.</P>
 *
 * A module compiler takes Waters modules ({@link ModuleProxy}) as input
 * and translates them into a product DES structures ({@link ProductDESProxy}).
 * It evaluates all parameterised and loop structures and loads and
 * instantiates other modules as needed.
 *
 * @author Robi Malik
 */

public class ModuleCompiler
{

  //#########################################################################
  //# Constructors
  public ModuleCompiler(final ModuleProxy module)
    throws JAXBException
  {
    this(module, new DefaultDocumentManager());
  }

  public ModuleCompiler(final ModuleProxy module,
			final DocumentManager manager)
  {
    mDocumentManager = manager;
    mModule = module;
    mResult = null;
    mContext = null;
  }


  //#########################################################################
  //# Invocation
  public ProductDESProxy compile()
    throws EvalException
  {
    final String name = mModule.getName();
    final File modlocation = mModule.getLocation();
    File deslocation = null;
    if (modlocation != null) {
      try {
	final ProxyMarshaller marshaller =
	  mDocumentManager.findProxyMarshaller(ProductDESProxy.class);
	final File dir = modlocation.getParentFile();
	final String ext = marshaller.getDefaultExtension();
	deslocation = new File(dir, name + ext);
      } catch (final IllegalArgumentException exception) {
	// No marshaller --- O.K.
      }
    }
    mContext = new CompilerContext(mModule);
    mResult = new ProductDESProxy(name, deslocation);
    final ModuleCompilerTask task = new ModuleCompilerTask(mContext, this);
    task.compileModule(mModule);
    return mResult;
  }


  //#########################################################################
  //# Callbacks for Subtasks
  DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  EventProxy getEvent(final EventProxy template)
  {
    try {
      return mResult.addEvent(template);
    } catch (final DuplicateNameException exception) {
      throw new UnexpectedWatersException(exception);
    }
  }

  void processAutomaton(final AutomatonProxy aut)
    throws DuplicateNameException
  {
    mResult.addAutomaton(aut);
  }


  //#########################################################################
  //# Local Class DefaultDocumentManager
  private static class DefaultDocumentManager extends DocumentManager
  {

    //#######################################################################
    //# Constructor
    DefaultDocumentManager()
      throws JAXBException
    {
      final ModuleMarshaller marshaller = new ModuleMarshaller();
      register(marshaller);
    }

  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mModule;
  private ProductDESProxy mResult;
  private CompilerContext mContext;

}
