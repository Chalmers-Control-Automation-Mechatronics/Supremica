//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   Proxy
//###########################################################################
//# $Id: Proxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;


/**
 * <P>The common functionality for all Waters elements.</P>
 *
 * <P>Every implementation of this interface corresponds to a particular
 * type of element in some of the Waters XML files. The interface specifies
 * the common basic functionality of printing and marshalling that needs to
 * be implemented by all classes that represent an element from one of the
 * XML files.</P>
 *
 * <P>Technically, most implementations of this interface are also
 * subclasses of the {@link ElementProxy} class, which provides some
 * additional functionality. In some cases, however, another base class has
 * to be used. Therefore the only guaranteed common functionality for all
 * objects found in a Waters document is that specified in this
 * interface.</P>
 * 
 * @author Robi Malik
 */

public interface Proxy {

  //#########################################################################
  //# Comparing
  /**
   * Checks whether two elements are equal and have the same geometry
   * information.  This method implements content-based equality, i.e., two
   * elements will be equal if their contents are the same. While the
   * standard {@link java.lang.Object#equals(Object) equals()} method only
   * considers structural contents, this method also takes the layout
   * information of graphical objects such as nodes and edges into
   * account. This method can be very slow for large structures and is
   * intended for testing purposes only.
   */
  public boolean equalsWithGeometry(final Object partner);


  //#########################################################################
  //# Printing
  /**
   * Writes a textual description of this element.
   * This method is for internal use and should not be called directly.
   * @param  printer The printer object to which output is to be sent.
   * @throws IOException if printing failed for some reason.
   * @see ModelPrinter
   */
  public void pprint(final ModelPrinter printer) throws IOException;

  /**
   * Writes a textual description of this element followed by a newline.
   * This method is for internal use and should not be called directly.
   * @param  printer The printer object to which output is to be sent.
   * @throws IOException if printing failed for some reason.
   * @see ModelPrinter
   */
  public void pprintln(final ModelPrinter printer) throws IOException;


  //#########################################################################
  //# Marshalling
  /**
   * Creates a new JAXB element representing the contents of this element.
   * This method is used internally for marshalling and should not
   * be called directly.
   * @param  factory The element factory to be used for creating the
   *                 JAXB element.
   * @return The created element.
   * @throws JAXBException to indicate that a fatal error occurred while
   *                 creating or copying some element.
   * @see    ProxyMarshaller
   */
  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException;

}
