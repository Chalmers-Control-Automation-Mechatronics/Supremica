package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;

import java.util.*;

import org.jgrafchart.Transitions.*;


public class GenericTransition
	extends GrafcetObject
{

	public SimpleNode node;

	// public JGoText myLabel = null;
	public GenericTransition()
	{
		super();
	}

	public String getLabelText()
	{
		return " ";
	}

	public void setLabelText(String s) {}

	public void setTextColor(Color s) {}

	public void testAndFire() {}

	public void addSucceedingStep(GrafcetObject s) {}

	public void addPrecedingStep(GrafcetObject s) {}

	public void compileStructure() {}
}
