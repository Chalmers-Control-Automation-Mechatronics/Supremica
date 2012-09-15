package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * The EventAliasesTree which shows under the definitions tab of the module
 * editor ({@link org.supremica.gui.ide.EditorPanel EditorPanel}).
 *
 * @author Carly Hona
 */
public class EventAliasesTree extends ModuleTree
{

  public EventAliasesTree(final ModuleWindowInterface root,
                             final WatersPopupActionManager manager)
  {
    super(root, manager, root.getModuleSubject(), root.getUndoInterface());
    mPopupFactory = new AliasesTreePopupFactory(manager, root.getModuleContext());
  }

  //#########################################################################
  //# Abstract Methods
  ListSubject<? extends ProxySubject> getRootList(){
    return getRootWindow().getModuleSubject().getEventAliasListModifiable();
  }

  String getRootName(){
    return "Event Aliases";
  }

  DataFlavor getSupportedDataFlavor()
  {
    return WatersDataFlavor.EVENT_ALIAS;
  }

  private static final long serialVersionUID = 1L;

  PopupFactory getPopupFactory()
  {
    return mPopupFactory;
  }

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