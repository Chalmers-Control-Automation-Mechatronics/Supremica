package org.supremica.external.fbd2smv.isagrafReader;

public class BOX implements java.lang.Comparable
{
    public String index;
    public String name;
    public int x;
    public int y;

    public BOX(String index, String name, String x, String y){
	Integer I = null;

	this.index = index;
	this.name = name;
	this.x = I.valueOf(x).intValue();
	this.y = I.valueOf(y).intValue();
    }

    public int compareTo(Object o)
    {
	int obj_x;
	int obj_y;

	obj_x = ((BOX)o).x;
	obj_y = ((BOX)o).y;

	if (this.y < obj_y)
	    {
		return -1;
	    }
	else 
	    {if (this.y == obj_y)
		{
		    if (this.x < obj_x) 
			{
			    return -1;
			}
		    else if (this.x == obj_x) 
			{
			    return 0;
			}
		    else
			{
			    return 1;
			}
		}
	    else
		{
		    return 1;
		}
	    }

    }

    public int hashCode()
	{
		return index.hashCode();
	}

	public boolean equals(Object other)
	{
		return index.equals(((BOX)other).index);
	}

	public String getIndex()
	{
		return index;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String toString()
	{
		return "Index: " + index + " Name: " + name + " X: " + x + " Y:" + y;
	}
}
