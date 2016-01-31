
import java.lang.System;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.omg.CORBA.portable.OutputStream;



//
// This is an implementation of a simplified version of a command
// line dictionary client. The program takes no arguments.
//


public class CSdict
{
    static final int MAX_LEN = 255;
    static final int PERMITTED_ARGUMENT_COUNT = 1;
    static Boolean debugOn = false;
    static Socket client;
    static boolean connected;
    static String serverName;
    static String port;
    static String currentdict = "*";
    static List<String> dictlist = new ArrayList<String>();



    public static void main(String [] args)
    {

    System.setProperty("java.net.preferIPv4Stack" , "true");

	byte cmdString[] = new byte[MAX_LEN];

	if (args.length == PERMITTED_ARGUMENT_COUNT) {
	    debugOn = args[0].equals("-d");
	    if (debugOn) {
		System.out.println("Debugging output enabled");
	    } else {
		System.out.println("997 Invalid command line option - Only -d is allowed");
		return;
            }
	} else if (args.length > PERMITTED_ARGUMENT_COUNT) {
	    System.out.println("996 Too many command line options - Only -d is allowed");
	    return;
	}

	try {
	    for (int len = 1; len > 0;) {
		System.out.print("csdict> ");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String[] parts = br.readLine().split("\\s+");
			String arg1 = parts[0];


			switch(arg1){

			case "open":

				String arg2 = parts[1];
				String arg3 = "2628";

				if (parts.length == 3){

					arg3 = parts[2];
					System.out.println(parts[2]);
				}


				open(arg2, arg3);

				break;

			case "dict":

				dict();

				break;

			case "set" :

				arg2 = parts[1];
				setDictionary(arg2);

				break;

			case "currdict" :

				currdict();

				break;

			case "define" :

				arg2 = parts[1];
				define(arg2);

				break;

			case "match" :

				arg2 = parts[1];
				match(arg2);

				break;

			case "prefixmatch" :

				arg2 = parts[1];
				prefixmatch(arg2);

				break;

			case "close" :

				close();

				break;

			case "quit" :

				quit();

				break;


			}


	    }
	} catch (IOException exception) {

	    System.err.println("998 Input error while reading commands, terminating.");
	}

    }


