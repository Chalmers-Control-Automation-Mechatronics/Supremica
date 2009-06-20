package org.supremica.automata.algorithms.Guard;

import java.util.HashSet;

/**
 *
 * @author Sajed Miremadi
 */
public class MDDNode {

    private HashSet<MDDNode> children = new HashSet<MDDNode>();
    private HashSet<MDDNode> parents = new HashSet<MDDNode>();
    private String name = "";
    private String resAut = "";
    private int id;
    private HashSet<Integer> reachedBy = new HashSet<Integer>();

    public MDDNode(int id, String name)
    {
        this.name = name;
        this.id = id;
    }

    public MDDNode(int id, String name, String resAut)
    {
        this(id,name);
        this.resAut = resAut;
    }

    public void addChild(MDDNode node)
    {
        children.add(node);
    }

    public void addParent(MDDNode node)
    {
        parents.add(node);
    }

    public String getName()
    {
        return name;
    }

    public String getResAut()
    {
        return resAut;
    }

    public HashSet<MDDNode> getChildren()
    {
        return children;
    }

    public HashSet<MDDNode> getParents()
    {
        return parents;
    }

    public int getID()
    {
        return id;
    }

    public boolean isRoot()
    {
        if(parents.size() == 0)
            return true;
        return false;
    }

    public void setReachedBy(HashSet<Integer> states)
    {
        reachedBy = states;
    }

    public HashSet<Integer> getReachedBy()
    {
        return reachedBy;
    }

}
