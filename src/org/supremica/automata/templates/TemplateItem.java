
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
package org.supremica.automata.templates;

import java.net.URL;
import org.supremica.automata.IO.*;
import org.supremica.automata.*;

public class TemplateItem
{
	private String description;
	private String path;

	public TemplateItem(String description, String path)
	{
		this.description = description;
		this.path = path;
	}

	public String getDescription()
	{
		return description;
	}

	public String getPath()
	{
		return path;
	}

	public Project createInstance(ProjectFactory theFactory)
		throws Exception
	{
		try
		{
			URL url = TemplateItem.class.getResource(path);

			//InputStream stream = url.openStream();
			ProjectBuildFromXml builder = new ProjectBuildFromXml(theFactory);
			Project theProject = builder.build(url);

			return theProject;
		}
		catch (Exception ex)
		{

			// logger.error("Exception building project." + ex);
			// logger.debug(ex.getStackTrace());
			throw ex;
		}
	}
}
