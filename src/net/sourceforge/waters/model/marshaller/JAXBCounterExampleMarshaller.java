//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.des.CounterExampleType;

import org.xml.sax.SAXException;


public class JAXBCounterExampleMarshaller
  extends JAXBMarshaller<CounterExampleProxy,CounterExampleType>
{

  //#########################################################################
  //# Constructors
  public JAXBCounterExampleMarshaller(final ProductDESProxyFactory factory)
    throws JAXBException, SAXException
  {
    super(new JAXBCounterExampleExporter(),
          new JAXBCounterExampleImporter(factory),
          "net.sourceforge.waters.xsd.des",
          "waters-des.xsd");
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the product DES corresponding to a counterexample to be unmarshalled.
   * If non-<CODE>null</CODE> the name of the product DES in the
   * <CODE>.wtra</CODE> must match the name of the given product DES,
   * so the counterexample automata can be taken from the given product DES.
   * If <CODE>null</CODE>, the product DES will be obtained using the
   * document manager, and an appropriate <CODE>.wdes</CODE> file must
   * exist.
   */
  public void setProductDES(final ProductDESProxy des)
  {
    final JAXBCounterExampleImporter importer =
      (JAXBCounterExampleImporter) getImporter();
    importer.setProductDES(des);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBMarshaller
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

  @Override
  public String getDescription()
  {
      return "Waters Trace files [*.wtra]";
  }

  @Override
  public Class<CounterExampleType> getElementClass()
  {
    return CounterExampleType.class;
  }

}
