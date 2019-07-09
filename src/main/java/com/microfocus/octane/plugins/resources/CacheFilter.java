package com.microfocus.octane.plugins.resources;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        filterChain.doFilter(servletRequest, servletResponse);
        //httpResponse.setDateHeader("Expires", System.currentTimeMillis() + 604800000L); // 1 wee
    }

    @Override
    public void destroy() {

    }
}
