
package org.supremica.automata.IO;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.ExtendedAutomaton;


/**
 * TCTConverter class to import TCT binary files.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class TCTConverter {
    private HashSet<Integer> markedStates;
    private HashSet<Integer> alphabet;
    private HashSet<Integer[]> transitions;
    private HashSet<Integer> states;
    private final URI uri;

    public TCTConverter(final URI uri){
        this.uri = uri;
    }

    private void readDES(final URL url) throws IOException{
        final InputStream stream = url.openStream();
        final DataInputStream reader = new DataInputStream(stream);
        byte[] b;

        b = new byte[5];
        reader.read(b);
        String str = new String(b);
        if(str.trim().equals("CTCT") || str.trim().equals("LTCT")){
            b = new byte[22];
            reader.read(b);
        } else if(str.equals("XPTCT")){
            b = new byte[23];
            reader.read(b);
        } else {
            throw new IOException("The file is not compatible with TCT!\n");
        }

        b = new byte[7];
        reader.read(b);
        str = new String(b);
        if(!str.trim().equals("Z8^0L;1"))
            throw new IOException("Wrong signature!\n");

        final int endian = readLEInt(reader);
        if(endian != 0xFF00AA55){
            throw new IOException("Wrong endian!\n");
        }

        final int[] block = new int[2];
	block[0] = readLEInt(reader); //Read block type and size.
        block[1] = readLEInt(reader);
	if(block[0]== 1)
            throw new IOException("Wrong block type!\n");

        final int nbrStates = readLEInt(reader);

	if(readLEInt(reader) != 0)
            throw new IOException("Initial state not 0!\n");

        markedStates = new HashSet<Integer>(nbrStates);
	while(true)
	{
            final long markedState = readLEInt(reader);
            if(markedState == -1L)
                break;
            markedStates.add((int)markedState);
	}

	//Read in the transitions
	//Note: sizeof(long)=sizeof(int)=4,sizeof(short)=2,sizeof(char)=1.

        alphabet = new HashSet<Integer>();
        transitions = new HashSet<Integer[]>();
        states = new HashSet<Integer>(nbrStates);
        Integer[] transition;
        int transNo = 0;

	while(true)
	{
            final long exit = readLEInt(reader); //The exit state of one transition
            if(exit == -1L)
                break;
            states.add((int)exit);
            final short nt = readLEShort(reader);
            transNo += nt;

            for(short j=0; j<nt; j++)
            {
                final int temp = readLEInt(reader);
                final int entrance = temp & 0x003fffff;
                final long event = (temp & 0xffffffffL) >> 22;
                states.add(entrance);
                alphabet.add((int)event); //Insert a new event.

                //Create a new transition and add it to the exit state.
                transition = new Integer[3];
                transition[0] = (int)exit;
                transition[1] = (int)event;
                transition[2] = entrance;
                transitions.add(transition);
            }
	}

        if(transNo != transitions.size())
            throw new IOException("Wrong number of transitions!/n");

    }

    public ExtendedAutomaton getExtendedAutomaton() throws IOException{
        readDES(uri.toURL());
        final String file = uri.toURL().getFile();
        final String name = file.substring(file.lastIndexOf('/')+1, file.lastIndexOf('.'));
        final ExtendedAutomaton des = new ExtendedAutomaton(name, ComponentKind.PLANT);

        for(final Integer st : states){
            final String stName = Integer.toString(st);
            final boolean isInitial = (st == 0)?true:false;
            final boolean isMarked = markedStates.contains(st);
            des.addState(stName, isMarked, isInitial, false);
        }

        EventKind kind;
        boolean isObservable;
        for(final Integer e : alphabet){
            isObservable = (e == 1000)?false:true;
            if(!isObservable)
                kind = EventKind.CONTROLLABLE;
            else
                kind = (e%2 != 0)?EventKind.CONTROLLABLE:EventKind.UNCONTROLLABLE;

            des.addEvent(Integer.toString(e), kind.value(), isObservable);
        }

        for(final Integer[] tran : transitions){
            des.addTransition(Integer.toString(tran[0]), Integer.toString(tran[2]), Integer.toString(tran[1]), null, null);
        }
        return des;
    }

    private int readLEInt(final DataInputStream reader) throws IOException{
        final byte[] b = new byte[4];
        reader.read(b);
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private short readLEShort(final DataInputStream reader) throws IOException{
        final byte[] b = new byte[2];
        reader.read(b);
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

}
