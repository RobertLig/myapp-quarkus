package org.example.myapp.auth;

public class GooglePayload {
    private String email;
    private String name;
    private String subject; // Google user ID

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getSubject() { return subject; }

    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setSubject(String subject) { this.subject = subject; }
}
