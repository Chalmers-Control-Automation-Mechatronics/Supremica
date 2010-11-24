package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sajed Miremadi
 */
public class IDD {

    static int nodeID = -1;

    private IDDNode root;
    private ArrayList<IDD> children;
    private ArrayList<IDD> parents;
    private ArrayList<ArrayList<String>> labels;

    private boolean visitedDuringCounting = false;
    private boolean oneTerminalIDD = false;
    private boolean zeroTerminalIDD = false;

    public IDD(IDDNode root, ArrayList<IDD> children, ArrayList<ArrayList<String>> labels)
    {
        if(root.getID().equals("1"))
            oneTerminalIDD = true;

        if(root.getID().equals("0"))
            zeroTerminalIDD = true;

        this.root = root;
        this.children = children;
        this.labels = labels;
        this.parents = new ArrayList<IDD>();
        if(children.size() > 0)
            for(IDD iddChild:children)
            {
                iddChild.parents.add(this);
            }
    }

    public IDD(IDDNode root)
    {
        this(root, new ArrayList<IDD>(),new ArrayList<ArrayList<String>>());
    }

    public boolean isOneTerminal()
    {
        return oneTerminalIDD;
    }

    public boolean isZeroTerminal()
    {
        return zeroTerminalIDD;
    }

    public int indexOfChildWithRoot(IDDNode node)
    {
        int index = -1;
        for(IDD child:children)
        {
            if(child.getRoot().getID().equals(node.getID()))
            {
                index = children.indexOf(child);
                break;
            }
        }

        return index;
    }

    public boolean isVisitedDuringCounting()
    {
        return visitedDuringCounting;
    }

    public int nbrOfNodes()
    {
//        System.out.println("node: "+root.getName());
        int nbr = 1;
        visitedDuringCounting = true;
        for(IDD child:children)
        {
            if(!child.isVisitedDuringCounting())
            {
                nbr += child.nbrOfNodes();
//                System.out.println(labelOfChild(child));
            }
        }
        
        return nbr;
    }

    public void addChild(IDD iddChild, ArrayList<String> label)
    {
        iddChild.parents.add(this);
        children.add(iddChild);
        labels.add(label);
    }

    public ArrayList<IDD> getParents()
    {
        return parents;
    }

    public ArrayList<String> labelOfChild(IDD child)
    {
        if(labels.size() > 0 && children.contains(child))
            return labels.get(children.indexOf(child));
        else
            return null;
    }

    public void setRoot(final IDDNode node)
    {
        root = node;
    }

    public IDDNode getRoot()
    {
        return root;
    }
    
    public ArrayList<IDD> getChildren()
    {
        return children;
    }

    public int nbrOfChildren()
    {
        return children.size();
    }

    public IDD getIthChild(int i)
    {
        return children.get(i);
    }

    public IDDNode createNode(final String name)
    {
        nodeID++;
        return new IDDNode(""+nodeID,name);
    }


}
