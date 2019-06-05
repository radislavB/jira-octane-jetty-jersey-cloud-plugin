package com.microfocus.octane.plugins;

import org.apache.commons.codec.Charsets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.stream.Collectors;

public class NotFoundServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContextPath().equals("") && req.getPathInfo().equals("/")) {
            getAtlassianConnectFile(resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getAtlassianConnectFile(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String filename = "/WEB-INF/atlassian-connect.json";
        ServletContext context = getServletContext();
        InputStream is = context.getResourceAsStream(filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8))) {
            String str = br.lines().collect(Collectors.joining(System.lineSeparator()));
            out.print(str);
        }
    }
}
