package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;

/**
 * @author Sajed Miremadi
 */

public class IDD {

    static int nodeID = -1;

    private IDDNode root;
    private final ArrayList<IDD> children;
    private final ArrayList<IDD> parents;
    private final ArrayList<ArrayList<String>> labels;

    private boolean visitedDuringCounting = false;
    private boolean oneTerminalIDD = false;
    private boolean zeroTerminalIDD = false;

    public IDD(final IDDNode root, final ArrayList<IDD> children, final ArrayList<ArrayList<String>> labels)
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
            for(final IDD iddChild:children)
            {
                iddChild.parents.add(this);
            }
    }

    public IDD(final IDDNode root)
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

    public int indexOfChildWithRoot(final IDDNode node)
    {
        int index = -1;
        for(final IDD child:children)
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
        for(final IDD child:children)
        {
            if(!child.isVisitedDuringCounting())
            {
                nbr += child.nbrOfNodes();
//                System.out.println(labelOfChild(child));
            }
        }

        return nbr;
    }

    public void addChild(final IDD iddChild, final ArrayList<String> label)
    {
        iddChild.parents.add(this);
        children.add(iddChild);
        labels.add(label);
    }

    public ArrayList<IDD> getParents()
    {
        return parents;
    }

    public ArrayList<String> labelOfChild(final IDD child)
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

    public IDD getIthChild(final int i)
    {
        return children.get(i);
    }

    public IDDNode createNode(final String name)
    {
        nodeID++;
        return new IDDNode(""+nodeID,name);
    }


}
