package net.sourceforge.waters.gui;
import javax.swing.JOptionPane;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;

public class ModuleTree extends JTree
{
    private DefaultMutableTreeNode mRootNode;
    private final ProxyPrinter mPrinter;
    private ModuleSubject mModule;
    private ModuleWindowInterface mModuleWindow;
    private ModuleTree mSelfRef;
    
    public ModuleTree(ModuleWindowInterface moduleWindow)
    {
        super();
        
        mPrinter = new PlainTextPrinter();
        mModule = moduleWindow.getModuleSubject();
        mModuleWindow = moduleWindow;
        mSelfRef = this;
        
        ArrayList l;
        if (mModule != null)
        {
            l = new ArrayList(mModule.getComponentList());
            mRootNode = new DefaultMutableTreeNode(new ComponentInfo("Module: " + mModule.getName(), mModule));
        }
        else
        {
            l = new ArrayList();
            mRootNode = null;
        }
        
        for (int i = 0; i < l.size(); i++)
        {
            mRootNode.add(makeTreeFromComponent((AbstractSubject) (l.get(i))));
        }
        
        this.setModel(new DefaultTreeModel(mRootNode));
        
        
        setCellRenderer(new ModuleTreeRenderer());
        setEditable(false);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        
        //moduleSelectTree.setCellRenderer(renderer);
        MouseListener ml = new MouseAdapter()
        {
            EditorWindowInterface ed = null;
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    TreePath selPath = getPathForLocation(e.getX(), e.getY());
                    if(selPath == null) return;
                    
                    mSelfRef.setSelectionPath(selPath);
                    
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    selPath.getLastPathComponent();
                    if (node == null)
                    {
                        return;
                    }
                    Object nodeInfo = node.getUserObject();
                    
                    AbstractSubject component = (((ComponentInfo) nodeInfo).getComponent());
                    if (component instanceof VariableSubject)
                    {
                        EditorEditVariableDialog.showDialog((VariableSubject) component,
                            (SimpleComponentSubject) component.getParent().getParent(),
                            mSelfRef);
                    }
                }
            }
            public void mousePressed(MouseEvent e)
            {
                int selRow = getRowForLocation(e.getX(), e.getY());
                TreePath selPath = getPathForLocation(e.getX(), e.getY());
                
                maybeShowPopup(e, selPath);
                
                //possibly open editor window
                if (selRow != -1)
                {
                    if (e.getClickCount() == 2)
                    {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                        
                        if (node == null)
                        {
                            return;
                        }
                        
                        Object nodeInfo = node.getUserObject();
                        AbstractSubject abstractComponent = (((ComponentInfo) nodeInfo).getComponent());
                        if (abstractComponent instanceof SimpleComponentSubject)
                        {
                            SimpleComponentSubject scp = (SimpleComponentSubject) abstractComponent;
                            
                            if (scp != null)
                            {
                                try
                                {
                                    ed = mModuleWindow.showEditor(scp);
                                }
                                catch (GeometryAbsentException g)
                                {
                                    JOptionPane.showMessageDialog(ModuleTree.this, g.getMessage());
                                }
                            }
                        }
                        if (abstractComponent instanceof VariableSubject)
                        {
                            VariableSubject v = (VariableSubject) abstractComponent;
                            if (v != null)
                            {
                                SimpleComponentSubject component = (SimpleComponentSubject)
                                ((ComponentInfo)((DefaultMutableTreeNode) node.getParent())
                                .getUserObject()).getComponent();
                                
                                EditorEditVariableDialog.showDialog(v, component, mSelfRef);
                            }
                        }
                    }
                }
            }
            
            public void mouseReleased(MouseEvent e)
            {
                TreePath selPath = getPathForLocation(e.getX(), e.getY());
                if(selPath == null) return;
                
                mSelfRef.setSelectionPath(selPath);
                maybeShowPopup(e, selPath);
            }
            
