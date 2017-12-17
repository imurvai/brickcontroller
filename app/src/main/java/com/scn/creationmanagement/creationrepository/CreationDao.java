package com.scn.creationmanagement.creationrepository;

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
    abstract long insertCreation(@NonNull CreationEntity creationEntity);

    @Query("SELECT * FROM creations")
    abstract List<CreationEntity> getCreations();

    @Update
    abstract void updateCreation(@NonNull CreationEntity creationEntity);

    @Query("DELETE FROM creations WHERE id = :creationId")
    abstract void deleteCreation(long creationId);

    // ControllerProfile

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertControllerProfile(@NonNull ControllerProfileEntity controllerProfileEntity);

    @Query("SELECT * FROM controller_profiles WHERE id = :controllerProfileId")
    abstract ControllerProfileEntity getControllerProfile(long controllerProfileID);

    @Query("SELECT * FROM controller_profiles WHERE creation_id = :creationId")
    abstract List<ControllerProfileEntity> getControllerProfiles(long creationId);

    @Update
    abstract void updateControllerProfile(@NonNull ControllerProfileEntity controllerProfileEntity);

    @Query("DELETE FROM controller_profiles WHERE controller_profile_id = :controllerProfileId")
    abstract void deleteControllerProfile(@NonNull long controllerProfileId);

    @Query("DELETE FROM controller_profiles where creation_id = :creationId")
    abstract void deleteControllerProfiles(long creationId);

    // ControllerEvent

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertControllerEvent(@NonNull ControllerEventEntity controllerEventEntity);

    @Query("SELECT * FROM controller_event WHERE id = :controllerEventId")
    abstract ControllerEventEntity getControllerEvent(long controllerEventId);

    @Query("SELECT * FROM controller_event WHERE controller_profile_id = :controllerProfileId")
    abstract List<ControllerEventEntity> getControllerEvents(long controllerProfileId);

    @Query("DELETE FROM controller_events WHERE controller_event_id = :controllerEventId")
    abstract void deleteControllerEvent(long controllerEventId);

    @Query("DELETE FROM controller_events WHERE controller_profile_id = :controllerProfileId")
    abstract void deleteControllerEvents(long controllerProfileId);

    // ControllerAction

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertControllerAction(@NonNull ControllerActionEntity controllerActionEntity);

    @Query("SELECT * FROM controller_actions WHERE controller_event_id = :controllerEventId")
    abstract List<ControllerActionEntity> getControllerActions(long controllerEventId);

    @Query("DELETE FROM controller_actions WHERE controller_action_id = :controllerActionId")
    abstract void deleteControllerAction(long controllerActionId);

    @Query("DELETE FROM controller_actions WHERE controller_event_id = :controllerEventId")
    abstract void deleteControllerActions(long controllerEventId);

    //
    //
    //

    @Transaction
    void deleteCreationRecursive(long creationId) {
        List<ControllerProfileEntity> controllerProfileEntities = getControllerProfiles(creationId);

        for (ControllerProfileEntity controllerProfileEntity : controllerProfileEntities) {
            List<ControllerEventEntity> controllerEventEntities = getControllerEvents(controllerProfileEntity.id);

            for (ControllerEventEntity controllerEventEntity : controllerEventEntities) {
                deleteControllerActions(controllerEventEntity.id);
            }

            deleteControllerEvents(controllerProfileEntity.id);
        }

        deleteControllerProfiles(creationId);
        deleteCreation(creationId);
    }

    @Transaction
    void deleteControllerProfileRecursive(long controllerProfileId) {
        List<ControllerEventEntity> controllerEventEntities = getControllerEvents(controllerProfileId);

        for (ControllerEventEntity controllerEventEntity : controllerEventEntities) {
            deleteControllerActions(controllerEventEntity.id);
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
