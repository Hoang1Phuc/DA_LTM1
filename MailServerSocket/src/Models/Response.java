package Models;

import java.io.Serializable;

/**
 *
 * @author Admin
 * @param <T>
 */
public class Response<T> implements Serializable {

    private String type;
    private T t;
    private String message; // Thêm thuộc tính message

    public Response(String type, T t) {
        this.type = type;
        this.t = t;
    }

    public Response(String type, T t, String message) { // Constructor mới để nhận message
        this.type = type;
        this.t = t;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public String getMessage() { // Thêm phương thức getMessage
        return message;
    }

    public void setMessage(String message) { // Thêm phương thức setMessage
        this.message = message;
    }
}
