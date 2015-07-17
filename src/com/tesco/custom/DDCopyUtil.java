package com.tesco.custom;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;

class DDCopyUtil {

	private static Logger log = Logger.getLogger(DDCopyUtil.class.getName());

	public static void copyDD(String src, String dst) throws IOException {
		try {

			log.info("Before copy---------------------");

			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			log.info("End copy---------------------");

		} catch (Exception e) {

			log.info("In copy Exception---------------------" + e);

		}
	}

}