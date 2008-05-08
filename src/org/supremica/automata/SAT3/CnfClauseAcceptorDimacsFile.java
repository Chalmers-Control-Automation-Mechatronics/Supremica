/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.Collection;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 *
 * @author voronov
 */
public class CnfClauseAcceptorDimacsFile implements CnfClauseAcceptor{

    private File file;
    private PrintWriter stream;
    private CnfClauseAcceptorDimacsStream acceptorDimacsStream;
    
    private long variables = 0;
    private long clauses   = 0;
    
    /**
     * Creates acceptor on temporary file with random name
     * @throws java.io.IOException              if failed to create temp file
     * @throws java.io.FileNotFoundException    if failed to find created temp file
     */
    public CnfClauseAcceptorDimacsFile() 
            throws IOException, 
                   FileNotFoundException
    {
        file = File.createTempFile("sup-dimacs-", ".tmp");
        init();
    }
    
    /**
     * Creates acceptor
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public CnfClauseAcceptorDimacsFile(File file) throws FileNotFoundException{
        this.file = file;
        init();        
    }
    
    /**
     * Internal constructor
     */
    private void init() throws FileNotFoundException{
        stream = new PrintWriter(file);
        acceptorDimacsStream = new CnfClauseAcceptorDimacsStream(stream);
    }

    /**
     * Flush the stream, and write to the file the number of clauses and variables
     * 
     * @throws java.io.FileNotFoundException     if file for reopen not found
     * @throws java.io.IOException               if writing failed
     */
    public void flush() throws FileNotFoundException, IOException{
        stream.close();

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.skipBytes(10);  // "p cnf   "
        raf.writeBytes(""+variables+" "+clauses);
        raf.close();
    }

    public void accept(Collection<Integer> c) {
        if(c.size()>0)
            clauses++;
        for(int i: c)
            if(java.lang.Math.abs(i) > variables)
                variables = java.lang.Math.abs(i);
        
        acceptorDimacsStream.accept(c);
    }
}
