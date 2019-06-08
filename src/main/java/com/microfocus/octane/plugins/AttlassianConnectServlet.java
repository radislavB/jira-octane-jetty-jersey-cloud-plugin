package com.microfocus.octane.plugins;

import org.apache.commons.codec.Charsets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.stream.Collectors;

public class AttlassianConnectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");


        String filename = "/WEB-INF/atlassian-connect.json";
        ServletContext context = getServletContext();
        InputStream is = context.getResourceAsStream(filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8))) {
            String str = br.lines().collect(Collectors.joining(System.lineSeparator()));
            out.print(str);
        }
        super.doGet(req, resp);
    }




}
