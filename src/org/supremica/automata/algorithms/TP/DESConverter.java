/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 *
 * @author shoaei
 */
public class DESConverter {
    
    public DESConverter() throws MalformedURLException, URISyntaxException, IOException{
        File file = new File("C:/Xptct138/USER/TEST2.DES");
        URL url = file.toURI().toURL();
        final InputStream stream = url.openStream();
        DataInputStream reader = new DataInputStream(stream);
        
        int ch;
        while((ch = reader.read()) != -1){
            if((char)ch==(char)0x1a)
                break;
        }
        
        byte[] b = new byte[8];
        reader.read(b);
        b[7]='\0';
        String signature = new String(b);
        if(!signature.trim().equals("Z8^0L;1"))
            System.err.print("File signature is invalid.\n");
        
        int endian = reader.readInt();
        if(endian != (int)0xaa00ff00)
            System.err.println("File endian is incorrect.\n");
        
        int block_type, data_size;
        do{
            block_type = reader.readInt();
            data_size = reader.readInt();
            System.err.println(block_type + " --- " + data_size);
            if(block_type == 0)
                break;
            else
                reader.mark(data_size);
        }while(block_type != -1);
        if(block_type == -1){
            System.err.println("File block-type is incorrect.\n");
        }
        System.err.println("HERE");  
        while((ch = reader.readInt()) != -1){
            System.err.println(ch);
        }
    }
    
}
