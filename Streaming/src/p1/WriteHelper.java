package p1;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class WriteHelper {
	
	public void writeStringToFile(String output, String name) throws IOException{
		URL url = this.getClass().getResource("website");
		File folder = new File(url.getFile());
		FileOutputStream fos = new FileOutputStream(folder.getAbsolutePath()+ File.separator + name);
		fos.write(output.getBytes());
		fos.close();
	}
	
	public void writeCSV(String output, String path , boolean append) {
		URL url = this.getClass().getResource("website");
		try {
		File file = new File(url.getPath(), path);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file, append);
		fos.write(output.getBytes());
		fos.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToFile(byte[] data, String folder,  String name, String type) {
		
		String cur_dir = System.getProperty("user.dir");
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		
		System.out.println(System.getProperty("os.name").toLowerCase());
		
		String command = "";
		String command2 = "";
		if(isWindows) {
			command = "cmd /c cd " + cur_dir + "\\p1\\website\\vids & rmdir /q /s " + folder + " & mkdir " + folder ;
			
		}else {
			command = "sh -c cd " + cur_dir + "/p1/website/vids && rm -rf " + folder + " && mkdir " + folder ;
		}
		String sep = File.separator;
		System.out.println("cmd is >>>"+ command);
		
		try {
			Process p1 = Runtime.getRuntime().exec(command);
			new Thread(new ProcessHandler(p1.getErrorStream())).start();
			new Thread(new ProcessHandler(p1.getInputStream())).start();
			p1.waitFor();
			p1.destroy();
			String file__ ="";
			file__ =  "p1/website/vids/" + folder;
			
			File f1 = new File(file__);
			f1.createNewFile();
			FileOutputStream fos = new FileOutputStream(f1 + sep + name + "." + type);
			//fos.write(data);
			fos.write(data, 0, data.length);
			fos.close();
			//ffmpeg -i file0.mp4 -profile:v baseline -level 3.0 -s 640x360 -start_number 0 -hls_time 10 -hls_list_size 0 -f hls file0.m3u8
			
			
		}catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void formatFile(String folder,  String name, String type) {
		//String cur_dir = System.getProperty("user.dir");
		String input_name = "p1\\website\\vids\\" + folder + "\\" + name + "." + type;
		String output_name = "p1\\website\\vids\\" + folder + "\\" + name + "." + "m3u8";
		
		String command = "cmd /c ffmpeg -i " + input_name + " -profile:v baseline -level 3.0 -s 640x360 "
				+ "-start_number 0 -hls_time 10 -hls_list_size 0 -f hls "+ output_name;
		
		System.out.println("command is $$$$$$$$$$$$$$" + command + " $$$$$$$$$$$$$$$$$$");
		try {
			
			
			Process process = Runtime.getRuntime().exec(command);
			new Thread(new ProcessHandler(process.getErrorStream())).start();
			new Thread(new ProcessHandler(process.getInputStream())).start();
			
			process.waitFor();
			process.destroy();
			System.out.println("\n\n %%%%%%%%%%%%%%%%%%%%%%%%%%%%% ffmpeg done converting %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	class ProcessHandler implements Runnable{
		InputStream input__;
		public ProcessHandler(InputStream is) {
			this.input__ = is;
		}
		@Override
		public void run() {
			try {
				int c;
				while ((c = input__.read()) != -1) {
					System.out.write(c);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
		}
	}
}
