package yt.java.com.tools;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class ByteTools {
	public static char[] BytesToChars(byte[] bytes) {
		Charset cs = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);
		return cb.array();
	}
	
	public static String BytesToString(byte[] content)  {
		System.out.println(content);
		int length = 0;
		for (int i = 0; i < content.length; i++) {
			if(content[i] == 0x00) {
				length = i;
				break;
			}
		}
		
		try {
			return new String(content, 0, length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
}
