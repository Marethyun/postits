package fr.marethyun.postit;

public class AuthRequest {
    public final String username;
    public final String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
