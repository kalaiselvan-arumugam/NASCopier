package com.nas.copier.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class ApplicationConfig {

	private static final Properties properties = new Properties();

	public boolean loadApplicationProperties(String configFilePath) throws IOException, URISyntaxException {
		try (FileInputStream input = new FileInputStream(configFilePath)) {
			properties.load(input);
			System.out.println("Properties loaded successfully");
			return true;
		} catch (Exception ex) {
			System.err.println("Exception while reading properties file: " + ex.getMessage());
			return false;
		}
	}

	public static String getTag(String key) {
		String value = properties.getProperty(key);
		return (value != null) ? value.trim() : "NA";
	}

}
