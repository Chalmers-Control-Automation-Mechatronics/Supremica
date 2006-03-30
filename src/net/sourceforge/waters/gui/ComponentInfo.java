//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentInfo
//###########################################################################
//# $Id: ComponentInfo.java,v 1.4 2006-03-30 15:21:55 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.subject.base.AbstractSubject;


public class ComponentInfo
{

	//#######################################################################
	//# Constructors
	public ComponentInfo(final String name, final AbstractSubject subject)
	{
		mName = name;
		mSubject = subject;
	}


	//#######################################################################
	//# Overrides for baseclass java.lang.Object
	public String toString()
	{
		return mName;
	}


	//#######################################################################
	//# Simple Access Methods
	public AbstractSubject getComponent()
	{
		return mSubject;
	}

	public String getName()
	{
		return mName;
	}

	//#######################################################################
	//# Data Members
	private final AbstractSubject mSubject;
	private final String mName;
}
