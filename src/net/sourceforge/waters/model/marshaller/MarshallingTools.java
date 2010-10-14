//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   MarshallingTools
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.xml.sax.SAXException;

/**
 * A collection of static methods to facilitate the writing of Waters
 * documents to files.
 *
 * @author Robi Malik
 */

public class MarshallingTools
{

  //#########################################################################
  //# Dummy Constructor to prevent instantiation of class
  private MarshallingTools()
  {
  }


  //#########################################################################
  //# Marshalling
  public static void saveProductDES(final ProductDESProxy des,
                                    final String filename)
  {
    try {
      final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
      final ProxyMarshaller<ProductDESProxy> marshaller =
        new JAXBProductDESMarshaller(factory);
      final File file = new File(filename);
      marshaller.marshal(des, file);
    } catch (final JAXBException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final SAXException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final WatersMarshalException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public static void saveModule(final ModuleProxy module,
                                final String filename)
  {
    try {
      final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ProxyMarshaller<ModuleProxy> marshaller =
        new JAXBModuleMarshaller(factory, optable);
      final File file = new File(filename);
      marshaller.marshal(module, file);
    } catch (final JAXBException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final SAXException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final WatersMarshalException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

}
