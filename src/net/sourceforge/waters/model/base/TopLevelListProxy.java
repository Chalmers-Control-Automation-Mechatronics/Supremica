//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   TopLevelListProxy
//###########################################################################
//# $Id: TopLevelListProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.io.IOException;

import net.sourceforge.waters.xsd.base.ElementType;


/**
 * <P>An list of named elements that has a directly corresponding XML
 * representation.</P>
 *
 * <P>A top-level list is an array list implementation that can also be
 * used as a {@link Proxy} in the Waters framework. The functionality
 * inherited from superclass {@link ArrayListProxy} is extended to
 * facilitate pretty-printing.</P>
 *
 * @author Robi Malik
 */

public abstract class TopLevelListProxy extends ArrayListProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty top-level list.
   */
  protected TopLevelListProxy()
  {
  }

  /**
   * Creates a top-level list.
   * @param  input      The initial contents of the new list.
   */
  protected TopLevelListProxy(final Collection input)
  {
    super(input);
  }

  /**
   * Creates a top-level list from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly; use class {@link ProxyMarshaller} instead.
   * @param  parent      The parsed XML structure of the element containing
   *                     a list of XML representations of the list elements.
   * @param  factory     A factory used to retrieve and convert the list
   *                     elements from the parent XML structure.
   * @throws DuplicateNameException to indicate that the list of XML
   *                     elements contains two different elements with the
   *                     same name.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  protected TopLevelListProxy(final ElementType parent,
			      final ProxyFactory factory)
    throws ModelException
  {
    super(parent, factory);
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    if (size() > 0) {
      if (getPPrintName() != null) {
	printer.print(getPPrintName());
	printer.print(' ');
      }
      super.pprint(printer);
      printer.println();
    }
  }


  //#########################################################################
  //# Provided by Users
  protected String getPPrintName()
  {
    return null;
  }

}
