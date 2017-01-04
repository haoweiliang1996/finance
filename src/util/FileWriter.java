package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class FileWriter {
	File file = null;
	BufferedWriter bw = null;
    OutputStreamWriter osw = null;
    FileOutputStream fos = null;
    
//    public FileWriteObj() {}
    public FileWriter(String filePath, String charset, boolean append) {
    	openFile(filePath, charset, append);
	}

	/**
	 * create by haowei
	 * @param filePath 文件路径
	 * @param charset 文件编码
	 * @param append 是否以append形式写入
	 * @param newFile 是否需要先创建文件
	 * @throws IOException
	 */
	public FileWriter(String filePath, String charset, boolean append,boolean newFile) throws IOException{
		if (newFile)
		{
			 File fileOut = new File(filePath);
        	 fileOut.createNewFile();
		}
		openFile(filePath, charset, append);
	}

    public void close() {
    	try {
			fos.close();
	    	osw.close();
	    	bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void openFile(String filePath, String charset, boolean append) {
		try {
			file = new File(filePath);
            file.getParentFile().mkdirs();
			fos = new FileOutputStream(filePath, append);
			osw = new OutputStreamWriter(fos, charset);
			bw = new BufferedWriter(osw);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        file = new File(filePath);
	}

    public void write(String content) {
    	try {
			bw.write(content);
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
