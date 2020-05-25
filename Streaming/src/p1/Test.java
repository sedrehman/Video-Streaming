package p1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Test {
	static URL folder = Test.class.getResource("website/");

	public static void main(String[] args) {
		try {
			//Process p = Runtime.getRuntime().exec("powershell ffmpeg --help");
			//Process p = Runtime.getRuntime().exec("cmd /c ffmpeg --help");
			//System.out.println(folder.getPath());
			Process p = Runtime.getRuntime().exec("cmd /c cd " + folder.getPath() + " & cd p1" + File.separator +"website"
					 + File.separator +"vids & mkdir file01 & dir");
			//Process p = Runtime.getRuntime().exec("cmd /c ffmpeg --help");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String result = "";
			String r = null;
			while((r = reader.readLine()) != null) {
				result += r + "\n";
			}
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
