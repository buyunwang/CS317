
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.io.*;

/**
 * 
 */

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 *
 */
public class DNSlookup {


	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static boolean tracingOn = false;
	static InetAddress rootNameServer;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String fqdn;
		
		int argCount = args.length;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		// get ip address in a byte form
		byte[] ipByte = rootNameServer.getAddress();
		fqdn = args[1];
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
				
				
		
		
		// HEADER SECTION 
		// Generate Query ID 
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(65536);
		ByteBuffer b = ByteBuffer.allocate(2);
		byte[] qID = b.array();
		qID.putInt(randomInt);
		
		// or
		// int curId = (int) System.currentTimeMillis() & 0xffff;

		// Generate QR to RD
		//byte[] qr = ByteBuffer.allocate(1);
		//qr.putInt(0);
		
		// QR to RCODE and QDCOUNT to ARCOUNT
		byte[] arr = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00,
		(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
		
		byte[] header = new byte[qID.length + arr.length];
		System.arraycopy(qID, 0, header, 0, qID.length);        //.length() or .length ?
		System.arraycopy(arr, 0, header, qID.length, arr.length);

		
		
		// QUESTION SECTION
		// convert a domain name to byte array
		//"www.ugrad.cs.ubc.ca"
		String[] parts = fqdn.split("\\."); 
		int num = parts.length();
		//byte[]  lengthOctets = new byte[num];
		int counter = 0;
		for (int i=0; i< num; i++){
			counter += parts[i].length();
		}
		byte[] qName = new byte[num+counter];
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		for (int i=0; i< num; i++){
			//counter += parts[i].length();
			//lengthOctets.add((byte)parts[i].length());
			outputStream.write( (byte)parts[i].length() );
			outputStream.write( parts[i].getBytes(StandardCharsets.UTF_8) );
			
        }
        byte[] end = new byte[] {(byte)0x00};
        outputStream.write(end);

		qName = outputStream.toByteArray();
		
	    byte[] qtype = new byte[] {(byte)0x00 , (byte)0x01};
	
        byte[] qclass = new byte[] {(byte)0x00 , (byte)0x01};

        ByteArrayOutputStream que = new ByteArrayOutputStream( );
        que.write(qName);
        que.write(qtype);
        que.write(qclass);
        byte[] question = que.toByteArray();


		byte[] query = new byte[header.length + question.length];
		System.arraycopy(header, 0, query, 0, header.length);
		System.arraycopy(question, 0, query, header.length, question.length);

		
		
		
		
		
		// sends the query to the provided nameserver 
		DatagramPacket packet = new DatagramPacket(buf, buf.length, 
					    address, 4445);
		socket.send(packet);
	}

	private static void usage() {
		System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-t]");
		System.out.println("   where");
		System.out.println("       rootDNS - the IP address (in dotted form) of the root");
		System.out.println("                 DNS server you are to start your search at");
		System.out.println("       name    - fully qualified domain name to lookup");
		System.out.println("       -t      -trace the queries made and responses received");
	}
}


