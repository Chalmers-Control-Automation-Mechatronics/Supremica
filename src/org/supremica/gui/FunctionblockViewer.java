
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
package org.supremica.gui;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.supremica.functionblocks.xsd.datatype.DataType;
import org.supremica.functionblocks.xsd.libraryelement.JaxbSystem;
import org.supremica.functionblocks.xsd.libraryelement.JaxbFBType;
import javax.xml.bind.JAXBException;


public class FunctionblockViewer
{


	public static String getExtension(File f)
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1)
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	public static void main(String args[])
		throws JAXBException
	{
		if (args.length <= 0)
		{
			System.err.println("Usage: FunctionblockViewer file.ext");
		}

		JAXBContext context = JAXBContext.newInstance("org.supremica.functionblocks.xsd.datatype:org.supremica.functionblocks.xsd.libraryelement:org.supremica.functionblocks.xsd.fbmanagement:");
		Unmarshaller unmarshaller = context.createUnmarshaller();
		unmarshaller.setValidating(false);
		Marshaller marshaller = context.createMarshaller();
    	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		for (int i = 0; i < args.length; i++)
		{
			System.out.println("Reading: " + args[i]);
			File currFile = new File(args[i]);
			String currExtension = FunctionblockViewer.getExtension(currFile);

			if ("dtp".equalsIgnoreCase(currExtension))
			{ // DataType:DataType
				DataType dataType = (DataType)unmarshaller.unmarshal(currFile);
				marshaller.marshal(dataType, System.out);
			}
			else if ("sys".equalsIgnoreCase(currExtension))
			{ // LibraryElement:System
				JaxbSystem system = (JaxbSystem)unmarshaller.unmarshal(currFile);
				marshaller.marshal(system, System.out);
			}
			else if ("fbt".equalsIgnoreCase(currExtension))
			{ // LibraryElement:FBType

				JaxbFBType fbType = (JaxbFBType)unmarshaller.unmarshal(currFile);
				marshaller.marshal(fbType, System.out);
			}
			else
			{ // Unknown or nonexisting extension
				System.err.println("Unknown or nonexisting extension");
			}

		}
	}

}


