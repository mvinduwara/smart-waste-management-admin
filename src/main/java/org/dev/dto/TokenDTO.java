package org.dev.dto;

public class TokenDTO {

    private String AccessToken;
    private String RequestToken;
    private int id;

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String accessToken) {
        AccessToken = accessToken;
    }

    public String getRequestToken() {
        return RequestToken;
    }

    public void setRequestToken(String requestToken) {
        RequestToken = requestToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
