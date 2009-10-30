//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   SupremicaPropertyChangeEvent
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.properties;

import java.util.EventObject;


public class SupremicaPropertyChangeEvent extends EventObject
{

    //#######################################################################
    //# Constructor
    public SupremicaPropertyChangeEvent(final Property property,
                                        final String oldvalue,
                                        final String newvalue)
    {
        super(property);
        mOldValue = oldvalue;
        mNewValue = newvalue;
    }


    //#######################################################################
    //# Simple Access
    public Property getSource()
    {
        return (Property) super.getSource();
    }

    public String getOldValue()
    {
        return mOldValue;
    }

    public String getNewValue()
    {
        return mNewValue;
    }


    //#######################################################################
    //# Data Members
    private final String mOldValue;
    private final String mNewValue;


    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

}
