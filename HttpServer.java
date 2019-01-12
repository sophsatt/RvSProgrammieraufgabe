package edu.udo.cs.rvs;

import java.io.*;
import java.lang.*;
import java.lang.annotation.*;
import java.lang.invoke.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.security.acl.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.jar.*;
import java.util.regex.*;
import java.util.spi.*;
import java.util.stream.*;
import java.util.zip.*;

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

/**
 * @author 2908s
 *
 */
public class HttpServer
{
    /**
     * Beispiel Dokumentation fuer dieses Attribut:
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
    private String ipAdresse = "127.0.0.1";

	private Socket mySocket;

    /**
     * Beispiel Dokumentation fuer diesen Konstruktor:
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
    	boolean sent = false;
    	System.out.println("in Methode startServer()");
    	ServerSocket ssocket = null;
		try {
			ssocket = new ServerSocket(port);
//			try {
//				ssocket.bind( new InetSocketAddress( ipAdresse, port ) );
//				System.out.println("ssocket gebunden");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				//e.printStackTrace();
//				System.out.println("error in ssocket.bin()");
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final ServerSocket finalSocket = ssocket;
		while(!sent){
			try {
				System.out.println("while-Schleife Thread");
				MyThread thread = new MyThread(finalSocket.accept(), wwwroot, ipAdresse);
				System.out.print("Thread Konstruktor aufgerufen, Socket acceptet");
				thread.start();
				System.out.println("Thread gestartet");
				//später rausnehmen
				sent = true;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    }
    
    /**
     * Methode zur Bestimmung der aktuellen IP-Adresse
     */
    public void getServer() 
    {
    	//TODO implementieren
    	
    	this.ipAdresse = "http//" + "";
    }
}
