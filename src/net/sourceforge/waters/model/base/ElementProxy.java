//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   ElementProxy
//###########################################################################
//# $Id: ElementProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;


/**
 * <P>The common base class of most Waters elements.</P>
 *
 * <P>Every subclass of this class corresponds to a particular type of
 * element in some of the Waters XML files. This abstract base class
 * provides common basic functionality for comparing, printing, and
 * marshalling all elements in a uniform way.</P>
 * 
 * @author Robi Malik
 */

public abstract class ElementProxy implements Proxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty element.
   */
  protected ElementProxy()
  {
  }

  /**
   * Creates a copy of an element.
   * @param  partner     The object to be copied from.
   */
  protected ElementProxy(final ElementProxy partner)
  {
  }


  //#########################################################################
  //# Equals and Hashcode
  /**
   * Checks whether two elements are equal.
   * This method implements content-based equality, i.e., two elements
   * will be equal if their contents are the same. This method can
   * be slow for large structures and therefore should be used with
   * care.
   * @see #equalsWithGeometry(Object) equalsWithGeometry()
   */
  public boolean equals(final Object partner)
  {
    return partner != null && getClass() == partner.getClass();
  }

  /**
   * Checks whether two elements are equal and have the same geometry
   * information. This method implements content-based equality, i.e., two
   * elements will be equal if their contents are the same. While the
   * standard {@link #equals(Object) equals()} method only considers structural
   * contents, this method also takes the layout information of graphical
   * objects such as nodes and edges into account. This method is very slow
   * for large structures and so far is only used for testing purposes.
   */
  public boolean equalsWithGeometry(final Object partner)
  {
    return equals(partner);
  }

  /**
   * Returns a hash code value for this element.
   * This is an implementation of the hashCode() function as documented in
   * the Java API. Care has been taken to satisfy the general hashCode()
   * contract, so the hash code does only depend on the immutable members
   * of an element. As a consequence, the method is not always as effective
   * as might be desired.
   */
  public int hashCode()
  {
    return getClass().hashCode();
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
  }

  public void pprintln(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer);
    printer.println(';');
  }

  /**
   * Produces a human-readable description of this element.
   * This method uses a {@link ModelPrinter} to obtain a verbose textual
   * description of an element's contents. The result may extend
   * over several lines.
   */
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ModelPrinter printer = new ModelPrinter(writer);
      pprint(printer);
      return writer.toString();
    } catch (final IOException exception) {
      throw new UnexpectedWatersException(exception);
    }
  }


  //#########################################################################
  //# Marshalling
  /**
   * Copies the contents of this element to a JAXB element.
   * This method is used internally for marshalling and should not
   * be called directly.
   * @param  element A JAXB element that is to be initialised with the
   *                 contents of this object.
   * @throws JAXBException to indicate that a fatal error occurred while
   *                 creating or copying nested elements.
   * @see    ProxyMarshaller
   */
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
  }

  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final ElementType element = factory.createElement(this);
    toJAXBElement(element);
    return element;
  }

}
