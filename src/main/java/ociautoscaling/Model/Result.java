package ociautoscaling.Model;

public class Result<T> {
    private boolean isSuccess;
    private T result;
    private String msg;

    public Result(boolean isSuccess, T t, String msg) {
        this.isSuccess = isSuccess;
        this.result = t;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
