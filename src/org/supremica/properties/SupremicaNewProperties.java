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
 * All properties are added in the Config class.
 **/
public final class SupremicaNewProperties
	implements Iterable<Property>
{
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
	public void loadProperties(String[] args)
	{
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
							System.err.println("Properties file not found: " + propFile.getAbsolutePath());
							System.err.println("Creating empty properties file: " + propFile.getAbsolutePath());
							propFile.createNewFile();
						}

						updateProperties(propFile);
					}
					catch (Exception e)
					{
						System.err.println("Error reading properties file: " + propFile.getAbsolutePath());
					}
				}
			}
		}
	}


	private Properties buildProperties(File theFile)
		throws FileNotFoundException, IOException
	{
		FileInputStream theStream = new FileInputStream(theFile);
		return buildProperties(new BufferedInputStream(theStream));
	}

	private Properties buildProperties(InputStream inStream)
		throws IOException
	{
		Properties newProperties = new Properties();
		newProperties.load(inStream);
		return newProperties;
	}

	public void updateProperties(File propertyFile)
		throws FileNotFoundException, IOException
	{
		Properties propertiesFromFile = buildProperties(propertyFile);
		for (Enumeration e = propertiesFromFile.keys(); e.hasMoreElements(); )
		{
			String newKey = (String)e.nextElement();
			String newValue = propertiesFromFile.getProperty(newKey);

			Property orgProperty = Property.getProperty(newKey);
			if (orgProperty == null)
			{
				System.err.println("Unknown property: " + newKey);
			}
			else if (orgProperty.isImmutable())
			{
				System.err.println("Property \"" + newKey + "\" is immutable");

			}
			else
			{
				try
				{
					orgProperty.set(newValue);
				}
				catch (IllegalArgumentException ex)
				{
					System.err.println("Invalid argument to key: " + newKey);
				}
			}
		}
	}

	public void saveProperties()
		throws IOException
	{
		saveProperties(false);
	}

	public void saveProperties(boolean saveAll)
		throws IOException
	{
		if (lastPropertyFile != null)
		{
			saveProperties(lastPropertyFile, saveAll);
		}
		else
		{
			System.err.println("Could not write to configuration file, unknown file name.");
		}
	}

	/**
	 * Save the property list to the configuration file.
	 *
	 * @param filename is the name of the config file
	 * @param saveAll if this is true all mutable properties are saved to file
	 * otherwise only those properties that values different from the default value is saved.
	 */
	public void saveProperties(String filename, boolean saveAll)
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
			if ((saveAll && !currProperty.isImmutable()) || currProperty.currentValueDifferentFromDefaultValue())
			{
				writer.append("# " + currProperty.getComment() + "\n");
				writer.append(currProperty.getPropertyType() + "." + currProperty.getKey() + " " + currProperty.valueToEscapedString()  + "\n\n");
			}
		}

		writer.flush();
		os.close();
	}

	private static SupremicaNewProperties supremicaProperties;
	private static Config config = Config.instance;
	private String lastPropertyFile = null;

	static
	{
		supremicaProperties = new SupremicaNewProperties();
	}


	public static void main(String[] args)
	{
		System.out.println(supremicaProperties.toString());
	}
}