    public static void open (String hostname, String portnumber){

    	if (isConnected())
        {
            return;
        }

        int pn = 2628;

    	try {

    	pn = Integer.parseInt(portnumber.trim());

    	}


    	catch (NumberFormatException e){
    		System.out.println("902 Invalid Argument 1");
    	}




    	if (pn < 0 || pn > 65535){
    		System.out.println("902 Invalid Argument 2");
    	}



    	String hn = hostname;

    	try {

    		SocketAddress sockaddr = new InetSocketAddress(hn, pn);

    		client = new Socket();
    		int timeout = 30000;

    		client.connect(sockaddr, timeout);


    		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),
                    "UTF-8"));
            String fromServer = in.readLine(); // Server banner

    		 if (fromServer.startsWith("220"))
                 {   // 220 = connect ok
    			 System.out.println("Socket connected.. " + client);
    		      connected=true;
    		      return;
                 }
    	}


    	catch (SocketTimeoutException e) {
    		System.out.println("902 Control connection to " + hn + " on port " + pn + "failed to open.");

    	}



    	catch (IOException ioe){
    		System.out.println("902 Control connection to " + hn + " on port " + pn + "failed to open.");

    	}

    	catch (IllegalBlockingModeException e){

    		System.out.println("902 Control connection to " + hn + " on port " + pn + "failed to open.");

    	}


    	catch (IllegalArgumentException e) {

    		System.out.println("902 Invalid Argument");
    	}

    }

    public static boolean isConnected()
    {
        return connected;
    }


	public static void dict() throws IOException{


    	PrintWriter output = null;
    	BufferedReader input = null;
    	String fromserver;




        try {
           output = new PrintWriter(CSdict.client.getOutputStream(), true);
           input = new BufferedReader(new InputStreamReader(CSdict.client.getInputStream(), "UTF-8"));

           output.println("SHOW DB");

           fromserver = input.readLine();

           if (fromserver.startsWith("110")){
           while ((fromserver = input.readLine()) != null && !fromserver.startsWith(".")){

		    		System.out.println(fromserver);
		    		dictlist.add(fromserver);


		    	}
		    }

        }

        catch (IOException e) {
           System.out.println("925 Control connection I/O error, closing control connection");
           close();
        }

    }

	public static void close() {

		String fromserver;
		boolean quit = false;

		if (!CSdict.isConnected()){

			return;
		}

		try {

			PrintWriter out = null;
			BufferedReader in;


			 out =
				    new PrintWriter(CSdict.client.getOutputStream(), true);


				in =
				    new BufferedReader(
				        new InputStreamReader(CSdict.client.getInputStream()));


		    out.println("QUIT");


		    while (quit == false && (fromserver = in.readLine()) != null){

		    	if (fromserver.startsWith("221")){
		    		quit = true;
		    	}
		    }

		    out.close();
		    in.close();
		    CSdict.client.close();

		    connected = false;

		    }

		    catch(IOException e) {

		    	e.printStackTrace();

		    }

	}

	public static void define (String word) {

		String fromserver;
		boolean quit = false;
		PrintWriter output = null;
		BufferedReader input = null;


		try {
	           output = new PrintWriter(CSdict.client.getOutputStream(), true);
	           input = new BufferedReader(new InputStreamReader(CSdict.client.getInputStream(), "UTF-8"));

	           output.println("DEFINE " + currentdict + " " + word);

	           fromserver = input.readLine();

	           if (fromserver.startsWith("150")){

	           while (quit == false && (fromserver = input.readLine()) != null){


	        	   if (fromserver.startsWith("250")){

	        		   quit = true;
	        		   break;

	        	   }
	        	   //if line starts with 151 replace start
	        	   if (fromserver.startsWith("151")){

	        		   int i = word.length();
	        		   String temp = fromserver.substring(5+i);
	        		   String blah = "@" + temp;
	        		   System.out.println(blah);

	        	   }


	        	   else {

	        	   System.out.println(fromserver);

	        	   }


	           }
	           }
	           else if (fromserver.startsWith("552")){

	        	   System.out.println("No matches found");

	           }
		}


	        catch (IOException e) {
	        	System.out.println("925 Control connection I/O error, closing control connection");
	            close();
	        }

	}

	public static void setDictionary(String name) {




		if (name =="*")
		{
			currentdict= "*";
			System.out.println("Default dictionary will be used!");
		}
		else if (name=="!")
		{
			currentdict= "!";
			System.out.println("First match will be set as the dict!");
		}
		else if (name.equals(null)){
			currentdict="*";
			System.out.println("Default dictionary will be used!");
		}

		else{
			if (dictlist == null)
			{
				try {
					dict();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Please open a server first!");
				}
			}
			String dictName ;
			String dictLine;

			for (int i=0; i<dictlist.size(); i++)
			{
				dictLine = dictlist.get(i);
				String[] parts = dictLine.split(" ");
				dictName = parts[0].trim();

				if (dictName.equals(name))
				{
					currentdict=name;
					System.out.println("The dictionary is set to "+currentdict);
				}

			}
		}

	}

	public static void currdict(){
		System.out.println("The current dictionary being used is "+currentdict);
	}

	public static void match(String word){

		List<String> matchList = new ArrayList<String>(); // All the dictionaries matching the word

		boolean quit = false;

		PrintWriter output = null;
		BufferedReader input = null;
		String fromserver;



		try {
			output = new PrintWriter(client.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

			output.println("MATCH "+currentdict +" exact "+ word );

			fromserver = input.readLine();

			System.out.println(fromserver);

			if (fromserver.startsWith("152")){
				while (quit == false && (fromserver = input.readLine()) != null)
				{
					if (fromserver.startsWith("552"))
					{
						quit=true;
					}
					else if (!fromserver.equals("."))
					{
						//matchList.add(fromserver+"/n");
						System.out.println(fromserver+"/n");
					}

					else if (fromserver.startsWith(".")){

						break;
					}
				}


			}
			else System.out.println("****No matches found****");
		}





		catch (IOException e) {
			System.out.println("925 Control connection I/O error, closing control connection");
	           close();
		}



	}

	public static void prefixmatch(String word){

		List<String> matchList = new ArrayList<String>(); // All the dictionaries matching the word

		boolean quit = false;

	//	isConnected();

		PrintWriter output = null;
		BufferedReader input = null;
		String fromserver;

		try {
			output = new PrintWriter(client.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

			output.println("MATCH "+currentdict +" prefix "+ word );

			fromserver = input.readLine();

			System.out.println(fromserver);

			if (fromserver.startsWith("152")){
				while (quit == false && (fromserver = input.readLine()) != null)
				{
					if (fromserver.startsWith("552"))
					{
						quit=true;
					}
					else if (!fromserver.equals("."))
					{
						System.out.println(fromserver+"/n");
					}
					else if (fromserver.startsWith(".")){

						break;
					}
				}


			}
			else System.out.println("****No prefix matches found****");
		}

		catch (IOException e) {
			System.out.println("925 Control connection I/O error, closing control connection");
	           close();
		}

	}


	public static void quit (){

		try {
			CSdict.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("999 Processing error." + e);
		}

		System.exit(0);

	}

    }

