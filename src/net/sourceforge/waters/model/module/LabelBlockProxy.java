//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   LabelBlockProxy
//###########################################################################
//# $Id: LabelBlockProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.LabelBlockType;
import net.sourceforge.waters.xsd.module.LabelGeometryType;


/**
 * A list of events with associated geometry information.  The typical
 * place where a label block is found is on the edges of a
 * graph. Technically, a label block can be treated as an implementation of
 * the {@link java.util.List} interface. The basic functionality is
 * implemented and documented in class {@link EventListProxy}, this class
 * just adds the geometry information.
 *
 * @author Robi Malik
 */

public class LabelBlockProxy extends EventListProxy {

  //#######################################################################
  //# Constructor
  /**
   * Creates an empty label block.
   */
  public LabelBlockProxy()
  {
  }

  /**
   * Creates a label block from a parsed XML structure.
   * @param  parent      The parsed XML structure representing the label
   *                     block to be created.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  LabelBlockProxy(final LabelBlockType block)
    throws ModelException
  {
    super(block);
    final LabelGeometryType geo = block.getLabelGeometry();
    if (geo != null) {
      mGeometry = new LabelGeometryProxy(geo);
    }
  }


  //#########################################################################
  //# Getters and Setters
  public LabelGeometryProxy getGeometry()
  {
    return mGeometry;
  }

  public void setGeometry(final LabelGeometryProxy geo)
  {
    mGeometry = geo;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equalsWithGeometry(final Object partner)
  {
    if (equals(partner)) {
      final LabelBlockProxy block = (LabelBlockProxy) partner;
      return GeometryProxy.equalGeometry(mGeometry, block.mGeometry);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Marshalling
  public LabelBlockType toLabelBlockType()
    throws JAXBException
  {
    final ElementFactory factory = new MemberElementFactory();
    final LabelBlockType block = (LabelBlockType) toJAXB(factory);
    if (mGeometry != null) {
      final LabelGeometryType geo = mGeometry.toLabelGeometryType();
      block.setLabelGeometry(geo);
    }
    return block;
  }


  //#########################################################################
  //# Data Members
  private LabelGeometryProxy mGeometry;


  //#########################################################################
  //# Local Class MemberElementFactory
  static class MemberElementFactory
    extends EventListProxy.MemberElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createLabelBlock();
    }

    public ElementFactory getNextFactory()
    {
      return new EventListProxy.MemberElementFactory();
    }

  }

}
