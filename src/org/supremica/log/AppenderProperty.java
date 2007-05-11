//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.log
//# CLASS:   AppenderProperty
//###########################################################################
//# $Id: AppenderProperty.java,v 1.2 2007-05-11 12:09:23 flordal Exp $
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.log;

import org.supremica.properties.BooleanProperty;
import org.supremica.properties.PropertyType;


/**
 * A property representing whether logging to a certain destination
 * is enabled. This is a {@link BooleanProperty} with the additional
 * ability to update the behaviour of the logging system when it is
 * set.
 *
 * Presently, this includes two Supremica properties:
 * <LI>
 * <UL>{@link Config#LOG_TO_CONSOLE}</UL>
 * <UL>{@link Config#LOG_TO_GUI}</UL>
 * </LI>
 *
 * @author Robi Malik
 */

public class AppenderProperty
    extends BooleanProperty
{
    
    //#######################################################################
    //# Constructors
    public AppenderProperty(final PropertyType type,
        final String key,
        final boolean value,
        final String comment)
    {
        super(type, key, value, comment);
    }
    
    public AppenderProperty(final PropertyType type,
        final String key,
        final boolean value,
        final String comment,
        final boolean immutable)
    {
        super(type, key, value, comment, immutable);
    }
    
    
    //#######################################################################
    //# Overrides for Abstract Base Class org.supremica.properties.Property
    public void set(final boolean value)
    {
        super.set(value);
        LoggerFactory.updateProperty(this);
    }
    
}
