package com.scn.creationmanagement;

/**
 * Created by steve on 2017. 11. 01..
 */

public final class Creation {

    //
    // Members
    //

    private String name;

    //
    // Constructor
    //

    Creation(String name) {
        this.name = name;
    }

    //
    // API
    //

    public String getName() { return name; }
    public void setName(String value) { name = value; }
}
