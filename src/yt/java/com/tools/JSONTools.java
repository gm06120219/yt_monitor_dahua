package yt.java.com.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JSONTools {

	public static int HasKeyValue(JSONArray list, String key, String value) {
		for (int i = 0; i < list.size(); i++) {
			JSONObject temp = list.getJSONObject(i);
			String tempValue = temp.getString(key);
			if(tempValue.equals(value) == true) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static JSONObject ReadJsonFile(String path) throws IOException {
		JSONObject json = null;
		try {
			File jsonFile = new File(path);
			FileReader fileReader = new FileReader(jsonFile);

			Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
			int ch = 0;
			StringBuffer sb = new StringBuffer();
			while ((ch = reader.read()) != -1) {
				sb.append((char) ch);
			}
			fileReader.close();
			reader.close();
			json = JSONObject.parseObject(sb.toString());
		} catch (IOException e) {
			throw e;
		}
		return json;
	}
	
	public static void WriteJsonFile(String path, JSONObject content) throws IOException {
		File json_file = null;
		FileOutputStream out_stream = null;
		OutputStreamWriter out_writer = null;
		try {
			json_file = new File(path);
			out_stream = new FileOutputStream(json_file);
			out_writer = new OutputStreamWriter(out_stream, "UTF-8");
			
			out_writer.write(JSONObject.toJSONString(content, true));
			out_writer.flush();
			
			out_stream.close();
			out_writer.close();
		} catch (IOException e) {
			throw e;
		}
	}
}
