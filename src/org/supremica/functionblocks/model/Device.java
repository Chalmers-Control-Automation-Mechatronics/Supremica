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
/**
 * @author Goran Cengic
 */

package org.supremica.functionblocks.model;

import java.util.*;

public class Device extends NamedObject
{

    private Resource managementResource;
    private Map resources = new HashMap();

    private Loader loader;

	private int threads;

    private Device() {}

    public Device(String name, String systemFileName, String libraryPathBase, String libraryPath, int threads)
    {
		this.threads = threads;
		setName(name);
		System.out.println("Device(" + getName() + ", " + systemFileName + ", " + libraryPathBase + ", " + libraryPath + ")");
		loader = new Loader(this, systemFileName, libraryPathBase, libraryPath);
	}

    public void run()
    {
		System.out.println("Device.runDevice()");
		for (Iterator iter = resources.keySet().iterator();iter.hasNext();)
		{
			getResource((String) iter.next()).run();
		}
    }

    public void addResource(String name)
    {
		resources.put(name,new Resource(name,this,threads));
    }

    public Resource getResource(String name)
    {
		return (Resource) resources.get(name);
    }

}
