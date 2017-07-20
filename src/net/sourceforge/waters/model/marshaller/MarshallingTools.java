//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
import java.util.Collection;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
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
  /**
   * Saves a product DES as a product DES (<CODE>.wdes</CODE>) file.
   * @param  des       The product DES to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wdes</CODE> extension.
   */
  public static void saveProductDES(final ProductDESProxy des,
                                    final String filename)
  {
    try {
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
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

  /**
   * Saves an automaton into a product DES (<CODE>.wdes</CODE>) file.
   * This methods creates a product DES containing the events of the
   * given automaton and the automaton, and saves it into a file.
   * @param  aut       The automaton to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wdes</CODE> extension.
   */
  public static void saveProductDES(final AutomatonProxy aut,
                                    final String filename)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, factory);
    saveProductDES(des, filename);
  }

  /**
   * Saves a collection of automata into a product DES (<CODE>.wdes</CODE>)
   * file. This methods creates a product DES containing the events of the
   * given automata and the automata, and saves it into a file.
   * @param  automata  The automata to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wdes</CODE> extension.
   */
  public static void saveProductDES(final Collection<? extends AutomatonProxy> automata,
                                    final String filename)
  {
    final int dotpos = filename.lastIndexOf(".");
    final String name = dotpos >= 0 ? filename.substring(0, dotpos) : filename;
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(name, automata, factory);
    saveProductDES(des, filename);
  }

  /**
   * Saves a module as a Waters module (<CODE>.wmod</CODE>) file.
   * @param  module    The module to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wmod</CODE> extension.
   */
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

  /**
   * Saves a product DES as a Waters module (<CODE>.wmod</CODE>) file.
   * This method converts the given product into a module and saves the
   * result to a file.
   * @param  des       The product DES to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wmod</CODE> extension.
   */
  public static void saveModule(final ProductDESProxy des,
                                final String filename)
  {
    try {
      final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
      final ProductDESImporter importer = new ProductDESImporter(factory);
      final ModuleProxy module = importer.importModule(des);
      saveModule(module, filename);
    } catch (final ParseException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  /**
   * Saves an automaton into a Waters module (<CODE>.wmod</CODE>) file.
   * This methods creates a module containing the events of the
   * given automaton and the automaton, and saves it into a file.
   * @param  aut       The automaton to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wmod</CODE> extension.
   */
  public static void saveModule(final AutomatonProxy aut,
                                final String filename)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, factory);
    saveModule(des, filename);
  }

  /**
   * Saves a collection of automata into a Waters module (<CODE>.wmod</CODE>)
   * file. This methods creates a product DES containing the events of the
   * given automata and the automata, and saves it into a file.
   * @param  automata  The automata to be saved.
   * @param  filename  The name of the output file,
   *                   should have <CODE>.wmod</CODE> extension.
   */
  public static void saveModule(final Collection<? extends AutomatonProxy> automata,
                                final String filename)
  {
    final int dotpos = filename.lastIndexOf(".");
    final String name = dotpos >= 0 ? filename.substring(0, dotpos) : filename;
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(name, automata, factory);
    saveModule(des, filename);
  }

  /**
   * Saves a product DES as a product DES (<CODE>.wdes</CODE>) or
   * module (<CODE>.wmod</CODE>) file, depending on the extension found
   * in the file name.
   * @param  des       The product DES to be saved.
   * @param  filename  The name of the output file. If no extension is
   *                   present, <CODE>.wdes</CODE> is added.
   */
  public static void saveProductDESorModule(final ProductDESProxy des,
                                            String filename)
  {
    try {
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      ProxyMarshaller<ProductDESProxy> desMarshaller;
      desMarshaller = new JAXBProductDESMarshaller(desFactory);
      final String desExt = desMarshaller.getDefaultExtension();
      if (filename.endsWith(desExt)) {
        saveProductDES(des, filename);
        return;
      }
      final ModuleProxyFactory modFactory = ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ProxyMarshaller<ModuleProxy> modMarshaller =
        new JAXBModuleMarshaller(modFactory, optable);
      final String modExt = modMarshaller.getDefaultExtension();
      if (filename.endsWith(modExt)) {
        saveModule(des, filename);
        return;
      }
      filename += desExt;
      saveProductDES(des, filename);
    } catch (final JAXBException | SAXException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

}
