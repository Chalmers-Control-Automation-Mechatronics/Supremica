
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ComponentInfo
//###########################################################################
//# $Id: ComponentInfo.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import java.util.ArrayList;
import java.beans.*;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class ComponentInfo
{
	ElementProxy e;
	String name;

	public ComponentInfo(String name, ElementProxy e)
	{
		this.e = e;
		this.name = name;
	}

	public String toString()
	{
		return name;
	}

	public ElementProxy getComponent()
	{
		return e;
	}

	public String getName()
	{
		return name;
	}
}
