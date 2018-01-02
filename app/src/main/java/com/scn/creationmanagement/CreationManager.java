package com.scn.creationmanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.logger.Logger;
import com.scn.ui.R;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by steve on 2017. 11. 01..
 */

@Singleton
public final class CreationManager {

    //
    // Constants
    //

    private static final String TAG = CreationManager.class.getSimpleName();

    public enum State {
        OK,
        LOADING,
        INSERTING,
        REMOVING,
        UPDATING
    }

    //
    // Members
    //

    private Context context;
    private CreationRepository creationRepository;
    private MutableLiveData<StateChange<State>> stateChangeLiveData = new MutableLiveData<>();

    //
    // Constructor
    //

    @Inject
    public CreationManager(@NonNull Context context, @NonNull CreationRepository creationRepository) {
        Logger.i(TAG, "constructor...");
        this.context = context;
        this.creationRepository = creationRepository;

        stateChangeLiveData.setValue(new StateChange<>(State.OK, State.OK, false));
    }

    //
    // API
    //

    @MainThread
    public LiveData<StateChange<State>> getStateChangeLiveData() {
        return stateChangeLiveData;
    }

    @MainThread
    public LiveData<List<Creation>> getCreationListLiveData() {
        return creationRepository.getCreationListLiveData();
    }

    @MainThread
    public boolean checkCreationName(String name) {
        Logger.i(TAG, "checkCreationName - " + name);
        return creationRepository.getCreation(name) == null;
    }

    @MainThread
    public Creation getCreation(long creationId) {
        Logger.i(TAG, "getCreation - " + creationId);
        return creationRepository.getCreation(creationId);
    }

    @MainThread
    public Creation getCreation(String creationName) {
        Logger.i(TAG, "getCreation - " + creationName);
        return creationRepository.getCreation(creationName);
    }

    @MainThread
    public ControllerProfile getControllerProfile(long controllerProfileId) {
        Logger.i(TAG, "getControllerProfile - " + controllerProfileId);
        return creationRepository.getControllerProfile(controllerProfileId);
    }

    @MainThread
    public ControllerEvent getControllerEvent(long controllerEventId) {
        Logger.i(TAG, "getControllerEvent - " + controllerEventId);
        return creationRepository.getControllerEvent(controllerEventId);
    }

    @MainThread
    public ControllerAction getControllerAction(long controllerActionId) {
        Logger.i(TAG, "getControllerAction - " + controllerActionId);
        return creationRepository.getControllerAction(controllerActionId);
    }

    @MainThread
    public boolean loadCreationsAsync(final boolean forceLoad) {
        Logger.i(TAG, "loadCreationsAsync...");

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.LOADING, false);

