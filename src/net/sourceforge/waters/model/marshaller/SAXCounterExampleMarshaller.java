//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;

import org.xml.sax.SAXException;


/**
 * <P>A helper class to read and write Waters Counterexample
 * (<CODE>.wtra</CODE>) files.</P>
 *
 * <P>Unlike other document types, counterexamples are only useful in the
 * context of a {@link ProductDESProxy}. When reading a counterexample file,
 * it is important to ensure that the {@link CounterExampleProxy} obtained
 * used references the same {@link AutomatonProxy}, {@link EventProxy}, and
 * {@link StateProxy} objects as the corresponding {@link ProductDESProxy}.</P>
 *
 * <P>As a result, unmarshalling of counterexamples works differently from
 * the other types. Although nominally implementing the {@link
 * ProxyUnmarshaller}&lt;{@link CounterExampleProxy}&gt; interface, this class
 * cannot be registered as an unmarshaller of a {@link DocumentManager}. The
 * best way to load a counterexample is to instantiate a
 * SAXCounterExampleMarshaller with its {@link
 * #SAXCounterExampleMarshaller(ProductDESProxyFactory) constructor} and call
 * the method {@link #unmarshal(URI, ProductDESProxy)} with the correct
 * {@link ProductDESProxy} as argument. The unmarshalling process maps the
 * names on the XML file to the appropriate objects in the {@link
 * ProductDESProxy} and throws {@link WatersUnmarshalException} if the names
 * do not match.</P>
 *
 * <P>Marshalling of counterexamples to files works normally, so registration
 * as a marshaller of a {@link DocumentManager} is possible.</P>
 *
 * @author Robi Malik
 */

public class SAXCounterExampleMarshaller
  extends SAXMarshaller<CounterExampleProxy>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new counterexample marshaller.
   * @param  factory   The factory to be used to create the objects when
   *                   loading a module from an XML file.
   */
  public SAXCounterExampleMarshaller(final ProductDESProxyFactory factory)
    throws SAXException, ParserConfigurationException
  {
    super(new SAXCounterExampleImporter(factory),
          new StAXProductDESWriter());
    mSaturator = new CounterExampleSaturator(factory);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether counterexample saturation is performed.
   * @param  sat  If <CODE>false</CODE> (the default), counterexamples
   *              are unchanged when reading and writing to and from files.
   *              If <CODE>true</CODE>, counterexamples are written to files
   *              in desaturated form, including only the state information
   *              needed to resolve nondeterminism, and they are saturated to
   *              include full state information after reading from a file.
   * @see CounterExampleSaturator
   */
  public void setSaturating(final boolean sat)
  {
    mSaturating = sat;
  }

  /**
   * Returns whether counterexample saturation is performed.
   * @see #setSaturating(boolean)
   */
  public boolean isSaturating()
  {
    return mSaturating;
  }


  //#########################################################################
  //# Specific Methods
  /**
   * Loads a counterexample from a file.
   * @param  uri      A URI specifying the location of the counterexample
   *                  to be retrieved.
   * @param  des      The product DES containing the automata and events
   *                  referenced in the file.
   * @return The loaded counterexample.
   * @throws WatersUnmarshalException to indicate that reading the XML file
   *                  has failed for some reason. This exception is also
   *                  thrown if the counterexample file mentions the names
   *                  of automata, events, or states that do not exist in
   *                  the given product DES, or if the counterexample fails
   *                  to be accepted by all automata in the given product DES.
   */
  public CounterExampleProxy unmarshal(final URI uri,
                                       final ProductDESProxy des)
    throws WatersUnmarshalException, IOException
  {
    final SAXCounterExampleImporter importer =
      (SAXCounterExampleImporter) getImporter();
    try {
      importer.setProductDES(des);
      CounterExampleProxy cex = unmarshal(uri);
      if (mSaturating) {
        cex = mSaturator.saturate(cex);
        cex.setLocation(uri);
      } else {
        mSaturator.check(cex);
      }
      return cex;
    } catch (final CounterExampleValidationException exception) {
      throw new WatersUnmarshalException(exception);
    } finally {
      importer.setProductDES(null);
    }
  }


  //#########################################################################
  //# Interfaces
  //# net.sourceforge.waters.model.marshaller.ProxyMarshaller<CounterExampleProxy>
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<CounterExampleProxy>
  @Override
  public void marshal(CounterExampleProxy cex, final File filename)
    throws WatersMarshalException, IOException
  {
    if (mSaturating) {
      try {
        cex = mSaturator.desaturate(cex);
      } catch (final CounterExampleValidationException exception) {
        throw new WatersMarshalException(exception);
      }
    }
    super.marshal(cex, filename);
  }


  @Override
  public Class<CounterExampleProxy> getDocumentClass()
  {
    return CounterExampleProxy.class;
  }

  @Override
  public StandardExtensionFileFilter getDefaultFileFilter()
  {
    return WTRA_FILE_FILTER;
  }


  //#########################################################################
  //# Data Members
  private final CounterExampleSaturator mSaturator;

  private boolean mSaturating = false;


  //#########################################################################
  //# Class Constants
  public static StandardExtensionFileFilter WTRA_FILE_FILTER =
    new StandardExtensionFileFilter("Waters counterexample files (*.wtra)", ".wtra");

}
