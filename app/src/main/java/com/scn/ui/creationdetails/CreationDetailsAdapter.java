package com.scn.ui.creationdetails;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.scn.creationmanagement.ControllerProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imurvai on 2017-12-18.
 */

final class CreationDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //
    // Private members
    //

    private List<ControllerProfile> controllerProfiles = new ArrayList<>();

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    //
    // API
    //

    //
    // ViewHolders
    //
}
