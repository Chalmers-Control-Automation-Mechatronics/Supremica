//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.xml.sax.SAXException;


/**
 * <P>A helper class to read and write Waters Counterexample (<CODE>.wtra</CODE>)
 * files.</P>
 *
 * @see DocumentManager
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
    super("waters-des.xsd",
          new SAXCounterExampleImporter(factory),
          new StAXProductDESWriter());
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
   *                  has failed for some reason.
   */
  public CounterExampleProxy unmarshal(final URI uri,
                                       final ProductDESProxy des)
    throws WatersUnmarshalException, IOException
  {
    final SAXCounterExampleImporter importer =
      (SAXCounterExampleImporter) getImporter();
    try {
      importer.setProductDES(des);
      return unmarshal(uri);
    } finally {
      importer.setProductDES(null);
    }
  }


  //#########################################################################
  //# Interfaces
  //# net.sourceforge.waters.model.marshaller.ProxyMarshaller<CounterExampleProxy>
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<CounterExampleProxy>
  @Override
  public String getDefaultExtension()
  {
    return ".wtra";
  }

  @Override
  public Class<CounterExampleProxy> getDocumentClass()
  {
    return CounterExampleProxy.class;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.SAXMarshaller<CounterExampleProxy>
  @Override
  public String getDescription()
  {
      return "Waters trace files [*.wtra]";
  }

}
