package com.app.mederrand.models.local;

public class EmergencyContact {
    String relation;
    String name;
    String contact;

    // Default constructor required for Firebase
    public EmergencyContact() {
    }

    public EmergencyContact(String relation, String name, String contact) {
        this.relation = relation;
        this.name = name;
        this.contact = contact;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
