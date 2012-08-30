package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * The ConstantAliasesTree which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona
 */
public class ConstantAliasesTree extends ModuleTree
{

  public ConstantAliasesTree(final ModuleWindowInterface root,
                             final WatersPopupActionManager manager)
  {
    super(root, manager, root.getModuleSubject(), root.getUndoInterface());
    mPopupFactory = new AliasesTreePopupFactory(manager, root.getModuleContext());
  }


  //#########################################################################
  //# Abstract Methods
  ListSubject<? extends ProxySubject> getRootList(){
    return getRootWindow().getModuleSubject().getConstantAliasListModifiable();
  }

  String getRootName(){
    return "Named Constants";
  }

  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.CONSTANT_ALIAS;
  }

  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

  private final PopupFactory mPopupFactory;
  private static final long serialVersionUID = 1L;


}