package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.Creation;

/**
 * Created by imurvai on 2017-12-17.
 */

@Entity(tableName = "creations")
final class CreationEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "creation_name")
    @NonNull public String creationName;

    public static CreationEntity fromCreation(@NonNull Creation creation) {
        CreationEntity ce = new CreationEntity();
        ce.id = creation.getId();
        ce.creationName = creation.getName();
        return ce;
    }
}
