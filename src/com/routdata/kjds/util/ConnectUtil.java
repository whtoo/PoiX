package com.routdata.kjds.util;

import java.io.IOException;
import java.util.Properties;
//读取配置文件信息
public class ConnectUtil {
	static Properties props = new Properties();
	static {
		try {
			props.load(ConnectUtil.class.getClassLoader().getResourceAsStream("config/connectUtil.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperties(String key) {
		return props.getProperty(key);
	}
}
