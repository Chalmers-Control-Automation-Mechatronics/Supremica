//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.external.valid;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXDocumentImporter;
import net.sourceforge.waters.model.marshaller.SAXModuleImporter;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ValidUnmarshaller
  implements ProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructor
  public ValidUnmarshaller(final ModuleProxyFactory factory,
                           final OperatorTable optable)
    throws SAXException, ParserConfigurationException
  {
    mImporter = new SAXModuleImporter(factory, optable);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<ModuleProxy>
  @Override
  public ModuleProxy unmarshal(URI uri)
    throws WatersUnmarshalException, IOException
  {
    try {
      final String name = uri.toString();
      if (VPRJFILTER.accept(name)) {
        final int len = name.length();
        final String newname = name.substring(0, len - 5) + EXT_MAINVMOD;
        uri = new URI(newname);
      }
      final ValidTransformer transformer = new ValidTransformer(uri);
      final InputSource source = transformer.getSource();
      transformer.start();
      mImporter.setURI(uri);
      return mImporter.parse(source);
    } catch (final SAXException | TransformerConfigurationException |
             URISyntaxException | IOException exception) {
      throw new WatersUnmarshalException(uri, exception);
    } finally {
      mImporter.setURI(null);
    }
  }

  @Override
  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  @Override
  public String getDefaultExtension()
  {
    return EXT_VMOD;
  }

  @Override
  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
    return FILTERS;
  }


  //#########################################################################
  //# Inner Class MainVmodFileFiler
  private static class MainVmodFileFiler
    extends FileFilter
  {
    //#######################################################################
    //# Overrides for Abstract Baseclass javax.swing.filechooser.FileFilter
    @Override
    public boolean accept(final File file)
    {
      if (file.isDirectory()) {
        return true;
      } else {
        final String filename = file.getName();
        final int lastunderscore = filename.lastIndexOf('_');
        if (lastunderscore > 0 && lastunderscore < filename.length() - 1) {
          final String ext = filename.substring(lastunderscore);
          return ext.equalsIgnoreCase(EXT_MAINVMOD);
        } else {
          return false;
        }
      }
    }

    @Override
    public String getDescription()
    {
      return DESCR_VMOD;
    }
  }


  //#########################################################################
  //# Data Members
  private final SAXDocumentImporter<ModuleProxy> mImporter;

  private static final Collection<String> EXTENSIONS;
  private static final Collection<FileFilter> FILTERS;
  private static final String EXT_VMOD = ".vmod";
  private static final String EXT_MAINVMOD = "_main.vmod";
  private static final String EXT_VPRJ = ".vprj";
  private static final String DESCR_VMOD = "VALID Main Module files [*.vmod]";
  private static final String DESCR_VPRJ = "VALID Project files [*.vprj]";
  private static final FileFilter VMODFILTER = new MainVmodFileFiler();
  private static final StandardExtensionFileFilter VPRJFILTER =
    new StandardExtensionFileFilter(DESCR_VPRJ, EXT_VPRJ);

  static {
    final Collection<String> exts = new LinkedList<String>();
    exts.add(EXT_VMOD);
    exts.add(EXT_VPRJ);
    EXTENSIONS = Collections.unmodifiableCollection(exts);
    final Collection<FileFilter> filters = new LinkedList<FileFilter>();
    filters.add(VMODFILTER);
    filters.add(VPRJFILTER);
    FILTERS = Collections.unmodifiableCollection(filters);
  }

}
