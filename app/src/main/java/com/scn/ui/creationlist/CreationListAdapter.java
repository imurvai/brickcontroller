package com.scn.ui.creationlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scn.creationmanagement.Creation;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-11-28.
 */

final class CreationListAdapter extends RecyclerView.Adapter<CreationListAdapter.CreationListAdapterViewHolder> {

    //
    // Private members
    //

    List<Creation> creationList = new ArrayList<>();

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public CreationListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(CreationListAdapterViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if (creationList != null) {
            return creationList.size();
        }

        return 0;
    }

    //
    // Api
    //

    public void setCreationList(List<Creation> creationList) {
        this.creationList = creationList;
    }

    //
    // ViewHolder
    //

    public class CreationListAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.creation_name) TextView creationName;

        public CreationListAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setView(Creation creation) {
            creationName.setText(creation.getName());
        }
    }
}
