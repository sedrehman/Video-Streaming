package p1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class RequestHandler implements Runnable{
	
	private static final String[] protocols = new String[] {"TLSv1.2"};
	private FileReaderHelper helper = new FileReaderHelper();
	private Header headGenerator = new Header();
	private SSLSocket client;
	private InputStream is;
	private Map<String, String> headers;
	private int FILE_COUNTER = 0;
	private String FILE_NAME = "file";
	private FileCounter fc;
	
	public RequestHandler(SSLSocket client, FileCounter fc) {
		//System.out.println("here");
		this.client = client;
		this.fc = fc;
		headers = new HashMap<String, String>();
		try {
			is = client.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//FILE_COUNTER = fc.FILE_COUNTER;
	}
	
	@Override
	public void run() {
		try {
			client.startHandshake();
			client.setSoTimeout(2000);
			while(!client.isClosed()) {
				String line = getLine();
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+line);
				String type[] = line.split(" ");
				headers.put("Path", type[1].trim() );
				
				if(type[0].equals("POST")) {
					handlePost();
					break;
				}else {
					handleGet();
					String connection = headers.get("Connection");
					if(connection != null) {
						
						type = connection.split(",");
						for(String e : type) {
							e = e.trim();
							if(e.equals("Upgrade")) {
								handleWebSocket();
							}else {
								handleNormalGet(null, null);
								break;
							}
						}
					}else {
						handleNormalGet(null, null);
						break;
					}
				}
			}
		}catch(javax.net.ssl.SSLHandshakeException e2) {
			//System.out.println("~~~~~~~~~~~~~~~");
		}catch(SocketTimeoutException e) {
			try {
				client.close();
			} catch (Exception e1) {
				System.out.println("###################");
			}
		}catch(IOException e) {
			
		}
		//System.out.println("done..\n");
	}
	
	private void handleNormalGet(String msg, String setCookie) {
		System.out.println(headers.toString());
		String head = null;
		byte body[] = null;
		String accept = headers.get("Accept");
		String path = headers.get("Path");
		
		if(accept.equals("*/*")) {
			accept = "text/javascript";
		}
		if(path.equals("/")) { 
			path = "index.html";
		}
		body = helper.getResponse(path);
		if(body == null) {
			body =  "THE CONTENT WAS NOT FOUND".getBytes();
			head = headGenerator.create404Header();
		}
		
		if(head == null) {
			head = headGenerator.createHeader(accept, "none", body.length);
		}
		send(head, body);
	}
	
	private void post_response(String file) {
		headers.put("Path", "/myVideo.html");
		headers.put("Accept", "text/html");
		handleNormalGet(null, null);
	}
	//System.out.println("next - " + next + " offset - " + offset + " buffer_size - " + buffer_size);
	private void send(String head, byte[] body) {
		try {	
			PrintWriter writer = new PrintWriter(client.getOutputStream());
			writer.println(head);
			writer.flush();
			OutputStream os = client.getOutputStream();
			int size = body.length;
			int offset = 0;
			int next ;
			int buffer_size = 1024;
			if(size < buffer_size) {
				buffer_size = size;
			}
			while(offset < size) {
				next = offset + buffer_size;
				if(next > size) {
					buffer_size = size - offset;
				}
				os.write(body, offset, buffer_size);
				offset = next;
			}
			writer.close();
			if(!client.isClosed()) {
				client.close();
			}
		} catch (IOException e) {

		} 
	}

	private void handleGet() {
		String line = null;
		try {
			while( (is.available() > 0) && (line = getLine()).length() > 0) {
				if(line.length() < 2) {
					break;
				}
				String type[] = line.split(":");
				switch(type[0]) {
				case "Accept":
					headers.put("Accept", type[1].split(",")[0].trim());
					break;
				case "Cookie":
					String cookie = type[1].trim().replace("token=", "");
					headers.put("Cookie", cookie);
					break;
				case "Connection":
					headers.put("Connection", type[1].trim());
					break;
				case "Upgrade":
					headers.put("Upgrade", type[1].trim());
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleWebSocket() {
		
	}
	
	private void handlePost() {
		System.out.println("handling post");
		
		String line = null;
		while((line = getLine()) != null && line.length() > 2) {
			System.out.println(line.trim());
			if(line.length() > 2) {
				String type[] = line.split(":");
				switch(type[0]) {
				case "Content-Length":
					headers.put("Content-Length", type[1].trim());
					break;
				case "Content-Type":
					String[] content = type[1].split(";");
					String content_type = content[0].trim();
					if(content_type.equals("multipart/form-data")) {
						headers.put("boundary", content[1].trim().split("=")[1].trim());
					}
					headers.put("Content-Type", content[0].trim());
					break;
				}
			}
		}
		
		int data_size = Integer.parseInt(headers.get("Content-Length")) ;
		byte data[] = new byte[data_size];
		try {
			System.out.print("########### about to read data..");
			int offset = data_size/30;
			for(int i = 0; i< data_size; i++ ) {
				data[i] = (byte) is.read();
				
				if(i % offset == 0) {
					System.out.print(".");
				}
			}
//			is.read(data, 0, data_size);
			System.out.print("done reading...###########\n\n");
		}catch(IOException e) {
			System.out.println("here  data  reading problem....");
			e.printStackTrace();
		}
		if(headers.get("Content-Type").equals("multipart/form-data")) {
			handlePostData(data);
		}
	}
	

	private Void handlePostData(byte data[]) {
		String endkey = headers.get("boundary").trim() + "--";
		WriteHelper helper = new WriteHelper();
		int counter = 0;
		int byte_in;
		byte[] whole_line = new byte[1000];
		byte[] actual_line;
		String line;
		String type[];
		boolean isVideo = false;
		int body_size = Integer.parseInt(headers.get("Content-Length"));
		String video_name = "";
		
		for(int i = 0; i< body_size; i++) {
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Get line~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			counter = 0;
			while((byte_in = data[i]) != 0xA) {
				whole_line[counter++] = (byte) byte_in;
				i++;
			}
			actual_line = new byte[counter];
			
			while(counter >= 1) {
				actual_line[--counter] = whole_line[counter];
			}
			line = new String(actual_line, StandardCharsets.UTF_8);
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			if(line.length() > 2) {

				type = line.split(":");
				if(type.length > 1) {
					String temp[] = type[1].split(";");

					switch(type[0].trim()) {
					case "Accept":
						headers.put("Accept", type[1].split(",")[0].trim());
						break;
					case "Content-Disposition":
						headers.put("Content-Disposition", temp[0].trim());
						//Content-Disposition: form-data; name="upload"; filename="icon.png"
						//   temp=                 0           1                  2
						for(int k =1; k < temp.length; k++) {
							temp[k] = temp[k].replaceAll("\"", "");
							String val[] = temp[k].trim().split("=");
							headers.put(val[0], val[1]);
						}
						break;
					case "Content-Type":

						headers.put("Content-Type", temp[0].trim());
						//Content-Type: image/png
						//  temp=          0
						if(temp[0].contains("video")) {
							isVideo = true;
							headers.put("filetype", temp[0].trim().split("/")[1]);
						}
						break;
					}
					if(isVideo) {
						
						// handle image from i to (end - endKey.length())
						String __type = headers.get("filetype");
						System.out.println("%%%%%%%%%%%%%%%%%% "+ __type +" vid %%%%%%%%%%%%%%%%%%%%%%%%%");
						byte[] file_ = Arrays.copyOfRange(data, i + 3, body_size- (endkey.length() +6));
						video_name = FILE_NAME + Integer.toString(FILE_COUNTER);
						//fc.FILE_COUNTER++;
						helper.writeToFile(file_, video_name, video_name , __type);
						helper.formatFile(video_name , video_name ,__type);
						i = body_size;
						break;
					}
				}
				else if(line.contains(endkey)) {
					//we are done. break everything.
					i = body_size;
					break;
				}
			}
			
		}
		post_response(video_name);
		return null;
	}

	private String getLine() {
		try {
			byte[] whole_line = new byte[1000];
			int counter = 0;
			int byte_in;
			while((byte_in = is.read())!= 0xA && (byte_in != -1)) {
				whole_line[counter++] = (byte) byte_in;
			}
			byte[] actual_line = new byte[counter];
			while(counter >= 1) {
				actual_line[--counter] = whole_line[counter];
			}
			String l = new String(actual_line, StandardCharsets.UTF_8);
			//System.out.println(l);
			return l;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
