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
package org.supremica.automata.IO;

import org.supremica.log.*;

public class EncodingHelper
{
	private static Logger logger = LoggerFactory.createLogger(EncodingHelper.class);

	private EncodingHelper()
	{
	}

	/**
	 * Places line breaks '\n' at as even distances as possible.
	 * There ought to be a method like this in the JDK somewhere?
	 */
	public static String linebreakAdjust(String input)
	{
		return linebreakAdjust(input, 50);
	}

	public static String linebreakAdjust(String input, int lineWidth)
	{
		if (input == null)
			return "";

		// Normalize string
		String s = input.replaceAll("[\n\r]", " ");
		s = s.replaceAll(" +", " ");

		int len = s.length();
		int lastBreak = 0;

		while (len - lastBreak > lineWidth)
		{
			int nextBreak = s.lastIndexOf(' ', lastBreak + lineWidth);
			if (nextBreak == -1)
				nextBreak = lastBreak + lineWidth;
			s = s.substring(0, nextBreak) + "\n" + s.substring(nextBreak+1,len);
			lastBreak = nextBreak;
		}

		return s;
	}
	
	public static String normalize(String input, boolean replacedot)
	{
		String s = input;
/*
		try
		{
			s = new String(input.getBytes("UTF-8"), "ISO-8859-1");
			//logger.info("org: " + input + " new: " + s);
		}
		catch (UnsupportedEncodingException ex)
		{
			logger.error("UTF-8 is an unsupported encoding");
			throw new RuntimeException("UTF-8 is an unsupported encoding");
		}
		return s.toString();
*/
		StringBuffer str = new StringBuffer();
		int len = (s != null)
				  ? s.length()
				  : 0;

		for (int i = 0; i < len; i++)
		{
			char ch = s.charAt(i);

			switch (ch)
			{
			case '.' :
			{
				if (replacedot)
				{
					str.append("_");
				}
				else
				{
					str.append(".");
				}
				break;
			}

			case '<' :
			{
				str.append("&lt;");

				break;
			}
			case '>' :
			{
				str.append("&gt;");

				break;
			}
			case '&' :
			{
				str.append("&amp;");

				break;
			}
			case '"' :
			{
				str.append("&quot;");

				break;
			}
			case '\r' :
			case '\n' :
			{
				// else, default append char
			}
			default :
			{
				str.append(ch);
			}
			}
		}

		return str.toString();
	}

	public static String normalize(String input)
	{
		return EncodingHelper.normalize(input, false);
	}
}
