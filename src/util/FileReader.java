package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FileReader {
	File file = null;
    BufferedReader reader = null;
    FileInputStream fis = null;
    InputStreamReader isr = null;
    
    public FileReader() {}
    public FileReader(String filePath, String charset) {
    	openFile(filePath, charset);
    }
    
    public void close() {
    	try {
			isr.close();
	    	fis.close();
	    	reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void openFile(String filePath, String charset) {
		try {
			fis = new FileInputStream(filePath);
			isr = new InputStreamReader(fis, charset);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        reader = new BufferedReader(isr);
        file = new File(filePath);
	}
	
    public String readLine() {
    	String ret = null;
    	try {
			ret = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
    }
}
