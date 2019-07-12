package net.sourceforge.waters.analysis.options;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.gui.ide.ModuleContainer;

import org.xml.sax.SAXException;

public class TestDESContext implements ProductDESContext
{
  //#########################################################################
  //# Constructor
  public TestDESContext()
  {
    mModuleContainer = null;
  }

  //#########################################################################
  //# Constructors
  @Override
  public ProductDESProxy getProductDES()
  {
    return des;
  }

  @Override
  public Icon getEventIcon(final EventProxy event)
  {
    final Map<Object,SourceInfo> infoMap = compiler.getSourceInfoMap();
    final SourceInfo info = infoMap.get(event);
    if (info == null) {
      return null;
    }
    final Proxy proxy = info.getSourceObject();
    if (!(proxy instanceof EventDeclProxy)) {
      return null;
    }
    final EventDeclProxy decl = (EventDeclProxy) proxy;
    final ModuleContext context = mModuleContainer.getModuleContext();
    return context.getIcon(decl);
  }

  public void loadAndCompileModule(final String filename)
  {
    try {
      final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory = ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final SAXModuleMarshaller marshaller = new SAXModuleMarshaller(moduleFactory, optable, false);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(marshaller);
      final File file = new File(filename);
      final ModuleProxy module = (ModuleProxy) docManager.load(file);

      compiler = new ModuleCompiler(docManager, desFactory, module);
      des = compiler.compile();

    } catch (final SAXException | WatersUnmarshalException | IOException |
                   EvalException | ParserConfigurationException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  //#########################################################################
  //# Constructors
  private final ModuleContainer mModuleContainer;
  private ProductDESProxy des;
  private ModuleCompiler compiler;
}
