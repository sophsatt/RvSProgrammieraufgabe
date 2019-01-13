package edu.udo.cs.rvs;

import java.io.*;
import java.net.*;


/**
 * Nutzen Sie diese Klasse um den HTTP Server zu implementieren. Sie duerfen
 * weitere Klassen erstellen, sowie Klassen aus den in der Aufgabe aufgelisteten
 * Paketen benutzen. Achten Sie darauf, Ihren Code zu dokumentieren und moegliche
 * Ausnahmen (Exceptions) sinnvoll zu behandeln.
 *
 * @author Lukas Bünger, 196891
 * @author Vu Duc Tran, 207061
 * @author Sophie Sattler, 180431
 */

public class HttpServer
{
    /**
     * Dieses Attribut gibt den Basis-Ordner fuer den HTTP-Server an.
     */
    private static final File wwwroot = new File("wwwroot");

    /**
     * Der Port, auf dem der HTTP-Server lauschen soll.
     */
    private int port;


    /**
     * die IP-Adresse vom aufrufenden System
     */
    private String ipAdresse;

	private ServerSocket ssocket;

    /**
     * Der Server wird initialisiert und der gewuenschte Port
     * gespeichert.
     * 
     * @param port
     *            der Port auf dem der HTTP-Server lauschen soll
     */
    public HttpServer(int port)
    {
        this.port = port;
    }

    /**
     * Beispiel Dokumentation fuer diese Methode:
     * Diese Methode oeffnet einen Port, auf dem der HTTP-Server lauscht.
     * Eingehende Verbindungen werden in einem eigenen Thread behandelt.
     */
    public void startServer()
    {
    	try {
			System.out.println("HttpServer erstellt");
			
			//Die IP-Adresse wird von System gegeben.
	    	if(ipAdresse != null && !ipAdresse.isEmpty()) {
	    		this.ssocket = new ServerSocket(port, 1, InetAddress.getByName(ipAdresse));
	    	}
	    	
	    	else {
	    		this.ssocket = new ServerSocket(port, 1, InetAddress.getLocalHost());
	    	}
	    	System.out.println("Server gestartet mit IP Adresse " + ssocket.getInetAddress().getHostAddress() + " und port " + port);
	    	
		} catch (IOException e) {
			System.out.println("Fehler beim Erstellen des ServerSocket." + e);
		}

    	
		final ServerSocket finalSocket = ssocket;
		while(true){
			try {
				MyThread thread = new MyThread(finalSocket.accept(), wwwroot, ipAdresse);

				thread.start();
				
			} catch (IOException e) {
				System.out.println("Fehler beim Starten des Threads, akzeptieren des ServerSockets. " + e);
			}

		}
		
    }
}