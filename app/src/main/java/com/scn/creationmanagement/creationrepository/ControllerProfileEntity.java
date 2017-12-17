package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.ControllerProfile;

/**
 * Created by imurvai on 2017-12-17.
 */

@Entity(tableName = "controller_profiles",
        foreignKeys = @ForeignKey(
                entity = CreationEntity.class,
                parentColumns = { "id" },
                childColumns = { "creation_id" }
                ))
final class ControllerProfileEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "creation_id")
    @NonNull public long creationId;

    @ColumnInfo(name = "controller_profile_name")
    @NonNull public String controllerProfileName;

    public static ControllerProfileEntity fromControllerProfile(long creationId, @NonNull ControllerProfile controllerProfile) {
        ControllerProfileEntity cpe = new ControllerProfileEntity();
        cpe.creationId = creationId;
        cpe.controllerProfileName = controllerProfile.getName();
        return cpe;
    }
}
