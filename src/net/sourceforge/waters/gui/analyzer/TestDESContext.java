package net.sourceforge.waters.gui.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.analysis.options.ProductDESContext;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.ComponentKind;
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

import org.xml.sax.SAXException;


public class TestDESContext implements ProductDESContext
{
  //#########################################################################
  //# Constructor
  public TestDESContext()
  {
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.options.ProductDESContext
  @Override
  public ProductDESProxy getProductDES()
  {
    return mProductDESProxy;
  }

  @Override
  public Icon getEventIcon(final EventProxy event)
  {
    final SourceInfo info = mSourceInfoMap.get(event);
    if (info == null) {
      return null;
    }
    final Proxy proxy = info.getSourceObject();
    if (!(proxy instanceof EventDeclProxy)) {
      return null;
    }
    final EventDeclProxy decl = (EventDeclProxy) proxy;
    return mModuleContext.getIcon(decl);
  }

  //#########################################################################
  //# Auxiliary Methods
  void loadAndCompileModule(final String filename)
  {
    try {
      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final SAXModuleMarshaller marshaller =
        new SAXModuleMarshaller(moduleFactory, optable, false);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(marshaller);
      final File file = new File(filename);
      final ModuleProxy module = (ModuleProxy) docManager.load(file);
      mModuleContext = new ModuleContext(module);
      final ModuleCompiler compiler =
        new ModuleCompiler(docManager, desFactory, module);
      compiler.setSourceInfoEnabled(true);
      mProductDESProxy = compiler.compile();
      mSourceInfoMap = compiler.getSourceInfoMap();
    } catch (final SAXException | WatersUnmarshalException | IOException |
                   EvalException | ParserConfigurationException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public Icon getComponentKindIcon(final ComponentKind kind)
  {
    if(kind == ComponentKind.PLANT) {
      return IconAndFontLoader.ICON_PLANT;
    }
    else if(kind == ComponentKind.PROPERTY) {
      return IconAndFontLoader.ICON_PROPERTY;
    }
    else if(kind == ComponentKind.SPEC) {
      return IconAndFontLoader.ICON_SPEC;
    }
    else if(kind == ComponentKind.SUPERVISOR) {
      return IconAndFontLoader.ICON_SUPERVISOR;
    }
    else {
      return null;
    }
  }

  //#########################################################################
  //# Data Members
  private ModuleContext mModuleContext;
  private ProductDESProxy mProductDESProxy;
  private Map<Object,SourceInfo> mSourceInfoMap;
}
