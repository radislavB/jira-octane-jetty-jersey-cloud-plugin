package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.JiraTenantSecurityContext;

import java.io.IOException;


public class SecurityContextManager extends BaseManager<JiraTenantSecurityContext> {


    public static SecurityContextManager instance = new SecurityContextManager();
    private final String SECURITY_CONTEXT_FILE_PREFIX = "context_";

    private SecurityContextManager() {

    }

    public static SecurityContextManager getInstance() {
        return instance;
    }

    public void install(JiraTenantSecurityContext securityContext) throws IOException {
        save(securityContext.getClientKey(), securityContext);
    }

    public void uninstall(String clientKey) {
        remove(clientKey);
    }

    public JiraTenantSecurityContext getSecurityContext(String clientKey) {
        return getItem(clientKey);
    }

    @Override
    protected Class<JiraTenantSecurityContext> getTypeClass() {
        return JiraTenantSecurityContext.class;
    }

    @Override
    protected String getItemFilePrefix() {
        return SECURITY_CONTEXT_FILE_PREFIX;
    }
}
