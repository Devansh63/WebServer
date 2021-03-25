import java.io.* ;
import java.net.* ;
import java.util.* ;
 
public final class WebServer
{
	public static void main(String argv[]) throws IOException 
	    {
	        // Set the port number.
	        int port = 6789;
	        System.out.println("Testing");

	        // Establish the listen socket.
	        ServerSocket listensocket = new ServerSocket(port);

	        // Process HTTP service requests in an infinite loop.
	        while (true)
	        {
	            // Listen for a TCP connection request.
	            Socket connecte = listensocket.accept();

	            // Construct an object to process the HTTP request message.
	            HttpRequest request = null;
				try {
					request = new HttpRequest(connecte);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            // Create a new thread to process the request.
	            Thread thread = new Thread(request);

	            // Start the thread.
	            thread.start();
	        }
	    }
	}
final class HttpRequest implements Runnable {
    private final static String CRLF = "\r\n";
    private Socket socket;

    //Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private void processRequest() throws Exception {

        // Get a reference to the socket's input and output streams.
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up input stream filters.
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();

        // Display request line.
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines.
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // Skip over the method, which should be "GET"
        String fileName = tokens.nextToken(); 

        // Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;

        // Open the request file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false; 
        }

        // Construct the response message
        String statusLine = null; 
        String contentTypeLine = null; 
        String entityBody = null; 
        if (fileExists) {
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        }
        else {
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: text/html" + CRLF;
            entityBody = "<HTML>" + 
                    "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                    "<BODY>Not Found</BODY></HTML>";
        }

        // Send the status line.
        os.writeBytes(statusLine);

        // Send the content type line.
        os.writeBytes(contentTypeLine);

        // Send a blank line to indicate the end of the header lines.
        os.writeBytes(CRLF);

        // Send the entity body
        if (fileExists) {
        	   sendBytes(fis, os);
        	   fis.close();
        }
        else {
            os.writeBytes(entityBody);
        }

        // Close streams and socket.
        os.close();
        br.close();
        socket.close();
    }
    
    private static void sendBytes (FileInputStream fis, DataOutputStream os) throws Exception {
        // Constructs a 1K Buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copy requested file into the socket's output stream.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String filename) {
      
        if (filename.endsWith(".htm") || filename.endsWith(".html")) {
            return "text/html";
        }

        if (filename.endsWith(".gif")) {
            return "image/gif";
        }
        
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        return "application/octet-stream";

    }

 
    
}
