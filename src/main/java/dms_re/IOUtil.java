package dms_re;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class IOUtil {

	public static long readLong(File file){
		BufferedReader br=null;
		long lastPosition= 0;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line=br.readLine();
			lastPosition = Long.parseLong(line);			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
		return lastPosition;
		
	}
	
	public static String readString(RandomAccessFile raf, int length){
		
		byte[] buf = new byte[length];
		try {
			raf.read(buf);
			return new String(buf,"ISO8859-1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
