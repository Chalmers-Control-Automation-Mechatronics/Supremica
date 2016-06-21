package net.sourceforge.waters.gui.flexfact;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

import net.sourceforge.waters.gui.simulator.Simulation;

public class UDPListener implements Runnable
{
  Simulation sim;
  public UDPListener(final Simulation _sim){
    sim = _sim;
  }

  @Override
  public void run() {
    final byte[] buf = new byte[1024];
    DatagramPacket dpin;
    MulticastSocket ds;
    try{
        ds = new MulticastSocket(40000);

        dpin = new DatagramPacket(buf, buf.length);

        while (true) {
            ds.receive(dpin);

            // Convert the contents to a string, and display them
            final String msg = new String(buf, 0, dpin.getLength());
            if(msg.startsWith("<Stop>")){

              final Thread flexfact = new Thread(new Flexfact(sim));
              final Thread local = new Thread(new Local());
              flexfact.start();
              local.start();
            }

            // Reset the length of the packet before reusing it.
            dpin.setLength(buf.length);
          }
    }
    catch(final Exception e) {
        e.printStackTrace();
    }
  }

}
