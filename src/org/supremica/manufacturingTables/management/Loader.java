/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

/**
 * The Loader class uses JAXB to load a Factory
 * application into a PLC program structure.
 *
 *
 * Created: Tue Nov  23 13:49:32 2005
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;

import java.io.*;
import javax.xml.bind.*;
import javax.xml.validation.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.XMLConstants;
import org.xml.sax.SAXException;

//import net.sourceforge.fuber.xsd.libraryelement.*;
//import net.sourceforge.fuber.xsd.libraryelement.impl.*;

/**
 * Class managing loading of Factory:s, ROP:s and EOP:s from manufacturingTable xml-files.
 */
public class Loader
{
    private JAXBContext jaxbContext;
    private Unmarshaller u;
        
    public Loader()
    {
    }
    
    public Object loadFactory(String path, String fileName)
    throws JAXBException, SAXException
    {
        // Create JAXBContext and unmarshaller for factory
        try
        {
            jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory");
            //System.err.println("jaxbcontext created");
            u = jaxbContext.createUnmarshaller();
            //System.err.println("unmarshaller created");
            
            // enable validation
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);
            StreamSource ss = new StreamSource(new File("../schema/manufacturingTables/xsd/Factory.xsd"));
            Schema schema = factory.newSchema(ss);
            u.setSchema(schema);
            System.err.println("validation is on");
            
            // We will allow the Unmarshaller's default
            // ValidationEventHandler to receive notification of warnings
            // and errors which will be sent to java.lang.System.err.  The default
            // ValidationEventHandler will cause the unmarshal operation
            // to fail with an UnmarshalException after encountering the
            // first error or fatal error.
            return load(path, fileName);
        }
        catch(SAXException se)
        {
            throw se;
        }
        catch(JAXBException je)
        {
            throw je;
        }
    }
    
    public Object loadEOP(String path, String fileName)
    throws JAXBException, SAXException
    {
        // Create JAXBContext and unmarshaller for EOP
        try
        {
            jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.eop");
            u = jaxbContext.createUnmarshaller();
            // enable validation
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);
            StreamSource ss = new StreamSource(new File("../schema/manufacturingTables/xsd/EOP.xsd"));
            Schema schema = factory.newSchema(ss);
            u.setSchema(schema);
            System.err.println("validation is on");
            
            // Se comment about validation above.
            return load(path, fileName);
        }
        catch(SAXException se)
        {
            throw se;
        }
        catch(JAXBException je)
        {
            throw je;
        }
    }
    
    public ROP loadROP(String path, String fileName)
    throws JAXBException, SAXException
    {
        try
        {
            // Create JAXBContext and unmarshaller for ROP
            jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.rop");
            u = jaxbContext.createUnmarshaller();
            // enable validation
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);
            StreamSource ss = new StreamSource(new File("../schema/manufacturingTables/xsd/ROP.xsd"));
            Schema schema = factory.newSchema(ss);
            u.setSchema(schema);
            System.err.println("validation is on");
            
            // Se comment about validation above.
            
            // Unmarshal!
            return (ROP) load(path, fileName);
        }
        catch(SAXException se)
        {
            throw se;
        }
        catch(JAXBException je)
        {
            throw je;
        }
    }
    
    private Object load(String path, String fileName)
    {                
        try
        {
            File theFile = getFile(path, fileName);
            
            if(theFile!=null)
            {
                // Unmarshall from the file
                Object o = u.unmarshal(theFile);
                java.lang.System.err.println("The file is unmarshalled");
                
                return o;
            }
            else
            {
                java.lang.System.err.println("Problems reading the file!");
            }
        }
        catch(UnmarshalException ue)
        {
            java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
            ue.printStackTrace();
        }
        catch(JAXBException je)
        {
            java.lang.System.err.println("JAXBException caught!");
            je.printStackTrace();
        }
        return null;
    }
    
    
    private File getFile(String path, String fileName)
    {
        File theFile = new File(fileName);
        if (path !=null)
        {
            File pathFile = new File(path);
            if (pathFile.isDirectory())
            {
                theFile = new File(path, fileName);
            }
            else
            {
                java.lang.System.err.println("The specified directory is invalid!");
                return null;
            }
        }
        if (theFile.isFile())
        {
            return theFile;
        }
        else
        {
            java.lang.System.err.println("The file does not exist in the specified directory!");
            return null;
        }
    }
}
