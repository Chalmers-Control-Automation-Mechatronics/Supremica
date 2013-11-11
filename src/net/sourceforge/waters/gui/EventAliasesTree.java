package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * The EventAliasesTree which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona
 */
public class EventAliasesTree extends ModuleTree
{

  public EventAliasesTree(final ModuleContainer root,
                             final WatersPopupActionManager manager)
  {
    super(root, manager, root.getModule(), root);
    mPopupFactory = new AliasesTreePopupFactory(manager, root.getModuleContext());
  }

  //#########################################################################
  //# Abstract Methods
  @Override
  ListSubject<? extends ProxySubject> getRootList(){
    return getModuleContainer().getModule().getEventAliasListModifiable();
  }

  @Override
  String getRootName(){
    return "Event Aliases";
  }

  @Override
  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.EVENT_ALIAS;
  }

  private static final long serialVersionUID = 1L;

  @Override
  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

  @Override
  boolean shouldForceCopy(final DataFlavor flavor,
                          final Transferable transferable)
  {
    if (flavor == WatersDataFlavor.IDENTIFIER
        && transferable.isDataFlavorSupported(WatersDataFlavor.EVENT_ALIAS)) {
      return true;
    }
    return false;
  }

  private final PopupFactory mPopupFactory;
}