package com.scn.common;

/**
 * Created by steve on 2017. 11. 26..
 */

public final class LiveDataResult<T> {

    public enum State {
        NONE,
        INPROGRESS,
        SUCCESS,
        ERROR
    }

    //
    // Members
    //

    private State state;
    private T data;
    private String message;

    //
    // Constructor
    //

    public LiveDataResult(State state, T data, String message) {
        this.state = state;
        this.data = data;
        this.message = message;
    }

    //
    // API
    //

    public State getState() { return state; }
    public T getData() { return data; }
    public String getMessage() { return message; }

    public static <T> LiveDataResult none() { return new LiveDataResult(State.NONE, null, null); }
    public static <T> LiveDataResult inProgress(T data) { return new LiveDataResult(State.INPROGRESS, data, null); }
    public static <T> LiveDataResult success(T data) { return new LiveDataResult(State.SUCCESS, data, null); }
    public static <T> LiveDataResult error(T data, String message) { return new LiveDataResult(State.ERROR, data, message); }
}
