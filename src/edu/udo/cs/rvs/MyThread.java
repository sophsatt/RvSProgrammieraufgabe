package edu.udo.cs.rvs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * In dieser Klasse werden die Anfragen vom Client verarbeitet und die Antworten vom Server erstellt und gesendet.
 * Die angefragten Dateien werden eingelesen und zurückgegeben.
 *
 */
public class MyThread extends Thread {
	
	/**
	 * Variable, die zeigt ob Thread weiterlaufen soll
	 */
	private boolean terminate;
	
	/**
	 * Socket über den Requests und Responses empfangen und geschickt werden
	 */
	private Socket socket;
	
	/**
	 * Dieses Attribut gibt den Basis-Ordner fuer den HTTP-Server an.
	 */
	private File wwwroot = null;
	
	/**
	 * IP-Adresse über die der Server erreicht werden kann
	 */
	private String ipAdresse;

	/**
	 * Thread wird initialisiert und Socket, Basis-Ordner und IP-Adresse werden gesetzt.
	 * @param s der Socket zum Austausch
	 * @param root der Basis-Ordner
	 * @param ipAd die IP-Adresse
	 */
	
	MyThread(Socket s, File root, String ipAd) {
		this.terminate = false;
		this.socket = s;
		this.wwwroot = root;
		this.ipAdresse = ipAd;	
	}
	
	
	/**
	 * Methode zum Terminieren des Threads, schließt den Socket.
	 */
	public void terminate() {
		terminate = true;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Fehler beim Schließen des Sockets.");
		}
	}
	
	/**
	 * run-Methode, die die Requests des Clients verarbeitet und entscheidet wie geantwortet werden soll
	 */
	@Override
	public void run() {
		
		BufferedReader in = null;
		BufferedOutputStream out = null;
		
		// BufferedOut/InpustStream zum Socket wird versucht aufzubauen
		try {
			out = new BufferedOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
		
		} catch (UnsupportedEncodingException e1) {
			System.out.println("Fehler beim Aufbau des In/Outputstreams durch EncodingException." );
			e1.printStackTrace();
		
		} catch (IOException e1) {
			System.out.println("Fehler beim Aufbau des In/Outputstreams");
			e1.printStackTrace();
		}
		
		//läuft solange Thread lebt
		while ( !terminate )
		{

			try {
				
				String frstLine = in.readLine();
				
				String method = "";
				String requestedFile = "";
				String version = "";
				boolean conGet = false;
				
				/**
				 * Der Browser schickt nach der ersten einfachen GET-Anfrage noch recht viele weitere Anfragen
				 * bzw. einfach Anweisungen für den Client (Bspw. welche Sprachen er unterstützt hab ich gesehen)
				 * Die sind teilweise auch nur 2 Wörter lang sodass der Tokenizer eine Exception wirft, diese Art
				 * Anfragen können wir aber einfach ignorieren.
				 */
				
				
				try {
					StringTokenizer frstParse = new StringTokenizer(frstLine);

					method = frstParse.nextToken();
					requestedFile = frstParse.nextToken();
					version = frstParse.nextToken();
					
				} catch(NullPointerException e){
					//Browser schickt leere Anfragen
					System.out.println("NullpointerException durch StringTokenizer");
					continue;
				}
				
				
				/**
				 * Eine Möglichkeit die überflüssigen Anfragen zu ignorieren, so wird immer "Not Implemented"
				 * zurückgegeben, was ja an sich auch stimmt, und der Browser arbeitet trotzdem einfach weiter 
				 */
				catch(NoSuchElementException e)
				{
					System.out.println("NoSuchElementException");
					version = "HTTP/1.0";			//Auch nicht wo wirklich richtig eigentlich
					method = "Not Implemented";		//Eigentlich egal was man nimmt, Hauptsache es wird als falsch erkannt
					requestedFile = null;
					continue;
				}
				
				String sndLine;
				
				String modDate = "";
				
				//Anfrage ist conditional GET
				if (method.equals("GET") && (sndLine = in.readLine()) != null && sndLine.length() != 0 && sndLine.contains("If-Modified-Since")) {
					StringTokenizer sndParse = new StringTokenizer(sndLine);
					sndParse.nextToken();
					modDate = sndParse.nextToken();	
					conGet = true;
				}
				
				//Zum Testen
				//System.out.println("REQUEST BROWSER " + method + " "+ requestedFile + " "+ version);
				
				int statusCode;
				
				File reqFile = null;
				
				String url = ipAdresse + requestedFile;
				
				boolean head = false;
								
				
				// unterstützt nur 1.0 und 1.1 Anfragen
				if(version.equals("HTTP/2.0") && !version.isEmpty()){
					statusCode = 400;
					sendResponse(out, reqFile, version, statusCode, url, head);
				}
				
				//nur GET, HEAD und POST werden unterstützt
				else if(!method.equals("GET") && !method.equals("POST") && !method.equals("HEAD") && !method.isEmpty()){
					statusCode = 501;
					sendResponse(out, reqFile, version, statusCode, url, head);
				} 
				
				//HEAD Anfrage, File wird nicht benötigt
				else if(!method.equals("GET") && !method.equals("POST") && method.equals("HEAD") && !method.isEmpty()){
					statusCode = 200;
					sendResponse(out, reqFile, version, statusCode, url, head);
				}
					
				//GET oder POST Methode, kein conditional Get
				else if (method.equals("GET") || method.equals("POST") && !method.equals("HEAD") && !method.isEmpty()) {
					
					//wenn Conditional GET muss überprüft werden, ob was seit dem modDate modifiziert wurde
					//hier noch nicht richtig implementiert, aber zur Vollständigkeit
					if (conGet) {
						if(modDate.equals(new Date().toString())) {
							statusCode = 304;
							sendResponse(out, null, version, statusCode, url, head);
						}
					}
					
					//kein Zugriff erlaubt auf Passwörter
					else if (requestedFile.endsWith("passwoerter.txt") && requestedFile != null) {
						statusCode = 403;
						sendResponse(out, null, version, statusCode, url, head);
					}
					
					//überprüfen ob, wenn nur Verzeichnis angegeben wird, eine Index-Datei vorhanden ist
					else if (requestedFile.endsWith("/") && requestedFile != null) {
						//index.??? hier festgelegt auf .html
						requestedFile += "index.html";
						
						reqFile = new File(wwwroot, requestedFile);
						
						//wenn keine Index-Datei im angegebenen Verzeichnis vorhanden ist
						if (!reqFile.exists()) {
							statusCode = 204;
							reqFile = null;
							
						} else {
							statusCode = 200;
						}
						sendResponse(out, reqFile, version, statusCode, url, head);
						
					}
					
					//Normalfall, zunächst überprüfen ob Datei existiert
					else {
						
						reqFile = new File(wwwroot, requestedFile);

						//wenn Datei nicht existiert
						if (!reqFile.exists()) {
							statusCode = 404;
							reqFile = null;
						} else {
							statusCode = 200;
						}
						sendResponse(out, reqFile, version, statusCode, url, head);
					}
				
				}
			
			} catch (FileNotFoundException e) {	
			
			} catch (IOException e) {
				System.out.println("Fehler beim In/Output: " + e);
				
				/**
				 * Es ist (denke ich jedenfalls und mit der Überlegung funktioniert es ja ziemlich gut) ein Thread
				 * nur für eine Übertragung zuständig, da (keine Ahnung wieso) bei Anfragen vom Browser der
				 * InputStream nicht blockiert und nurnoch Exceptions wirft die die Konsole fluten und auch neue
				 * Anfragen vom Browser nicht mehr erkennt. Jetzt schließt der Thread sobald in nichts mehr lesen
				 * kann weil der Browser nichts mehr sendet.
				 */
				
				
				terminate();
			}
		}
	}

	
	/**
	 * Methode zum Senden der Response.
	 * Baut aus der Datei, der Version, dem Statuscode, der URL der Datei und der Art der Anfrage die
	 * gesamte Response Nachricht mit Header und, falls benötigt, der angefragten Datei
	 * und sendet diese.
	 * 
	 * brauchen wir den OutputStream als Parameter?? wird hier doch erst angelegt oder?
	 * @param out			OutputStream zum Senden
	 * @param file			Datei die gesendet werden soll
	 * @param version		benutzte HTTP-Version
	 * @param statusCode	Response-Status-Code
	 * @param URL			URL der zu sendenden Datei
	 * @param HEAD			Ob die Anfrage HEAD war oder nicht
	 * 
	 * TODO: Exceptions?
	 * TODO: Pro Thread eine Anfrage oder mehr? Out schließen oder nicht?
	 */
	private void sendResponse(BufferedOutputStream out, File file, String version, int statusCode, String URL, boolean HEAD)
	{
		try
		{
			//HashMap für die Reason-Phrases
			HashMap<Integer, String> phrases = new HashMap<Integer, String>();
			phrases.put(200, "OK");
			phrases.put(204, "No Content");
			phrases.put(304, "Not Modified");
			phrases.put(400, "Bad Request");
			phrases.put(403, "Forbidden");
			phrases.put(404, "Not Found");
			phrases.put(500, "Internal Server Error");
			phrases.put(501, "Not Implemented");
			
			//Status-Line
			//Version als String übernommen
			String Response = version + " " + statusCode + " " + phrases.get(statusCode) + " \r\n";
			
			//General-Header
			Response += "Server: RvSWebserver" + " \r\n";
			Response += "Date: " +  new Date().toString() + "\r\n";
			
			//Response-Header
			Response += "Location: " + URL + " \r\n";
			
			//Falls eine Datei gefunden wurde, füge Entity Header hinzu
			if(statusCode==200 && file != null)
			{
				Response +=	  "Content-Type: "   + getContentType(file) + "\r\n"
				            + "Content-Length: " + file.length()        + "\r\n" 
							+ "Last-Modified: "  + file.lastModified()  + "\r\n";
			}
			Response += "\r\n";
			
			//Zum Testen
			//System.out.println(Response);
			
			
			//Sende Header
			out.write(Response.getBytes());

			//Sende Datei (Falls gefunden und keine HEAD-Anfrage)
			if(statusCode==200 && file != null && !HEAD)
			{
				FileInputStream in = new FileInputStream(file);
				ByteArrayOutputStream arr = new ByteArrayOutputStream();
				int b = in.read();
				byte[] bytes;
				
				//Datei auslesen
				while(b!=-1)
				{
					arr.write(b);
					b = in.read();
				}
				in.close();
				
				//Falls die Datei Textbasiert ist muss der Text UTF8 kodiert sein
				if(getFileEnding(file).equals(".txt"))
				{
					//Umwandeln erst in String dann wieder in Byte[], doppelt gemoppelt aber hab bisher noch nix besseres gefunden
					String str = arr.toString("UTF8");
					bytes = str.getBytes("UTF8");
				}
				else
				{
					bytes = arr.toByteArray();
				}
				
				out.write(bytes, 0, bytes.length);
				String endResponse = " \n";
				out.write(endResponse.getBytes());
				
			}
			
			out.flush();				 
			
		}
		catch(IOException e)
		{
			System.err.println("Fehler beim Response schicken " + e);
		}
	}
	

	/**
	 * Methode die den MIME-Type bzw. die Endung einer Datei wiedergibt.
	 * 
	 * @param file	Datei deren MIME-Type ermittelt werden soll.
	 */
	private String getFileEnding(File file)
	{
        String filename = file.getName();
        int pos = filename.lastIndexOf(".");
        if (pos >= 0) 
        	{
        		return filename.substring(pos);
        	}
        return "";
    }
	
	/**
	 * Methode die den Content-Type einer Datei für den Entity-Header zurückgibt. 
	 * 
	 * @param file	Datei deren Content-Type ermittelt werden soll.
	 */
	private String getContentType(File file)
	{
		String ending = getFileEnding(file);
			
		//HashMap für die Content-Types
		HashMap<String, String> types = new HashMap<String, String>();
		types.put(".txt", "text/plain; charset=utf-8");
		types.put(".html", "text/html; charset=utf-8");
		types.put(".htm", "text/html; charset=utf-8");
		types.put(".css", "text/css; charset=utf-8");
		types.put(".ico", "image/x-icon");
		types.put(".pdf", "application/pdf");
				
		String end = types.get(ending);
		if(end != null)
		{
			return end;
		}
		
		//Falls kein bekannter Content-Type
		return "application/octet-stream";
	}
}