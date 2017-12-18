package com.scn.creationmanagement;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by imurvai on 2017-12-17.
 */

@Dao
abstract class CreationDao {

    // Creation

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertCreation(@NonNull Creation creation);

    @Query("SELECT * FROM creations")
    abstract List<Creation> getCreations();

    @Update
    abstract void updateCreation(@NonNull Creation creation);

    @Query("DELETE FROM creations WHERE id = :id")
    abstract void deleteCreation(long id);

    // ControllerProfile

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertControllerProfile(@NonNull ControllerProfile controllerProfile);

    @Query("SELECT * FROM controller_profiles WHERE id = :controllerProfileId")
    abstract ControllerProfile getControllerProfile(long controllerProfileId);

    @Query("SELECT * FROM controller_profiles WHERE creation_id = :creationId")
    abstract List<ControllerProfile> getControllerProfiles(long creationId);

    @Update
    abstract void updateControllerProfile(@NonNull ControllerProfile controllerProfile);

    @Query("DELETE FROM controller_profiles WHERE id = :id")
    abstract void deleteControllerProfile(@NonNull long id);

    @Query("DELETE FROM controller_profiles where creation_id = :creationId")
    abstract void deleteControllerProfiles(long creationId);

    // ControllerEvent

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertControllerEvent(@NonNull ControllerEvent controllerEvent);

    @Query("SELECT * FROM controller_events WHERE id = :controllerEventId")
    abstract ControllerEvent getControllerEvent(long controllerEventId);

    @Query("SELECT * FROM controller_events WHERE controller_profile_id = :controllerProfileId")
    abstract List<ControllerEvent> getControllerEvents(long controllerProfileId);

    @Query("DELETE FROM controller_events WHERE id = :id")
    abstract void deleteControllerEvent(long id);

    @Query("DELETE FROM controller_events WHERE controller_profile_id = :controllerProfileId")
    abstract void deleteControllerEvents(long controllerProfileId);

    // ControllerAction

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertControllerAction(@NonNull ControllerAction controllerAction);

    @Query("SELECT * FROM controller_actions WHERE controller_event_id = :controllerEventId")
    abstract List<ControllerAction> getControllerActions(long controllerEventId);

    @Query("DELETE FROM controller_actions WHERE id = :id")
    abstract void deleteControllerAction(long id);

    @Query("DELETE FROM controller_actions WHERE controller_event_id = :controllerEventId")
    abstract void deleteControllerActions(long controllerEventId);

    //
    //
    //

    @Transaction
    void deleteCreationRecursive(long creationId) {
        List<ControllerProfile> controllerProfiles = getControllerProfiles(creationId);

        for (ControllerProfile controllerProfile : controllerProfiles) {
            List<ControllerEvent> controllerEvents = getControllerEvents(controllerProfile.getId());

            for (ControllerEvent controllerEventEntity : controllerEvents) {
                deleteControllerActions(controllerEventEntity.getId());
            }

            deleteControllerEvents(controllerProfile.getId());
        }

        deleteControllerProfiles(creationId);
        deleteCreation(creationId);
    }

    @Transaction
    void deleteControllerProfileRecursive(long controllerProfileId) {
        List<ControllerEvent> controllerEvents = getControllerEvents(controllerProfileId);

        for (ControllerEvent controllerEvent : controllerEvents) {
            deleteControllerActions(controllerEvent.getId());
        }

        deleteControllerEvents(controllerProfileId);
        deleteControllerProfile(controllerProfileId);
    }

    @Transaction
    void deleteControllerEventRecursive(long controllerEventId) {
        deleteControllerActions(controllerEventId);
        deleteControllerEvent(controllerEventId);
    }
}
