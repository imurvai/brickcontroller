package com.scn.ui.creationlist;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 2017. 11. 08..
 */

@Module
public class CreationListActivityModule {

    @Provides
    CreationListAdapter provideCreationListAdapter() { return new CreationListAdapter(); }
}
