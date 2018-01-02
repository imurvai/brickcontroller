package com.scn.creationmanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by imurvai on 2017-12-17.
 */

@Singleton
final class CreationRepository {

    //
    // Members
    //

    private static final String TAG = CreationRepository.class.getSimpleName();

    private final CreationDao creationDao;
    private final List<Creation> creationList = new ArrayList<>();
    private final MutableLiveData<List<Creation>> creationListLiveData = new MutableLiveData<>();
    private boolean isLoaded = false;

    //
    // Constructor
    //

    @Inject
    CreationRepository(@NonNull Context context) {
        Logger.i(TAG, "constructor...");

        CreationDatabase database = Room.databaseBuilder(context, CreationDatabase.class, CreationDatabase.DatabaseName).build();
        creationDao = database.creationDao();

        creationListLiveData.setValue(new ArrayList<>());
    }

    //
    // API
    //

    @WorkerThread
    synchronized void loadCreations(boolean forceLoad) {
        Logger.i(TAG, "loadCreations...");

        if (isLoaded && !forceLoad) {
            Logger.i(TAG, "  Already loaded, not forced.");
        }

        creationList.clear();

        List<Creation> creations = creationDao.getCreations();
        for (Creation creation : creations) {
            List<ControllerProfile> controllerProfiles = creationDao.getControllerProfiles(creation.getId());
            for (ControllerProfile controllerProfile : controllerProfiles) {

                List<ControllerEvent> controllerEvents = creationDao.getControllerEvents(controllerProfile.getId());
                for (ControllerEvent controllerEvent : controllerEvents) {

                    List<ControllerAction> controllerActions = creationDao.getControllerActions(controllerEvent.getId());
                    for (ControllerAction controllerAction : controllerActions) {
                        controllerEvent.addControllerAction(controllerAction);
                    }

                    controllerProfile.addControllerEvent(controllerEvent);
                }

                creation.addControllerProfile(controllerProfile);
            }

            creationList.add(creation);
        }

        isLoaded = true;
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void insertCreation(@NonNull Creation creation) {
        Logger.i(TAG, "saveCreation - " + creation);
        creation.setId(creationDao.insertCreation(creation));
        creationList.add(creation);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void updateCreation(@NonNull Creation creation,
                                     @NonNull String newName) {
        Logger.i(TAG, "updateCreation - " + creation + ", new name: " + newName);

        String originalName = creation.getName();
        try {
            creation.setName(newName);
            creationDao.updateCreation(creation);
        }
        catch (Exception e) {
            Logger.e(TAG, "  Could not update creation - " + creation);
            creation.setName(originalName);
            throw e;
        }

        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void removeCreation(@NonNull Creation creation) {
        Logger.i(TAG, "removeCreation - " + creation);
        creationDao.deleteCreationRecursive(creation.getId());
        creationList.remove(creation);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void insertControllerProfile(@NonNull Creation creation,
                                              @NonNull ControllerProfile controllerProfile) {
        Logger.i(TAG, "insertControllerProfile - " + controllerProfile);
        controllerProfile.setId(creationDao.insertControllerProfile(controllerProfile));
        creation.addControllerProfile(controllerProfile);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void updateControllerProfile(@NonNull ControllerProfile controllerProfile,
                                              @NonNull String newName) {
        Logger.i(TAG, "updateControllerProfile - " + controllerProfile + ", new name: " + newName);

        String originalName = controllerProfile.getName();
        try {
            controllerProfile.setName(newName);
            creationDao.updateControllerProfile(controllerProfile);
        }
        catch (Exception e) {
            Logger.e(TAG, "  Could not update controller profile - " + controllerProfile);
            controllerProfile.setName(originalName);
            throw e;
        }

        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void removeControllerProfile(@NonNull Creation creation,
                                              @NonNull ControllerProfile controllerProfile) {
        Logger.i(TAG, "removeControllerProfile - " + controllerProfile);
        creationDao.deleteControllerProfileRecursive(controllerProfile.getId());
        creation.removeControllerProfile(controllerProfile);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void insertControllerEvent(@NonNull ControllerProfile controllerProfile,
                                            @NonNull ControllerEvent controllerEvent) {
        Logger.i(TAG, "insertControllerEvent - " + controllerEvent);
        controllerEvent.setId(creationDao.insertControllerEvent(controllerEvent));
        controllerProfile.addControllerEvent(controllerEvent);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void removeControllerEvent(@NonNull ControllerProfile controllerProfile,
                                            @NonNull ControllerEvent controllerEvent) {
        Logger.i(TAG, "removeControllerEvent - " + controllerEvent);
        creationDao.deleteControllerEventRecursive(controllerEvent.getId());
        controllerProfile.removeControllerEvent(controllerEvent);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void insertControllerAction(@NonNull ControllerEvent controllerEvent,
                                             @NonNull ControllerAction controllerAction) {
        Logger.i(TAG, "insertControllerAction - " + controllerAction);
        controllerAction.setId(creationDao.insertControllerAction(controllerAction));
        controllerEvent.addControllerAction(controllerAction);
        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void updateControllerAction(@NonNull ControllerAction controllerAction,
                                             @NonNull String deviceId,
                                             int channel,
                                             boolean isRevert,
                                             boolean isToggle,
                                             int maxOutput) {
        Logger.i(TAG, "updateControllerAction - " + controllerAction);

        String originalDeviceId = controllerAction.getDeviceId();
        int originalChannel = controllerAction.getChannel();
        boolean originalIsRevert = controllerAction.getIsRevert();
        boolean originalIsToggle = controllerAction.getIsToggle();
        int originalMaxOutput = controllerAction.getMaxOutput();

        try {
            controllerAction.setDeviceId(deviceId);
            controllerAction.setChannel(channel);
            controllerAction.setIsRevert(isRevert);
            controllerAction.setIsToggle(isToggle);
            controllerAction.setMaxOutput(maxOutput);
            creationDao.updateControllerAction(controllerAction);
        }
        catch (Exception e) {
            Logger.e(TAG, "  Could not update controller action.");
            controllerAction.setDeviceId(originalDeviceId);
            controllerAction.setChannel(originalChannel);
            controllerAction.setIsRevert(originalIsRevert);
            controllerAction.setIsToggle(originalIsToggle);
            controllerAction.setMaxOutput(originalMaxOutput);
        }

        creationListLiveData.postValue(creationList);
    }

    @WorkerThread
    synchronized void removeControllerAction(@NonNull ControllerEvent controllerEvent,
                                             @NonNull ControllerAction controllerAction) {
        Logger.i(TAG, "removeControllerAction - " + controllerAction);
        creationDao.deleteControllerAction(controllerAction.getId());
        controllerEvent.removeControllerAction(controllerAction);
        creationListLiveData.postValue(creationList);
    }

    synchronized Creation getCreation(long creationId) {
        Logger.i(TAG, "getCreation - " + creationId);

        for (Creation creation : creationList) {
            if (creation.getId() == creationId) return creation;
        }

        Logger.w(TAG, "  Could not find creation.");
        return null;
    }

    synchronized Creation getCreation(@NonNull String creationName) {
        Logger.i(TAG, "getCreation - " + creationName);

        for (Creation creation : creationList) {
            if (creation.getName().equals(creationName)) return creation;
        }

        Logger.w(TAG, "  Could not find creation.");
        return null;
    }

    synchronized ControllerProfile getControllerProfile(long controllerProfileId) {
        Logger.i(TAG, "getControllerProfile - " + controllerProfileId);

        for (Creation creation : creationList) {
            for (ControllerProfile controllerProfile : creation.getControllerProfiles()) {
                if (controllerProfile.getId() == controllerProfileId) return controllerProfile;
            }
        }

        Logger.w(TAG, "  Could not find controller profile.");
        return null;
    }

    synchronized ControllerEvent getControllerEvent(long controllerEventId) {
        Logger.i(TAG, "getControllerEvent - " + controllerEventId);

        for (Creation creation : creationList) {
            for (ControllerProfile controllerProfile : creation.getControllerProfiles()) {
                for (ControllerEvent controllerEvent : controllerProfile.getControllerEvents()) {
                    if (controllerEvent.getId() == controllerEventId) return controllerEvent;
                }
            }
        }

        Logger.w(TAG, "  Could not find controller event.");
        return null;
    }

    synchronized ControllerAction getControllerAction(long controllerActionId) {
        Logger.i(TAG, "getControllerAction - " + controllerActionId);

        for (Creation creation : creationList) {
            for (ControllerProfile controllerProfile : creation.getControllerProfiles()) {
                for (ControllerEvent controllerEvent : controllerProfile.getControllerEvents()) {
                    for (ControllerAction controllerAction : controllerEvent.getControllerActions()) {
                        if (controllerAction.getId() == controllerActionId) return controllerAction;
                    }
                }
            }
        }

        Logger.w(TAG, "  Could not find controller action.");
        return null;
    }

    synchronized LiveData<List<Creation>> getCreationListLiveData() {
        Logger.i(TAG, "getCreationListLiveData...");
        return creationListLiveData;
    }
}
