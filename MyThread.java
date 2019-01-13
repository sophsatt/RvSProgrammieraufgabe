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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class MyThread extends Thread {
	private boolean terminate;
	private Socket socket;
	private File wwwroot = null;
	private String ipAdresse;
	static final String DEFAULT_FILE = "index.html";
	private BufferedReader in = null;
	private BufferedOutputStream out = null;
	
	MyThread(Socket s, File root, String ipAd) {
		this.terminate = false;
		this.socket = s;
		this.wwwroot = root;
		this.ipAdresse = ipAd;
		System.out.println("Konstruktor MyThread");
	
	}
	
	public void terminate() {
		System.out.println("in terminate MyThread");
		terminate = true;
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("run() in MyThread()");
		try {
			out = new BufferedOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while ( !terminate )
		{
			System.out.println("start while-Schleife Thread");

			//aus der while-Schleife nach oben verschoben
//			BufferedOutputStream out = null;
//			Bufferedreader in = null;
			try {
				//aus der while-Schleife verschoben
//				out = new BufferedOutputStream(socket.getOutputStream());
				
				//String output = "Verbindung aufgebaut zu " + ipAdresse + "..." +" \r\n";
				//out.write(output.getBytes());
				//out.flush();
				// eventuell output f�r Inhalt trennen?
				// BufferedReader und BufferedOutputStream auf Socket einrichten
				//zum Lesen des Requests und �bergeben der Response
				
				System.out.println("Mache neuen inputstream");

				//aus der while-Schleife verschoben
//				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
				
				
				String frstLine = in.readLine();
				
				String method = null;
				String requestedFile = null;
				String version = null;
				
				/**
				 * Der Browser schickt nach der ersten einfachen GET-Anfrage noch recht viele weitere Anfragen
				 * bzw. einfach Anweisungen f�r den Client (Bspw. welche Sprachen er unterst�tzt hab ich gesehen)
				 * Die sind teilweise auch nur 2 W�rter lang sodass der Tokenizer eine Exception wirft, diese Art
				 * Anfragen k�nnen wir aber einfach ignorieren.
				 */
				
				
				try {
					StringTokenizer frstParse = new StringTokenizer(frstLine);

					method = frstParse.nextToken();
					requestedFile = frstParse.nextToken();
					version = frstParse.nextToken();
					
				} catch(NullPointerException e){
					//TODO exception
					//Test
					System.out.println("NullpointerException");
				}
				
				
				/**
				 * Eine M�glichkeit die �berfl�ssigen Anfragen zu ignorieren, so wird immer "Not Implemented"
				 * zur�ckgegeben, was ja an sich auch stimmt, und der Browser arbeitet trotzdem einfach weiter 
				 */
				catch(NoSuchElementException e)
				{
					System.out.println("NoSuchElementException");
					version = "HTTP/1.0";			//Auch nicht wo wirklich richtig eigentlich
					method = "Not Implemented";		//Eigentlich egal was man nimmt, Hauptsache es wird als falsch erkannt
					requestedFile = null;
				}
				
				String sndLine;
				
				Date modDate;
				if ((sndLine = in.readLine()) != null && sndLine.length() != 0 && sndLine.contains("If-Modified-Since")) {
					StringTokenizer sndParse = new StringTokenizer(sndLine);
					System.out.println("Second Line" + sndLine);
					sndParse.nextToken();
					// TODO DateFormat anpassen! sind wahrscheinlich auch mehrere Token dann
					try {
						modDate = new SimpleDateFormat("dd/MM/yyyy").parse(sndParse.nextToken());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				System.out.println("REQUEST BROWSER " + method + " "+ requestedFile + " "+ version);
				
				int statusCode;
				
				File reqFile = null;
				
				String url = ipAdresse + requestedFile;
				
				boolean head = false;
				
				boolean conGet = false;
				
				//TODO �berpr�fen ob File vorhanden ist
				// unterst�tzt nur 1.0 und 1.1 Anfragen
				if(version.equals("HTTP/2.0")){
					statusCode = 400;
					sendResponse(out, reqFile, version, statusCode, url, head);
				}
				
				//nur GET, HEAD und POST werden unterst�tzt
				else if(!method.equals("GET") && !method.equals("POST") && !method.equals("HEAD")){
					statusCode = 501;
					sendResponse(out, reqFile, version, statusCode, url, head);
					//GET oder POST Methode, kein conditional Get
				} else if (method.equals("GET") || method.equals("POST") && !method.equals("HEAD") && conGet == false) {
					//kein Zugriff erlaubt auf Passw�rter
					if (requestedFile.endsWith("passwoerter.txt")) {
						statusCode = 403;
						sendResponse(out, null, version, statusCode, url, head);
					}
					//�berpr�fen ob im angegebenen Verzeichnis eine index-Datei liegt
					else if (requestedFile.endsWith("/")) {
						//index.???
						requestedFile += "index.html";
						System.out.println(requestedFile + " nach Index suchen");
					}
					//TODO F�lle: File nicht vorhanden, Directory angegeben, File vorhanden, keine Zugriffsrechte
					reqFile = new File(wwwroot, requestedFile);
					statusCode = 200;
					sendResponse(out, reqFile, version, statusCode, url, head);
					
				}
				
				
				
				//Fallunterscheidung GET,POST,HEAD,....
				
				
			} catch (FileNotFoundException e) {
//				try {
//					//fileNotFound(out, dataOut, fileRequested);
//				} catch (IOException ioe) {
//					System.err.println("Error with file not found exception : " + ioe.getMessage());
//				}
			
			} catch (IOException ioe) {
				ioe.printStackTrace();
//				System.out.println(ioe.getCause().toString());
				System.err.println("Server error : " + ioe);
				
				/**
				 * Es ist (denke ich jedenfalls und mit der �berlegung funktioniert es ja ziemlich gut) ein Thread
				 * nur f�r eine �bertragung zust�ndig, da (keine Ahnung wieso) bei Anfragen vom Browser der
				 * InputStream nicht blockiert und nurnoch Exceptions wirft die die Konsole fluten und auch neue
				 * Anfragen vom Browser nicht mehr erkennt. Jetzt schlie�t der Thread sobald in nichts mehr lesen
				 * kann weil der Browser nichts mehr sendet.
				 */
				
				
				terminate();
			}
				// TODO in & out closen???
		}
	}

	
	/**
	 * Methode zum Senden der Response.
	 * Baut aus der Datei, der Version, dem Statuscode, der URL der Datei und der Art der Anfrage die
	 * gesamte Response Nachricht mit Header und, falls ben�tigt, der angefragten Datei
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
	 * TODO: Pro Thread eine Anfrage oder mehr? Out schlie�en oder nicht?
	 */
	private void sendResponse(BufferedOutputStream out, File file, String version, int statusCode, String URL, boolean HEAD)
	{
		//BufferedOutputStream out = null;
		try
		{
			System.out.println("sendResponse");
			//out = new BufferedOutputStream(socket.getOutputStream());
			//HashMap f�r die Reason-Phrases
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
			//Version als String �bernommen
			//String Response = "HTTP/" + version + " " + statusCode + " " + phrases.get(statusCode) + " \r\n";
			String Response = version + " " + statusCode + " " + phrases.get(statusCode) + " \r\n";
			
			//General-Header
			Response += "Server: RvSWebserver" + " \r\n";
			Response += "Date: " +  new Date().toString() + "\r\n";
			
			//Response-Header
			Response += "Location: " + URL + " \r\n";
			
			//Falls eine Datei gefunden wurde, f�ge Entity Header hinzu
			if(statusCode==200 && file != null)
			{
				Response +=	  "Content-Type: "   + getContentType(file) + "\r\n"
				            + "Content-Length: " + file.length()        + "\r\n" 
							+ "Last-Modified: "  + file.lastModified()  + "\r\n";
			}
			Response += "\r\n";
			
			
			System.out.println(Response);
			//Sende Header
			out.write(Response.getBytes());
//			System.out.println("nach out write response");
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
			//out.close();
			//terminate();
				 
			
		}
		catch(IOException e)
		{
			//TODO Was machen mit Exceptions?
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
	 * Methode die den Content-Type einer Datei f�r den Entity-Header zur�ckgibt. 
	 * 
	 * @param file	Datei deren Content-Type ermittelt werden soll.
	 */
	private String getContentType(File file)
	{
		String ending = getFileEnding(file);
			
		//HashMap f�r die Content-Types
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