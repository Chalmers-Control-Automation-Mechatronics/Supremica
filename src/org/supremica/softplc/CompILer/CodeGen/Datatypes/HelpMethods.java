
/**HelpMethods provides users of this package for IEC 6-1131-3 dataypes
 * with general methods, e.g. parsing
 */
package org.supremica.softplc.CompILer.CodeGen.Datatypes;

import java.util.*;

public class HelpMethods
{
	public static TypeBOOL parseBOOL(String s)
	{
		StringTokenizer tokens = new StringTokenizer(s, "#", false);
		int TokenCount = tokens.countTokens();
		String st1 = tokens.nextToken();

		if (TokenCount > 2)
		{
			throw new NumberFormatException("Error parsing BOOL: " + s);
		}

		if (TokenCount == 2)
		{
			if (!st1.equals("BOOL"))
			{
				throw new NumberFormatException("Error parsing BOOL: " + s);
			}

			st1 = tokens.nextToken();
		}

		if (st1.equals("0") || st1.equals("FALSE"))
		{
			return new TypeBOOL(false);
		}
		else
		{
			return new TypeBOOL(true);
		}
	}

	public static TypeConstant parseTypeConstants(String s)
	{
		if (s.equals("BOOL"))
		{
			return TypeConstant.T_BOOL;
		}
		else if (s.equals("SINT"))
		{
			return TypeConstant.T_SINT;
		}
		else if (s.equals("INT"))
		{
			return TypeConstant.T_INT;
		}
		else if (s.equals("DINT"))
		{
			return TypeConstant.T_DINT;
		}
		else if (s.equals("LINT"))
		{
			return TypeConstant.T_LINT;
		}
		else if (s.equals("USINT"))
		{
			return TypeConstant.T_USINT;
		}
		else if (s.equals("UINT"))
		{
			return TypeConstant.T_UINT;
		}
		else if (s.equals("UDINT"))
		{
			return TypeConstant.T_UDINT;
		}
		else if (s.equals("ULINT"))
		{
			return TypeConstant.T_ULINT;
		}
		else if (s.equals("REAL"))
		{
			return TypeConstant.T_REAL;
		}
		else if (s.equals("LREAL"))
		{
			return TypeConstant.T_LREAL;
		}
		else if (s.equals("TIME"))
		{
			return TypeConstant.T_TIME;
		}
		else if (s.equals("DATE"))
		{
			return TypeConstant.T_DATE;
		}
		else if (s.equals("TOD"))
		{
			return TypeConstant.T_TOD;
		}
		else if (s.equals("DT"))
		{
			return TypeConstant.T_DT;
		}
		else if (s.equals("STRING"))
		{
			return TypeConstant.T_STRING;
		}
		else if (s.equals("BYTE"))
		{
			return TypeConstant.T_BYTE;
		}
		else if (s.equals("WORD"))
		{
			return TypeConstant.T_WORD;
		}
		else if (s.equals("DWORD"))
		{
			return TypeConstant.T_DWORD;
		}
		else if (s.equals("LWORD"))
		{
			return TypeConstant.T_LWORD;
		}
		else if (s.equals("WSTRING"))
		{
			return TypeConstant.T_WSTRING;
		}
		else if (s.equals("DERIVED"))
		{
			return TypeConstant.T_DERIVED;
		}

		return null;
	}

	public static TypeANY_NUM parseANY_NUM(String s)
	{
		StringTokenizer tokens = new StringTokenizer(s, "#", false);
		int TokenCount = tokens.countTokens();
		String st1 = tokens.nextToken();

		switch (TokenCount)
		{

		case 1 :
			if (st1.indexOf(".") != -1)
			{
				return null;
			}    // new TypeREAL(removeDashes(st1));}
			else
			{
				return null;
			}    // new TypeINT(removeDashes(st1),10);}
		case 2 :
			if (st1.equals("2"))
			{
				return null;
			}    // new TypeINT(removeDashes(tokens.nextToken()),2);}
			else if (st1.equals("8"))
			{
				return null;
			}    // new TypeINT(removeDashes(tokens.nextToken()),8);}
			else if (st1.equals("16"))
			{
				return null;
			}    // new TypeINT(removeDashes(tokens.nextToken()),16);}
			else if (st1.equals("SINT"))
			{
				return null;
			}    // new TypeSINT(removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("INT"))
			{
				return null;
			}    // new TypeINT(removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("DINT"))
			{
				return new TypeDINT(removeDashes(tokens.nextToken()), 10);
			}
			else if (st1.equals("LINT"))
			{
				return null;
			}    // new TypeLINT (removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("USINT"))
			{
				return null;
			}    // new TypeUSINT(removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("UINT"))
			{
				return null;
			}    // new TypeUINT(removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("UDINT"))
			{
				return null;
			}    // new TypeUDINT(removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("ULINT"))
			{
				return null;
			}    // new TypeULINT(removeDashes(tokens.nextToken()),10);}
			else if (st1.equals("REAL"))
			{
				return new TypeREAL(removeDashes(tokens.nextToken()));
			}
			else if (st1.equals("LREAL"))
			{
				return null;
			}    // new TypeLREAL(removeDashes(tokens.nextToken()));}
		case 3 :
			int radix = Integer.parseInt(tokens.nextToken());

			if (st1.equals("SINT"))
			{
				return null;
			}    // new TypeSINT(removeDashes(tokens.nextToken()),radix);}
			else if (st1.equals("INT"))
			{
				return null;
			}    // new TypeINT(removeDashes(tokens.nextToken()),radix);}
			else if (st1.equals("DINT"))
			{
				return new TypeDINT(removeDashes(tokens.nextToken()), radix);
			}
			else if (st1.equals("LINT"))
			{
				return null;
			}    // new TypeLINT (removeDashes(tokens.nextToken()),radix);}
			else if (st1.equals("USINT"))
			{
				return null;
			}    // new TypeUSINT(removeDashes(tokens.nextToken()),radix);}
			else if (st1.equals("UINT"))
			{
				return null;
			}    // new TypeUINT(removeDashes(tokens.nextToken()),radix);}
			else if (st1.equals("UDINT"))
			{
				return null;
			}    // new TypeUDINT(removeDashes(tokens.nextToken()),radix);}
			else if (st1.equals("ULINT"))
			{
				return null;
			}    // new TypeULINT(removeDashes(tokens.nextToken()),radix);}
		}

		throw new NumberFormatException("Error when parsing ANY_NUM: " + s);
	}

	/**removeDashes is a private method used to remove dashes ('_') from
	 * a string
	 * @param s the string dashes should be removed from
	 * @return s without dashes */
	private static String removeDashes(String s)
	{
		StringTokenizer tokens = new StringTokenizer(s, "_", false);
		String outString = new String();

		for (; tokens.countTokens() > 0; )
		{
			outString = outString.concat(tokens.nextToken());
		}

		return outString;
	}
}
