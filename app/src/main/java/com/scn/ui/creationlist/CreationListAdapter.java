package com.scn.ui.creationlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

final class CreationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnCreationClickListener {
        void onClick(Creation creation);
        void onRemoveClick(Creation creation);
    }

    //
    // Private members
    //

    private static final int VIEWTYPE_CREATION = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<Creation> creationList = new ArrayList<>();
    private OnCreationClickListener creationClickListener = null;

    //
    // RecyclerView.Adapter overrides
    //

    @Override
    public int getItemViewType(int position) {
        if (creationList == null || creationList.size() == 0) {
            return VIEWTYPE_DEFAULT;
        }

        return VIEWTYPE_CREATION;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_CREATION: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_creation, parent, false);
                return new CreationItemViewHolder(view);
            }

            case VIEWTYPE_DEFAULT: {
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_creation_list_default, parent, false);
                return new CreationListDefaultViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEWTYPE_CREATION:
                ((CreationItemViewHolder)holder).bind(creationList.get(position), creationClickListener);
                break;

            case VIEWTYPE_DEFAULT:
                ((CreationListDefaultViewHolder)holder).bind();
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (creationList != null && creationList.size() > 0) {
            return creationList.size();
        }

        return 1;
    }

    //
    // API
    //

    public void setCreationList(@NonNull List<Creation> creationList) {
        this.creationList = creationList;
        notifyDataSetChanged();
    }

    public void setCreationClickListener(@NonNull OnCreationClickListener creationClickListener) {
        this.creationClickListener = creationClickListener;
        notifyDataSetChanged();
    }

    //
    // ViewHolders
    //

    public class CreationItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.creation_name) TextView creationName;
        @BindView(R.id.remove_creation) Button removeCreationButton;

        public CreationItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Creation creation, final OnCreationClickListener creationClickListener) {
            creationName.setText(creation.getName());

            itemView.setOnClickListener(view -> {
                if (creationClickListener != null) creationClickListener.onClick(creation);
            });

            removeCreationButton.setOnClickListener(view -> {
                if (creationClickListener != null) creationClickListener.onRemoveClick(creation);
            });
        }
    }

    public class CreationListDefaultViewHolder extends RecyclerView.ViewHolder {

        public CreationListDefaultViewHolder(View itemView) {
            super(itemView);
        }

        public void bind() {
        }
    }
}
