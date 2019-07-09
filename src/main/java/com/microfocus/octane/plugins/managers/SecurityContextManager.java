package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.JiraTenantSecurityContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SecurityContextManager extends BaseManager<JiraTenantSecurityContext> {


    public static SecurityContextManager instance = new SecurityContextManager();
    private static final String FILE_NAME = "security_context.json";

    private SecurityContextManager() {

    }

    public static SecurityContextManager getInstance() {
        return instance;
    }

    public void install(JiraTenantSecurityContext securityContext) throws IOException {
        save(securityContext.getClientKey(), securityContext);
    }

    @Override
    protected void save(String clientKey, JiraTenantSecurityContext item) throws IOException {
        super.save(clientKey, item);

        //append to list
        try {
            File listFile = new File(getItemFile(item.getClientKey()).getParentFile().getParentFile(), "tenants.list.txt");
            if (!listFile.exists()) {
                listFile.createNewFile();
            }
            Path path = Paths.get(listFile.getAbsolutePath());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(new Date());

            String textToAppend = String.format("%s\t%s\t%s%s", strDate, item.getClientKey(), item.getBaseUrl(), System.lineSeparator());
            Files.write(path, textToAppend.getBytes(), StandardOpenOption.APPEND);  //Append mode
        } catch (Exception e) {
            //TODO add log
            int t = 5;
        }
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
    protected String getItemFileName() {
        return FILE_NAME;
    }
}
