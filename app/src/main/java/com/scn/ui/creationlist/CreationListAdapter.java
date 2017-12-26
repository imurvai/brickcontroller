package com.scn.ui.creationlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.scn.creationmanagement.Creation;
import com.scn.ui.DefaultRecyclerViewViewHolder;
import com.scn.ui.OnListItemClickListener;
import com.scn.ui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-11-28.
 */

final class CreationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //
    // Private members
    //

    private static final int VIEWTYPE_CREATION = 1;
    private static final int VIEWTYPE_DEFAULT = 2;

    private List<Creation> creationList = new ArrayList<>();
    private OnListItemClickListener<Creation> listItemClickListener = null;

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
                        .inflate(R.layout.list_item_default, parent, false);
                return new DefaultRecyclerViewViewHolder(view, parent.getContext().getString(R.string.add_creation));
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEWTYPE_CREATION:
                ((CreationItemViewHolder)holder).bind(creationList.get(position), listItemClickListener);
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

    public void setListItemClickListener(@NonNull OnListItemClickListener<Creation> listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
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

        public void bind(Creation creation, final OnListItemClickListener<Creation> listItemClickListener) {
            creationName.setText(creation.getName());

            itemView.setOnClickListener(view -> {
                if (listItemClickListener != null) listItemClickListener.onClick(creation, OnListItemClickListener.ItemClickAction.CLICK, null);
            });

            removeCreationButton.setOnClickListener(view -> {
                if (listItemClickListener != null) listItemClickListener.onClick(creation, OnListItemClickListener.ItemClickAction.REMOVE, null);
            });
        }
    }
}
