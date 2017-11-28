package com.scn.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Created by steve on 2017. 11. 13..
 */

@Singleton
public class BrickControllerViewModelFactory implements ViewModelProvider.Factory {

    //
    // Members
    //

    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> creators;

    //
    // Constructor
    //

    @Inject
    public BrickControllerViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> creators) {
        this.creators = creators;
    }

    //
    // ViewModelProvider.Factory overrides
    //

    @Override
    public <T extends ViewModel> T create(Class<T> viewModelClass) {
        Provider<? extends ViewModel> creator = creators.get(viewModelClass);
        if (creator == null) {
            for (Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> entry : creators.entrySet()) {
                if (viewModelClass.isAssignableFrom(entry.getKey())) {
                    creator = entry.getValue();
                    break;
                }
            }
        }
        if (creator == null) {
            throw new IllegalArgumentException("Can't create viewmodel for " + viewModelClass);
        }

        try {
            return (T)creator.get();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
