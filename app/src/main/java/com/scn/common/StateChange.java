package com.scn.common;

/**
 * Created by imurvai on 2017-11-29.
 */

public final class StateChange<T> {

    //
    // Private members
    //

    private final T previousState;
    private final T currentState;
    private boolean isError;

    //
    // Constructor
    //

    public StateChange(T previousState, T currentState, boolean isError) {
        this.previousState = previousState;
        this.currentState = currentState;
        this.isError = isError;
    }

    //
    // API
    //

    public T getPreviousState() { return previousState; }
    public T getCurrentState() { return currentState; }
    public boolean isError() { return isError; }

    public void setErrorHandled() { isError = false; }
}
