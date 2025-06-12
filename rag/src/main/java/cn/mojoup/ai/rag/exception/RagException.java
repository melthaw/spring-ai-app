package cn.mojoup.ai.rag.exception;

public class RagException extends RuntimeException {

    private int code;

    public RagException(int code) {
        this.code = code;
    }

    public RagException(String message) {
        super(message);
        this.code = code;
    }

    public RagException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public RagException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RagException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public RagException(int code,
                        String message,
                        Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
