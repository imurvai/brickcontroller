package com.scn.common;

import android.support.annotation.NonNull;

/**
 * Created by imurvai on 2017-11-29.
 */

public final class StateChange<T> {

    //
    // Private members
    //

    private T previousState;
    private T currentState;
    private boolean isError;
    private Object data = null;

    //
    // Constructor
    //

    public StateChange(@NonNull T previousState, @NonNull T currentState, boolean isError) {
        this.previousState = previousState;
        this.currentState = currentState;
        this.isError = isError;
    }

    public StateChange(@NonNull T previousState, @NonNull T currentState, boolean isError, Object data) {
        this(previousState, currentState, isError);
        this.data = data;
    }

    //
    // API
    //

    public T getPreviousState() { return previousState; }
    public T getCurrentState() { return currentState; }
    public boolean isError() { return isError; }
    public Object getData() { return data; }

    public void resetPreviousState() {
        previousState = currentState;
        isError = false;
    }
}
