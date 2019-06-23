package com.microfocus.octane.plugins.resources.pojo;

public class Select2Item {
    private String id;
    private String text;


    public Select2Item() {

    }

    public Select2Item(String id, String name) {
        setId(id);
        setText(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
