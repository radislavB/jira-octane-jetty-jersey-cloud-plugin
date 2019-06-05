package com.microfocus.octane.plugins.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microfocus.octane.plugins.rest.pojo.JiraTenantSecurityContext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class SecurityContextManager {

    private String repositoryFolder;
    public static SecurityContextManager instance = new SecurityContextManager();
    private String SECURITY_CONTEXT_FILE_PREFIX = "context_";
    private Map<String, JiraTenantSecurityContext> contexts = new HashMap<>();

    private SecurityContextManager() {

    }

    public static SecurityContextManager getInstance() {
        return instance;
    }

    public void init(String repositoryFolder) {
        this.repositoryFolder = repositoryFolder;
        File repositoryFolderFile = new File(repositoryFolder);
        repositoryFolderFile.mkdirs();

        final ObjectMapper mapper = new ObjectMapper();
        File[] files = repositoryFolderFile.listFiles((dir, name) -> name.startsWith(SECURITY_CONTEXT_FILE_PREFIX));
        (files == null ? Stream.<File>empty() : Arrays.stream(files)).forEach(f -> {
            try {
                JiraTenantSecurityContext securityContext = mapper.readValue(f, JiraTenantSecurityContext.class);
                contexts.put(securityContext.getClientKey(), securityContext);
            } catch (IOException e) {
                //TODO - add log of failing to read and parse file
            }
        });

    }

    public void install(JiraTenantSecurityContext securityContext) throws IOException {
        contexts.put(securityContext.getClientKey(), securityContext);

        //save to file
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File file = getSecurityContextFile(securityContext.getClientKey());
        file.createNewFile();
        mapper.writeValue(file, securityContext);
    }

    private File getSecurityContextFile(String clientKey) {
        return new File(repositoryFolder, SECURITY_CONTEXT_FILE_PREFIX + clientKey);
    }

    public void uninstall(String clientKey) {
        contexts.remove(clientKey);
        getSecurityContextFile(clientKey).delete();
    }

    public JiraTenantSecurityContext getSecurityContext(String clientKey) {
        if (contexts.containsKey(clientKey)) {
            return contexts.get(clientKey);
        } else {
            throw new RuntimeException("Client not found");
        }
    }


}
