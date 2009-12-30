//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   DocumentIntegrityChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import junit.framework.Assert;

import net.sourceforge.waters.model.base.DocumentProxy;


public abstract class DocumentIntegrityChecker<D extends DocumentProxy>
{

  //#########################################################################
  //# Constructor
  protected DocumentIntegrityChecker()
  {
  }


  //#########################################################################
  //# Invocation
  public void check(final D doc)
    throws Exception
  {
    checkDocumentIntegrity(doc);
  }


  //#########################################################################
  //# Integrity Checking
  private void checkDocumentIntegrity(final DocumentProxy doc)
    throws MalformedURLException
  {
    final URI location = doc.getLocation();
    try {
      final File file = doc.getFileLocation();
      if (location == null) {
        Assert.assertNull("Null location, non-null file???", file);
      } else {
        Assert.assertEquals("Location and file location do not match!",
                            location, file.toURI());
      }
    } catch (final MalformedURLException exception) {
      final URL url = location.toURL();
      Assert.assertFalse("MalformedURLException thrown despite FILE URI!",
                         url.getProtocol().equals("file"));
    }
  }

}
