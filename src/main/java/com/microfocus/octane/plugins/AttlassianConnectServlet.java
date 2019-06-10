package com.microfocus.octane.plugins;

import com.microfocus.octane.plugins.utils.ResourceUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AttlassianConnectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");


        String filename = "/WEB-INF/atlassian-connect.json";
        String content = ResourceUtils.readFile(getServletContext(), filename);
        out.print(content);
    }
}
