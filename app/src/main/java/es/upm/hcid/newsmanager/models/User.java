package es.upm.hcid.newsmanager.models;

/**
 * Simple data class for user information
 */
public class User {
    private int id;
    private String username;
    private String apiKey;
    private String authType;
    private boolean isAdmin = false;

    public User(int id, String username, String apiKey, String authType) {
        this.id = id;
        this.username = username;
        this.apiKey = apiKey;
        this.authType = authType;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAuthType() {
        return authType;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