            private void maybeShowPopup(MouseEvent e, TreePath selPath)
            {
                //possibly show popup menu
                if (e.isPopupTrigger() && selPath != null)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                    if (node == null)
                    {
                        return;
                    }
                    Object nodeInfo = node.getUserObject();
                    AbstractSubject component =
                        (((ComponentInfo) nodeInfo).getComponent());
                    
                    ModuleTreePopupMenu popup = new ModuleTreePopupMenu(
                        mSelfRef, mModuleWindow, component);
                    popup.show(mSelfRef, e.getX(), e.getY());                    
                }
            }            
        };
        addMouseListener(ml);
    }
    
    public void addVariable(VariableSubject variable)
    {
        DefaultMutableTreeNode parentNode = null;
        DefaultMutableTreeNode v = makeTreeFromComponent(variable);
        TreePath parentPath = getSelectionPath();
        parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        if(((ComponentInfo) parentNode.getUserObject()).getComponent()
        instanceof SimpleComponentSubject)
        {
            addObject(parentNode, v.getUserObject(), true);
        }
        else if(((ComponentInfo) parentNode.getUserObject()).getComponent()
        instanceof VariableSubject)
        {
            addObject((DefaultMutableTreeNode) parentNode.getParent(), v.getUserObject(), true);
        }
        else
        {
            System.err.println("ModuleTree.addVariable(): can't add variable to node of this type");
        }
    }
    
    public void addComponent(AbstractSubject o)
    {
        if (mModule != null)
        {
            DefaultMutableTreeNode parentNode = null;
            TreePath parentPath = getSelectionPath();
            
            if (parentPath == null)
            {
                //There's no selection. Default to the root node.
                parentNode = mRootNode;
                
                expandPath(new TreePath(mRootNode.getPath()));
            }
            else
            {
                parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
            }
            
            ComponentInfo ci = (ComponentInfo) (parentNode.getUserObject());
            
            // We don't want InstanceSubject components having children
            if (!(ci.getComponent() instanceof ForeachSubject) &&!(o instanceof ParameterBindingSubject))
            {
                parentNode = mRootNode;
            }
            
            if (!(ci.getComponent() instanceof InstanceSubject) && (o instanceof ParameterBindingSubject))
            {
                return;
            }
            
            //Take care of adding variables
            if (o instanceof VariableSubject)
            {
                if(ci.getComponent() instanceof SimpleComponentSubject)
                {
                    parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                else if(ci.getComponent() instanceof VariableSubject)
                {
                    parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                    parentNode = (DefaultMutableTreeNode) parentNode.getParent();
                }
                else
                {
                    System.out.println("ModuleTree.addComponent(): Can't add variable to object of this type");
                }
            }
            
            DefaultMutableTreeNode newChild = makeTreeFromComponent((AbstractSubject) o);
            
            ((DefaultTreeModel) treeModel).insertNodeInto(newChild, parentNode, treeModel.getChildCount(parentNode));
            
            if (o instanceof ForeachSubject)
            {
                expandPath(new TreePath(newChild.getPath()));
            }
            
            if ((o instanceof SimpleComponentSubject))
            {
                SimpleComponentSubject scp = (SimpleComponentSubject) o;
            }
            
            expandPath(new TreePath(parentNode.getPath()));
        }
    }
    
    private DefaultMutableTreeNode makeTreeFromComponent(AbstractSubject e)
    {
        final String text = mPrinter.toString(e);
        final Object userobject = new ComponentInfo(text, e);
        if (e instanceof SimpleComponentSubject)
        {
            SimpleComponentSubject component = (SimpleComponentSubject) e;
            DefaultMutableTreeNode compNode = new DefaultMutableTreeNode(userobject, true);
            List<VariableProxy> variables = component.getVariables();
            for(VariableProxy variable: variables)
            {
                compNode.add(makeTreeFromComponent((VariableSubject) variable));
            }
            //return new DefaultMutableTreeNode(userobject, false);
            return compNode;
        }
        else if(e instanceof VariableSubject)
        {
            return new DefaultMutableTreeNode(userobject, false);
        }
        else if (e instanceof InstanceSubject)
        {
            final InstanceSubject inst = (InstanceSubject) e;
            final DefaultMutableTreeNode tmp =
                new DefaultMutableTreeNode(userobject, true);
            final List<ParameterBindingSubject> bindings =
                inst.getBindingListModifiable();
            for (final ParameterBindingSubject binding : bindings)
            {
                tmp.add(makeTreeFromComponent(binding));
            }
            return tmp;
        }
        else if (e instanceof ParameterBindingSubject)
        {
            return new DefaultMutableTreeNode(userobject, false);
        }
        else if (e instanceof ForeachSubject)
        {
            final ForeachSubject foreach = (ForeachSubject) e;
            final DefaultMutableTreeNode tn =
                new DefaultMutableTreeNode(userobject, true);
            final List<AbstractSubject> body = foreach.getBodyModifiable();
            for (final AbstractSubject item : body)
            {
                tn.add(makeTreeFromComponent(item));
            }
            return tn;
        }
        else
        {
            throw new IllegalArgumentException
                ("Don't know how to make tree from subject of type " +
                e.getClass().getName() + "!");
        }
    }
    
    
    /** Remove all nodes except the root node. */
    private void clear()
    {
        mRootNode.removeAllChildren();
        ((DefaultTreeModel) treeModel).reload();
    }
    
    /** Remove the currently selected node. */
    public void removeCurrentNode()
    {
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null)
        {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
            (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null)
            {
                ((DefaultTreeModel) treeModel).removeNodeFromParent(currentNode);
                return;
            }
        }
    }
    
    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child)
    {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath();
        
        if (parentPath == null)
        {
            parentNode = mRootNode;
        }
        else
        {
            parentNode = (DefaultMutableTreeNode)
            (parentPath.getLastPathComponent());
        }
        
        return addObject(parentNode, child, true);
    }
    
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
        Object child)
    {
        return addObject(parent, child, false);
    }
    
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
        Object child,
        boolean shouldBeVisible)
    {
        DefaultMutableTreeNode childNode =
            new DefaultMutableTreeNode(child);
        
        if (parent == null)
        {
            parent = mRootNode;
        }
        
        ((DefaultTreeModel) treeModel).insertNodeInto(childNode, parent,
            parent.getChildCount());
        
        //Make sure the user can see the lovely new node.
        if (shouldBeVisible)
        {
            scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }
    
    public DefaultMutableTreeNode getRoot()
    {
        return mRootNode;
    }
    
    public void updateSelectedNode()
    {
        TreePath selPath = this.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        selPath.getLastPathComponent();
        AbstractSubject c = ((ComponentInfo) node.getUserObject())
        .getComponent();
        String name = mPrinter.toString(c);
        node.setUserObject(new ComponentInfo(name, c));
        this.updateUI();
    }
}
