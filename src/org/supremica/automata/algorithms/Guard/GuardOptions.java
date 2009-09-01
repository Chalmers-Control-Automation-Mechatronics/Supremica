//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata.algorithms.Guard
//# CLASS:   GuardOptions
//###########################################################################
//# $Id$
//###########################################################################

/*
 * GuardOptions.java
 *
 * Created on May 7, 2008, 6:36 PM
 */

package org.supremica.automata.algorithms.Guard;

import org.supremica.automata.*;
import org.supremica.log.*;
import org.supremica.properties.Config;
import org.supremica.util.SupremicaException;

/**
 *
 * @author Sajed
 */

public final class GuardOptions
{
	private static Logger logger = LoggerFactory.createLogger(GuardOptions.class);
	private String event;    
	private int expressionType;    // 0: the guard expression will be generated from the forbidden states; 1: from forbidden states; 2: Optimal case
    // I do not understand. 0 and 1 seem to be the same.
    // Please clarify, or better use an enumeration. ~~~ Robi
    private boolean dialogOK = false;
    private int nbrOfExecuters;

	public GuardOptions()
	{
		this.nbrOfExecuters = Config.SYNC_NBR_OF_EXECUTERS.get();
		//The following check should ideally be done within SupremicaProperties
		if (this.nbrOfExecuters > 1 )
		{
			//			throw new SupremicaException("Error in SupremicaProperties. The property synchNbrOfExecuters must be at least 1.");
		}

		this.event = "";
		this.expressionType = 2;
	}

	public void setDialogOK(boolean bool)
	{
		dialogOK = bool;
	}

	public boolean getDialogOK()
	{
		return dialogOK;
	}

	public int getNbrOfExecuters()
	{
		return nbrOfExecuters;
	}

	public int getExpressionType()
    {
        return expressionType;
    }

    public String getEvent()
    {
        return event;
    }
        
    public void setExpressionType(int set)
	{
		expressionType = set;
	}
        
        public void setEvent(String set)
	{
		event = set;
	}


	public boolean isValid()
	{
		if (nbrOfExecuters > 1)
		{
                    return false;
		}

		return true;
	}

	public static GuardOptions getDefaultGuardOptions()
	{
		GuardOptions options = new GuardOptions();
		options.setExpressionType(2);
		options.setEvent(""); 
		return options;
	}
}

