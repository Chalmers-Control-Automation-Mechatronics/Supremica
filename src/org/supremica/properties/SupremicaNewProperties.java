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
package org.supremica.properties;

import java.util.*;
import java.io.*;


/**
 * Properties for Supremica.
 */
public final class SupremicaNewProperties
	implements Iterable<Property>
{


	public static final StringProperty docDBServerName = new StringProperty(PropertyType.UNCLASSIFIED, "docdbHost", "", "No comments");
//	public static final IntProperty docDBServerPort = new IntProperty("docdbPort");

	private SupremicaNewProperties()
	{
	}

	public Iterator<Property> iterator()
	{
		return Property.iterator();
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (Iterator<Property> it = iterator(); it.hasNext(); )
		{
			Property currProperty = it.next();
			sb.append("# " + currProperty.getComment() + "\n");
			sb.append(currProperty.toString() + "\n\n");
		}
		return sb.toString();

	}

	/**
	 * Looks for "-p propertyFile" option, and loads it if it exists.
	 * Looks also for developer/user options
	 */
/*
	public void loadProperties(String[] args)
	{
		// do we  want developer stuff by default
		boolean enabled_developer_mode = true;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-p") || args[i].equals("--properties"))
			{
				if (i + 1 < args.length)
				{
					String fileName = args[i + 1];
					File propFile = new File(fileName);

					try
					{
						if (!propFile.exists())
						{
							System.out.println("Properties file not found: " + propFile.getAbsolutePath());
							System.out.println("Creating empty properties file: " + propFile.getAbsolutePath());
							propFile.createNewFile();
						}

						setProperties(propFile);
					}
					catch (Exception e)
					{
						System.err.println("Error reading properties file: " + propFile.getAbsolutePath());
					}
				}
			}
			else if (args[i].equals("--developer"))
			{
				enabled_developer_mode = true;
			}
			else if (args[i].equals("--user"))
			{
				enabled_developer_mode = false;
			}
		}

//		wp.setProperty(INCLUDE_EXPERIMENTAL_ALGORITHMS, enabled_developer_mode
														? "true"
														: "false", true);
//		updateBDDOptions(false);    // sync BDD options to the newly loaded options
	}
*/


	/**
	 * Save the property list to the configuration file.
	 *
	 * @param name is the name of the config file
	 */
	public void saveProperties(String filename)
		throws IOException
	{
		OutputStream os = new FileOutputStream(filename);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "8859_1"));

		writer.write("# Supremica configuration file");
		writer.newLine();
		writer.write("#" + new Date().toString());
		writer.newLine();

		for (Property currProperty : this)
		{
			if (currProperty.currentValueDifferentFromDefaultValue())
			{
				writer.append("# " + currProperty.getComment() + "\n");
				writer.append(currProperty.getPropertyType() + "." + currProperty.getKey() + " " + currProperty.valueToEscapedString()  + "\n\n");
			}
		}

		writer.flush();
		os.close();
	}
/*
	public void saveProperties()
		throws IOException
	{
		if (lastPropertyFile != null)
		{
			saveProperties(lastPropertyFile);
		}
		else
		{
			System.err.println("Could not write to configuration file, unknown file name.");
		}
	}

	public void setProperties(File aFile)
		throws Exception
	{
		lastPropertyFile = aFile.getAbsolutePath();    // save it for later days,,,,

		FileInputStream fStream = new FileInputStream(aFile);
		BufferedInputStream bStream = new BufferedInputStream(fStream);

		setProperties(bStream);
	}

	public void setProperties(InputStream iStream)
		throws Exception
	{
		Properties newProperties = new Properties();

		newProperties.load(iStream);
		setProperties(newProperties);
//		updateBDDOptions(false);
	}

	public static final void setProperties(Properties otherProperties)
	{
		for (Enumeration propEnum = otherProperties.propertyNames();
				propEnum.hasMoreElements(); )
		{
			String currKey = (String) propEnum.nextElement();

			if (wp.allowExternalModification(currKey))
			{
				wp.setProperty(currKey, otherProperties.getProperty(currKey));
			}
		}
	}

*/
	private String lastPropertyFile = null;
}
