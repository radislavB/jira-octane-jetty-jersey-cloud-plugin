package com.microfocus.octane.plugins.utils;

import org.apache.commons.codec.Charsets;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.stream.Collectors;

public class ResourceUtils {

    public static String readFile(ServletContext servletContext, String resourceFilePath) throws IOException {
        InputStream is = servletContext.getResourceAsStream(resourceFilePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
