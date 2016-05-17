package net.sourceforge.waters.gui.flexfact;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Flexfact implements Runnable {
	static List<String> events = new ArrayList<String>();
	static String subscribeStr = "";
	public Flexfact() {};
	@Override
  public void run(){

		try (
			Scanner sc = new Scanner(System.in); // Read in commands from the console
			Socket flexFactSocket = new Socket(InetAddress.getLocalHost(), 40000); // This is for receiving commands
			PrintWriter flexFactOut = new PrintWriter(flexFactSocket.getOutputStream(), true); // For sending out Subscribe
		) {
			//START FLEXFACT SIMULATION FIRST

			//Start reading what Flexfact has to say locally and on the Flexfact socket.
			final Thread sendingThread = new Thread(new Read(flexFactSocket, false));
			sendingThread.start();

			System.out.println("This is a Flexfact exec");
			// Send the subscribe message only
			// when the line has been read
			flexFactOut.println("<Subscribe> </Subscribe>");
//			while(subscribeStr.equals(""))
//			{
//			}
//			System.out.println(subscribeStr);
//			flexFactOut.println(subscribeStr);

			// Is notify the only tag we need?
			//TODO: If multiple commands on a line, check each in list and send ones that match.
			while (sc.hasNext()){
				final String input = sc.nextLine();
				//System.out.println("you wrote: " + input);
				flexFactOut.println(input);

				if(input.toLowerCase().equals("exit"))
					break;
//				else if(events.contains(input)){
//					// Debugging
//					System.out.println("Command sent.");
//					out.println("<Notify> " + input + " </Notify>");
//
//				}
//				else
//					System.out.println("Invalid command.");

			}
			System.out.println("\nClosing...\n");
			//out.close();
			//clientSocket.close();
			//serverSocket.close();
		}
		catch(final Exception e) {
			e.printStackTrace();
		}
	}
}
