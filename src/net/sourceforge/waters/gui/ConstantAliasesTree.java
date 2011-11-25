package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.ConstantAliasTransferable;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * The ConstantAliasesTree which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona
 */
public class ConstantAliasesTree extends AliasesTree
{

  public ConstantAliasesTree(final ModuleWindowInterface root,
                             final WatersPopupActionManager manager)
  {
    super(root, manager);
  }


  //#########################################################################
  //# Abstract Methods
  ListSubject<? extends ProxySubject> getRootList(){
    return getRoot().getModuleSubject().getConstantAliasListModifiable();
  }

  String getRootName(){
    return "Named Constants";
  }

  Transferable getTransferable(final List<? extends Proxy> items)
  {
    return new ConstantAliasTransferable(items);
  }

  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.CONSTANT_ALIAS_LIST;
  }

  private static final long serialVersionUID = 1L;

}