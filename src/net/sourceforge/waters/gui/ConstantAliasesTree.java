package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * The ConstantAliasesTree which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona
 */
public class ConstantAliasesTree extends ModuleTree
{

  public ConstantAliasesTree(final ModuleContainer root,
                             final WatersPopupActionManager manager)
  {
    super(root, manager, root.getModule(), root);
    mPopupFactory = new AliasesTreePopupFactory(manager, root.getModuleContext());
  }


  //#########################################################################
  //# Abstract Methods
  @Override
  ListSubject<? extends ProxySubject> getRootList(){
    return getModuleContainer().getModule().getConstantAliasListModifiable();
  }

  @Override
  String getRootName(){
    return "Named Constants";
  }

  @Override
  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.CONSTANT_ALIAS;
  }

  @Override
  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

  private final PopupFactory mPopupFactory;
  private static final long serialVersionUID = 1L;


}