/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.util;

import java.io.*;

/**
 * This is used by the SOFT PLC runtime to load the generated classfiles
 **/
public class FileClassLoader
	extends ClassLoader
{
	private String root;

	public FileClassLoader()
	{
		super();
	}

	public FileClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	public FileClassLoader(String rootDir)
	{
		if (rootDir == null)
		{
			throw new IllegalArgumentException("Null root directory");
		}

		root = rootDir;
	}

	protected Class<?> loadClass(String name, boolean resolve)
		throws ClassNotFoundException
	{

		// Since all support classes of loaded class use same class loader
		// must check subclass cache of classes for things like Object
		Class<?> c = findLoadedClass(name);

		if (c == null)
		{
			try
			{
				c = findSystemClass(name);
			}
			catch (Exception e) {}
		}

		if (c == null)
		{

			// Convert class name argument to filename
			// Convert package names into subdirectories
			String filename = name.replace('.', File.separatorChar) + ".class";

			try
			{
				byte data[] = loadClassData(filename);

				c = defineClass(name, data, 0, data.length);

				if (c == null)
				{
					throw new ClassNotFoundException(name);
				}
			}
			catch (IOException e)
			{
				throw new ClassNotFoundException("Error reading file: " + filename);
			}
		}

		if (resolve)
		{
			resolveClass(c);
		}

		return c;
	}

	private byte[] loadClassData(String filename)
		throws IOException
	{

		// Create a file object relative to directory provided
		File f = new File(root, filename);

		// Get size of class file
		int size = (int) f.length();

		// Reserve space to read
		byte buff[] = new byte[size];

		// Get stream to read from
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);

		// Read in data
		dis.readFully(buff);

		// close stream
		dis.close();

		// return data
		return buff;
	}
}
