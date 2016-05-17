package net.sourceforge.waters.gui.flexfact;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Local implements Runnable {
	static List<String> events = new ArrayList<String>();
	public Local(){};
	@Override
  public void run(){

		try (
			Scanner sc = new Scanner(System.in); // Read in commands from the console
		    ServerSocket awaitingSocket = new ServerSocket(40001); 	// Server Socket to grab Flexfact's waiting connection
		    Socket localSocket = awaitingSocket.accept();	// This is for sending commands
			PrintWriter localOut = new PrintWriter(localSocket.getOutputStream(), true); // For sending Flexfact commands/notifications
		) {
			//START FLEXFACT SIMULATION FIRST

			//Start reading what Flexfact has to say locally and on the Flexfact socket.
			final Thread localThread = new Thread(new Read(localSocket, true));
			localThread.start();

			// Is notify the only tag we need?
			//TODO: If multiple commands on a line, check each in list and send ones that match.
			while (sc.hasNext()){
				final String input = sc.nextLine();
				if(input.toLowerCase().equals("exit"))
					break;
				else if(events.contains(input)){
					// Debugging
					System.out.println("Command sent.");
					localOut.println("<Notify> " + input + " </Notify>");

				}
				else
					System.out.println("Invalid command.");

			}
			System.out.println("\nClosing...\n");
			awaitingSocket.close();
			localSocket.close();
			localOut.close();
		}
		catch(final Exception e) {
			e.printStackTrace();
		}
	}
}

