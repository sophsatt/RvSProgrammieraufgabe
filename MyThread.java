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
	
	
	
	/**
	 * Methode zum Senden der Response.
	 * Baut aus der Datei, der Version, dem Statuscode, der URL der Datei und der Art der Anfrage die
	 * gesamte Response Nachricht mit Header und, falls benötigt, der angefragten Datei
	 * und sendet diese.
	 * 
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
	private void sendResponse(BufferedOutputStream out, File file, double version, int statusCode, String URL, boolean HEAD)
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
			String Response = "HTTP/" + version + " " + statusCode + " " + phrases.get(statusCode) + " \r\n";
			
			//General-Header
			Response += "Date: " +  new Date().toString() + " \r\n";
			
			//Response-Header
			Response += "Location: " + URL + " \r\n";
			
			//Falls eine Datei gefunden wurde, füge Entity Header hinzu
			if(statusCode==200 && file != null)
			{
				Response +=	  "Content-Length: " + file.length()        + " \r\n"
							+ "Content-Type: "   + getContentType(file) + " \r\n"
							+ "Last-Modified: "  + file.lastModified()  + " \r\n"
							+ " \r\n";
			}
			
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
				
				/**
				 * TODO: Output schließen oder mehrere Antworten pro Thread? 
				 * 
				 * out.flush();
				 * out.close();
				 */
			}
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
	 * Methode die den Content-Type einer Datei für den Entity-Header zurückgibt. 
	 * 
	 * @param file	Datei deren Content-Type ermittelt werden soll.
	 */
	private String getContentType(File file)
	{
		String ending = getFileEnding(file);
			
		//HashMap für die Content-Types
		HashMap<String, String> types = new HashMap<String, String>();
		types.put(".txt", "text/plain; charset=utf-0");
		types.put(".html", "text/html; charset=utf-0");
		types.put(".htm", "text/html; charset=utf-0");
		types.put(".css", "text/css; charset=utf-0");
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