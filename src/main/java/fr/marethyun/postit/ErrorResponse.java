package fr.marethyun.postit;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("error_code")
    public final int errorCode;

    public ErrorResponse(int errorCode) {
        this.errorCode = errorCode;
    }
}
