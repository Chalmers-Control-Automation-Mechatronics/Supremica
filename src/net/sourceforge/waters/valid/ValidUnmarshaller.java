//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ValidUnmarshaller
//###########################################################################
//# $Id: ValidUnmarshaller.java,v 1.7 2006-09-14 21:10:21 flordal Exp $
//###########################################################################

package net.sourceforge.waters.valid;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.filechooser.FileFilter;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBDocumentImporter;
import net.sourceforge.waters.model.marshaller.JAXBMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBModuleImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import net.sourceforge.waters.xsd.module.Module;
import org.supremica.gui.StandardExtensionFileFilter;

import org.xml.sax.SAXException;


public class ValidUnmarshaller
  implements ProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructor
  public ValidUnmarshaller(final ModuleProxyFactory factory,
                           final OperatorTable optable)
    throws JAXBException, SAXException
  {
    final SchemaFactory schemafactory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final URL url = JAXBMarshaller.class.getResource("waters-module.xsd");
    final Schema schema = schemafactory.newSchema(url);
    final JAXBContext context =
      JAXBContext.newInstance("net.sourceforge.waters.xsd.module");
    mUnmarshaller = context.createUnmarshaller();
    mUnmarshaller.setSchema(schema);
    mImporter = new JAXBModuleImporter(factory, optable);
  }


  //#########################################################################
  //# Access Methods
  public ModuleProxy unmarshal(URI uri)
    throws WatersUnmarshalException, IOException
  {
    try {
      final String name = uri.toString();
      if (name.endsWith(".vprj")) {
        final int len = name.length();
        final String newname = name.substring(0, len - 5) + "_main.vmod";
        uri = new URI(newname);
      }
      final ValidTransformer transformer = new ValidTransformer(uri);
      final Source source = transformer.getSource();
      transformer.start();
      final Module module = (Module) mUnmarshaller.unmarshal(source);
      final ModuleProxy modproxy = mImporter.importDocument(module, null);
      return modproxy;
    } catch (final JAXBException exception) {
      throw new WatersUnmarshalException(uri, exception);
    } catch (final TransformerConfigurationException exception) {
      throw new WatersUnmarshalException(uri, exception);
    } catch (final URISyntaxException exception) {
      throw new WatersUnmarshalException(uri, exception);
    }
  }

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public String getDefaultExtension()
  {
    return EXT_VMOD;
  }

  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  public Collection<FileFilter> getSupportedFileFilters()
  {
      return FILTERS;
  }
  
  public DocumentManager getDocumentManager()
  {
    return null;
  }

  public void setDocumentManager(DocumentManager manager)
  {
  }


  //#########################################################################
  //# Data Members
  private final Unmarshaller mUnmarshaller;
  private final JAXBDocumentImporter<ModuleProxy,Module> mImporter;

  private static final Collection<String> EXTENSIONS;
  private static final Collection<FileFilter> FILTERS;
  private static final String EXT_VMOD = ".vmod";
  private static final String EXT_VPRJ = ".vprj";

  static {
    final Collection<String> exts = new LinkedList<String>();
    exts.add(EXT_VMOD);
    exts.add(EXT_VPRJ);
    EXTENSIONS = Collections.unmodifiableCollection(exts);
    
    final Collection<FileFilter> filters = new LinkedList<FileFilter>();
    filters.add(new StandardExtensionFileFilter(EXT_VMOD, "VALID Module files [*.vmod]"));
    filters.add(new StandardExtensionFileFilter(EXT_VPRJ, "VALID Project files [*.vprj]"));
    FILTERS = Collections.unmodifiableCollection(filters);
  }

}
