package edu.udo.cs.rvs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MyThread extends Thread {
	private boolean _terminate;
	private Socket socket;
	
	MyThread(Socket s) {
		_terminate = false;
		socket = s;
	}
	
	public void terminate() {
		_terminate = true;
	}
	
	@Override
	public void run() {
		while ( !_terminate )
		{
			BufferedReader in = null;
			BufferedOutputStream out = null;
			try {
				// eventuell output für Inhalt trennen?
				// BufferedReader und BufferedOutputStream auf Socket einrichten
				//zum Lesen des Requests und übergeben der Response
				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
				out = new BufferedOutputStream(socket.getOutputStream());
				
				
				//Fallunterscheidung GET,POST,HEAD,....
				
				
			} catch (FileNotFoundException fnfe) {
//				try {
//					//fileNotFound(out, dataOut, fileRequested);
//				} catch (IOException ioe) {
//					System.err.println("Error with file not found exception : " + ioe.getMessage());
//				}
			
			} catch (IOException ioe) {
				System.err.println("Server error : " + ioe);
			} finally {
				try {
					in.close();
					out.close();
					socket.close(); // we close socket connection
				} catch (Exception e) {
					System.err.println("Error closing stream : " + e.getMessage());
				} 
			}
//	     // Timeout für die Verbindung von 30 Sekunden
//	        // Verhindert zu viele offengehaltene Verbindungen, aber auch Übertragung von großen Dateien
//	        try {
//	            socket.setSoTimeout(30000);
//	        } catch (SocketException e) {
//	            Logger.exception(e.getMessage());
//	        }
	         
		}
	}
}