        Single.fromCallable(() -> {
            creationRepository.loadCreations(forceLoad);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Load creations onSuccess...");
                            setState(State.OK, false);
                        },
                        error -> {
                            Logger.e(TAG, "Load creations onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean addCreationAsync(@NonNull final String creationName, boolean addDefaultControllerProfile) {
        Logger.i(TAG, "addCreationAsync - " + creationName);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.INSERTING, false);

        Single.fromCallable(() -> {
            Creation creation = new Creation(0, creationName);
            creationRepository.insertCreation(creation);

            if (addDefaultControllerProfile) {
                String defaultName = context.getString(R.string.default_controller_profile_name);
                ControllerProfile controllerProfile = new ControllerProfile(0, creation.getId(), defaultName);
                creationRepository.insertControllerProfile(creation, controllerProfile);
                creation.addControllerProfile(controllerProfile);
            }

            return creation;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        creation -> {
                            Logger.i(TAG, "Insert creation onSuccess...");
                            setState(State.OK, false, creation.getName());
                        },
                        error -> {
                            Logger.e(TAG, "Insert creation onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean removeCreationAsync(@NonNull final Creation creation) {
        Logger.i(TAG, "removeCreationAsync - " + creation);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            creationRepository.removeCreation(creation);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Remove creation onSuccess...");
                            setState(State.OK, false);
                        },
                        error -> {
                            Logger.e(TAG, "Remove creation onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean updateCreationAsync(@NonNull final Creation creation, @NonNull String newName) {
        Logger.i(TAG, "removeCreationAsync - " + creation);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            creationRepository.updateCreation(creation, newName);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Update creation onSuccess...");
                            setState(State.OK, false, newName);
                        },
                        error -> {
                            Logger.e(TAG, "Update creation onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean addControllerProfileAsync(@NonNull final Creation creation, @NonNull final String controllerProfileName) {
        Logger.i(TAG, "addControllerProfileAsync - " + controllerProfileName);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.INSERTING, false);

        Single.fromCallable(() -> {
            ControllerProfile controllerProfile = new ControllerProfile(0, creation.getId(), controllerProfileName);
            creationRepository.insertControllerProfile(creation, controllerProfile);
            return controllerProfile;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        controllerProfile -> {
                            Logger.i(TAG, "Insert controller profile onSuccess...");
                            setState(State.OK, false, controllerProfile.getId());
                        },
                        error -> {
                            Logger.e(TAG, "Insert controller profile onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean removeControllerProfileAsync(@NonNull final Creation creation, @NonNull final ControllerProfile controllerProfile) {
        Logger.i(TAG, "removeControllerProfileAsync - " + controllerProfile);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            creationRepository.removeControllerProfile(creation, controllerProfile);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Remove controller profile onSuccess...");
                            setState(State.OK, false);
                        },
                        error -> {
                            Logger.e(TAG, "Remove controller profile onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean updateControllerProfileAsync(@NonNull final ControllerProfile controllerProfile, @NonNull String newName) {
        Logger.i(TAG, "updateControllerProfileAsync - " + controllerProfile);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            creationRepository.updateControllerProfile(controllerProfile, newName);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Update controller profile onSuccess...");
                            setState(State.OK, false, newName);
                        },
                        error -> {
                            Logger.e(TAG, "Update controller profile onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean addControllerEventAsync(@NonNull final ControllerProfile controllerProfile, @NonNull final ControllerEvent.ControllerEventType eventType, final int eventCode) {
        Logger.i(TAG, "addControllerEventAsync - " + eventType + ", code: " + eventCode);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.INSERTING, false);

        Single.fromCallable(() -> {
            ControllerEvent controllerEvent = new ControllerEvent(0, controllerProfile.getId(), eventType, eventCode);
            creationRepository.insertControllerEvent(controllerProfile, controllerEvent);
            return controllerEvent;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        controllerEvent -> {
                            Logger.i(TAG, "Insert controller event onSuccess...");
                            setState(State.OK, false, controllerEvent.getId());
                        },
                        error -> {
                            Logger.e(TAG, "Insert controller event onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean removeControllerEventAsync(@NonNull final ControllerProfile controllerProfile, @NonNull final ControllerEvent controllerEvent) {
        Logger.i(TAG, "removeControllerEventAsync - " + controllerEvent);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            creationRepository.removeControllerEvent(controllerProfile, controllerEvent);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Remove controller event onSuccess...");
                            setState(State.OK, false);
                        },
                        error -> {
                            Logger.e(TAG, "Remove controller event onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean addControllerActionAsync(@NonNull final ControllerEvent controllerEvent, @NonNull String deviceId, int channel, boolean isRevert, boolean isToggle, int maxOutput) {
        Logger.i(TAG, "addControllerActionAsync - " + deviceId + ", channel: " + channel);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.INSERTING, false);

        Single.fromCallable(() -> {
            ControllerAction controllerAction = new ControllerAction(0, controllerEvent.getId(), deviceId, channel, isRevert, isToggle, maxOutput);
            creationRepository.insertControllerAction(controllerEvent, controllerAction);
            return controllerAction;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        controllerAction -> {
                            Logger.i(TAG, "Insert controller action onSuccess...");
                            setState(State.OK, false, controllerEvent.getId());
                        },
                        error -> {
                            Logger.e(TAG, "Insert controller action onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean removeControllerActionAsync(@NonNull final ControllerProfile controllerProfile, @NonNull final ControllerEvent controllerEvent, @NonNull final ControllerAction controllerAction, boolean removeEmptyControllerEvent) {
        Logger.i(TAG, "removeControllerActionAsync - " + controllerAction);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.REMOVING, false);

        Single.fromCallable(() -> {
            creationRepository.removeControllerAction(controllerEvent, controllerAction);

            if (removeEmptyControllerEvent && controllerEvent.getControllerActions().size() == 0) {
                creationRepository.removeControllerEvent(controllerProfile, controllerEvent);
            }

            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Remove controller action onSuccess...");
                            setState(State.OK, false);
                        },
                        error -> {
                            Logger.e(TAG, "Remove controller action onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    @MainThread
    public boolean updateControllerActionAsync(@NonNull ControllerAction controllerAction,
                                               @NonNull String deviceId,
                                               int channel,
                                               boolean isRevert,
                                               boolean isToggle,
                                               int maxOutput) {
        Logger.i(TAG, "updateControllerActionAsync - " + controllerAction);

        if (getCurrentState() != State.OK) {
            Logger.w(TAG, "  wrong state - " + getCurrentState().toString());
            return false;
        }

        setState(State.UPDATING, false);

        Single.fromCallable(() -> {
            creationRepository.updateControllerAction(controllerAction, deviceId, channel, isRevert, isToggle, maxOutput);
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        x -> {
                            Logger.i(TAG, "Update controller action onSuccess...");
                            setState(State.OK, false);
                        },
                        error -> {
                            Logger.e(TAG, "Update controller action onError...", error);
                            setState(State.OK, true);
                        });

        return true;
    }

    //
    // Private methods
    //

    @MainThread
    private CreationManager.State getCurrentState() {
        return stateChangeLiveData.getValue().getCurrentState();
    }

    @MainThread
    private void setState(@NonNull CreationManager.State newState, boolean isError) {
        Logger.i(TAG, "setState - " + getCurrentState() + " -> " + newState);
        CreationManager.State currentState = getCurrentState();
        stateChangeLiveData.setValue(new StateChange(currentState, newState, isError));
    }

    @MainThread
    private void setState(@NonNull CreationManager.State newState, boolean isError, Object data) {
        Logger.i(TAG, "setState - " + getCurrentState() + " -> " + newState);
        CreationManager.State currentState = getCurrentState();
        stateChangeLiveData.setValue(new StateChange(currentState, newState, isError, data));
    }
}
