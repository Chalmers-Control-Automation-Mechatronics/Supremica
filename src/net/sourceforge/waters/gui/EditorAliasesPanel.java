package net.sourceforge.waters.gui;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JList;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.ConstantAliasSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;


public class EditorAliasesPanel extends JList implements SelectionOwner
{

  public EditorAliasesPanel(final ModuleWindowInterface root,
                            final WatersPopupActionManager manager)
  {
    mRoot = root;
    mPopupFactory = new EventDeclListPopupFactory(manager);

    final ModuleSubject module = mRoot.getModuleSubject();
    final ListSubject<ConstantAliasSubject> aliases =
      module.getConstantAliasListModifiable();
    mModel = new IndexedListModel<ConstantAliasSubject>(aliases);
    setModel(mModel);
    final MouseListener handler = new EditorAliasMouseListener();
    addMouseListener(handler);
  }

  public void attach(final Observer o)
  {
    // TODO Auto-generated method stub

  }

  public void detach(final Observer o)
  {
    // TODO Auto-generated method stub

  }

  public void fireEditorChangedEvent(final EditorChangedEvent e)
  {
    // TODO Auto-generated method stub

  }

  public UndoInterface getUndoInterface(final Action action)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasNonEmptySelection()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean canSelectMore()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isSelected(final Proxy proxy)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public List<? extends Proxy> getCurrentSelection()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List<? extends Proxy> getAllSelectableItems()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Proxy getSelectionAnchor()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Proxy getSelectableAncestor(final Proxy item)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void clearSelection(final boolean propagate)
  {
    // TODO Auto-generated method stub

  }

  public void replaceSelection(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub

  }

  public void addToSelection(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub

  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub

  }

  public Object getInsertPosition(final Proxy item)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void insertCreatedItem(final Proxy item, final Object inspos)
  {
    // TODO Auto-generated method stub

  }

  public boolean canCopy(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public Transferable createTransferable(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean canPaste(final Transferable transferable)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean canDelete(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void insertItems(final List<InsertInfo> inserts)
  {
    // TODO Auto-generated method stub

  }

  public void deleteItems(final List<InsertInfo> deletes)
  {
    // TODO Auto-generated method stub

  }

  public void scrollToVisible(final List<? extends Proxy> items)
  {
    // TODO Auto-generated method stub

  }

  public void activate()
  {
    // TODO Auto-generated method stub

  }

  public void close()
  {
    // TODO Auto-generated method stub

  }


  //#########################################################################
  //# Inner Class EventDeclMouseListener
  /**
   * A simple mouse listener to trigger opening the event declaration editor
   * dialog by double-click, and to trigger a popup menu.
   */
  private class EditorAliasMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1
          && event.getClickCount() == 2) {
        final Point point = event.getPoint();
        final int index = locationToIndex(point);
        if (index >= 0 && index < mModel.getSize()) {
          final ConstantAliasSubject decl = mModel.getElementAt(index);
          new ConstantAliasEditorDialog(mRoot, decl);
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      requestFocusInWindow();
      maybeShowPopup(event);
    }

    public void mouseReleased(final MouseEvent event)
    {
      maybeShowPopup(event);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void maybeShowPopup(final MouseEvent event)
    {
      final Point point = event.getPoint();
      final int index = locationToIndex(point);
      final ConstantAliasSubject clicked;
      if (index >= 0 && index < mModel.getSize()) {
        clicked = mModel.getElementAt(index);
      } else {
        clicked = null;
      }
      mPopupFactory.maybeShowPopup(EditorAliasesPanel.this, event, clicked);
    }

  }

  /**
  *
  */
  private static final long serialVersionUID = 1L;

  private final IndexedListModel<ConstantAliasSubject> mModel;
  private final PopupFactory mPopupFactory;
  private final ModuleWindowInterface mRoot;
}