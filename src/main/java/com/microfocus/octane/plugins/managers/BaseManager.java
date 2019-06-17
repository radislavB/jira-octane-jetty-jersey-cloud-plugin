package com.microfocus.octane.plugins.managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseManager<T> {

    private File repositoryFolderFile;
    private Map<String, T> items = new HashMap<>();

    private static Object syncObject = new Object();

    public void init(String repositoryFolder) {
        this.repositoryFolderFile = new File(repositoryFolder);

        File repositoryFolderFile = new File(repositoryFolder);
        synchronized (syncObject) {
            repositoryFolderFile.mkdirs();
        }
    }

    private File getRepositoryFolder() {
        return repositoryFolderFile;
    }

    protected abstract Class<T> getTypeClass();

    protected abstract String getItemFilePrefix();

    private File getItemFile(String clientKey) {
        return new File(getRepositoryFolder(), getItemFilePrefix() + clientKey);
    }

    protected void save(String clientKey, T item) throws IOException {
        items.put(clientKey, item);
        saveToFile(clientKey, item);
    }

    private void saveToFile(String clientKey, T item) throws IOException {
        //save to file
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File file = getItemFile(clientKey);
        file.createNewFile();
        mapper.writeValue(file, item);
    }

    protected void remove(String clientKey) {
        items.remove(clientKey);
        getItemFile(clientKey).delete();
    }

    protected T getItem(String clientKey) {
        if (items.containsKey(clientKey)) {
            return items.get(clientKey);
        } else {
            File f = getItemFile(clientKey);
            if (f.exists()) {
                final ObjectMapper mapper = new ObjectMapper();

                try {
                    T item = mapper.readValue(f, getTypeClass());
                    items.put(clientKey, item);
                    return item;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read item", e);
                }
            } else {
                throw new RuntimeException("Item for client is not found");
            }
        }
    }

    protected T getItemOrCreateNew(String clientKey) {
        if (items.containsKey(clientKey) || getItemFile(clientKey).exists()) {
            return getItem(clientKey);
        } else {
            try {
                return getTypeClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to create a new Item : " + e.getMessage());
            }
        }
    }
}
