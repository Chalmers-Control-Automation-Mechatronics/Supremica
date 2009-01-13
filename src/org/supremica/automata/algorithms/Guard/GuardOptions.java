/*
 * GuardOptions.java
 *
 * Created on May 7, 2008, 6:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
	private boolean expressionType;    // if true: the guard expression will be generated from the allowed states, else: from forbidden states
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
		this.expressionType = true;
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

	public boolean getExpressionType()
        {
            return expressionType;
        }
        
        public String getEvent()
        {
            return event;
        }
        
        public void setExpressionType(boolean set)
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
		options.setExpressionType(true);
		options.setEvent(""); 
		return options;
	}
}

