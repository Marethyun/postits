package fr.marethyun.postit;

import com.google.gson.annotations.SerializedName;

public enum Error {
    INVALID_USERNAME(1, "Le nom d'utilisateur est invalide"),
    INVALID_PASSWORD(2, "Le mot de passe est invalide"),
    NO_SESSION(5, "Vous devez être connecté pour faire cela"),
    ALREADY_LOGGED(6, "Vous êtes déjà connecté"),
    ERROR(666, "Quelque chose de mal est arrivé");

    public final int code;
    @SerializedName("message")
    public final String genericMessage;

    Error(int code, String genericMessage) {
        this.code = code;
        this.genericMessage = genericMessage;
    }

    @Override
    public String toString() {
        return this.genericMessage;
    }
}
