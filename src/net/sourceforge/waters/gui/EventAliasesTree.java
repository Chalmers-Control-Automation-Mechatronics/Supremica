package net.sourceforge.waters.gui;

import java.awt.datatransfer.DataFlavor;
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
    super(root, manager);
    mPopupFactory = new AliasesTreePopupFactory(manager, root.getModuleContext());
  }

  //#########################################################################
  //# Abstract Methods
  ListSubject<? extends ProxySubject> getRootList(){
    return getRoot().getModuleSubject().getEventAliasListModifiable();
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

  private final PopupFactory mPopupFactory;
}