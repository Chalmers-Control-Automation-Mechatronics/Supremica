package org.supremica.automata.algorithms.Guard;

import java.util.ArrayList;

/**
 *
 * @author Sajed Miremadi
 */
public class IDDNode {

    private String name = "";
    private String resAut = "";
    private String id;
    private ArrayList<Integer> reachedBy = new ArrayList<Integer>();

    public IDDNode(String id, String name)
    {
        this.name = name;
        this.id = id;
    }

    public IDDNode(String id, String name, String resAut)
    {
        this(id,name);
        this.resAut = resAut;
    }

    public String getName()
    {
        return name;
    }

    public String getResAut()
    {
        return resAut;
    }

    public String getID()
    {
        return id;
    }

    public void setReachedBy(ArrayList<Integer> states)
    {
        reachedBy = states;
    }

    public ArrayList<Integer> getReachedBy()
    {
        return reachedBy;
    }

}
