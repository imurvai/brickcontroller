package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.creationmanagement.Creation;
import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imurvai on 2017-12-17.
 */

public final class CreationRepository {

    //
    // Members
    //

    private static final String TAG = CreationRepository.class.getSimpleName();

    private final CreationDao creationDao;
    private final List<Creation> creationList = new ArrayList<>();

    //
    // Constructor
    //

    public CreationRepository(@NonNull Context context) {
        Logger.i(TAG, "constructor...");

        CreationDatabase database = Room.databaseBuilder(context, CreationDatabase.class, CreationDatabase.DatabaseName).build();
        creationDao = database.creationDao();
    }

    //
    // API
    //

    @WorkerThread
    public synchronized void loadCreations() {
        Logger.i(TAG, "loadCreations...");

        creationList.clear();

        List<CreationEntity> creationEntities = creationDao.getCreations();
        for (CreationEntity creationEntity : creationEntities) {
            Creation creation = new Creation(creationEntity.creationName);

            List<ControllerProfileEntity> controllerProfileEntities = creationDao.getControllerProfiles(creationEntity.id);
            for (ControllerProfileEntity controllerProfileEntity : controllerProfileEntities) {
                ControllerProfile controllerProfile = new ControllerProfile(controllerProfileEntity.id, controllerProfileEntity.controllerProfileName);

                List<ControllerEventEntity> controllerEventEntities = creationDao.getControllerEvents(controllerProfileEntity.id);
                for (ControllerEventEntity controllerEventEntity : controllerEventEntities) {
                    ControllerEvent controllerEvent = new ControllerEvent(controllerEventEntity.id, controllerEventEntity.eventType, controllerEventEntity.eventCode);

                    List<ControllerActionEntity> controllerActionEntities = creationDao.getControllerActions(controllerEventEntity.id);
                    for (ControllerActionEntity controllerActionEntity : controllerActionEntities) {
                        ControllerAction controllerAction = new ControllerAction(controllerActionEntity.id, controllerActionEntity.deviceId, controllerActionEntity.channel, controllerActionEntity.isRevert, controllerActionEntity.isToggle, controllerActionEntity.maxOutput);
                        controllerEvent.addControllerAction(controllerAction);
                    }

                    controllerProfile.addControllerEvent(controllerEvent);
                }

                creation.addControllerProfile(controllerProfile);
            }

            creationList.add(creation);
        }
    }

    @WorkerThread
    public synchronized void insertCreation(@NonNull Creation creation) {
        Logger.i(TAG, "saveCreation - " + creation);
        creation.setId(creationDao.insertCreation(CreationEntity.fromCreation(creation)));
        creationList.add(creation);
    }

    @WorkerThread
    public synchronized void updateCreation(@NonNull Creation creation, @NonNull String newName) {
        Logger.i(TAG, "updateCreation - " + creation + ", new name: " + newName);
        CreationEntity ce = CreationEntity.fromCreation(creation);
        ce.creationName = newName;
        creationDao.updateCreation(ce);
        creation.setName(newName);
    }

    @WorkerThread
    public synchronized void removeCreation(@NonNull Creation creation) {
        Logger.i(TAG, "removeCreation - " + creation);
        creationDao.deleteCreationRecursive(creation.getId());
        creationList.remove(creation);
    }

    @WorkerThread
    public synchronized void insertControllerProfile(@NonNull Creation creation, @NonNull ControllerProfile controllerProfile) {
        Logger.i(TAG, "insertControllerProfile - " + controllerProfile);
        controllerProfile.setId(creationDao.insertControllerProfile(ControllerProfileEntity.fromControllerProfile(creation.getId(), controllerProfile)));
        creation.addControllerProfile(controllerProfile);
    }

    @WorkerThread
    public synchronized void updateControllerProfile(@NonNull Creation creation, @NonNull ControllerProfile controllerProfile, @NonNull String newName) {
        Logger.i(TAG, "updateControllerProfile - " + controllerProfile + ", new name: " + newName);
        ControllerProfileEntity cpe = ControllerProfileEntity.fromControllerProfile(creation.getId(), controllerProfile);
        cpe.controllerProfileName = newName;
        creationDao.updateControllerProfile(cpe);
        controllerProfile.setName(newName);
    }

    @WorkerThread
    public synchronized void removeControllerProfile(@NonNull Creation creation, @NonNull ControllerProfile controllerProfile) {
        Logger.i(TAG, "removeControllerProfile - " + controllerProfile);
        creationDao.deleteControllerProfile(controllerProfile.getId());
        creation.removeControllerProfile(controllerProfile);
    }

    @WorkerThread
    public synchronized void insertControllerEvent(@NonNull ControllerProfile controllerProfile, @NonNull ControllerEvent controllerEvent) {
        Logger.i(TAG, "insertControllerEvent - " + controllerEvent);
        controllerEvent.setId(creationDao.insertControllerEvent(ControllerEventEntity.fromControllerEvent(controllerProfile.getId(), controllerEvent)));
    }
}
