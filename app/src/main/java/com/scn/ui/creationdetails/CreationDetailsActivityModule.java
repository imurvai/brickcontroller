package com.scn.ui.creationdetails;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 2017. 12. 11..
 */

@Module
public class CreationDetailsActivityModule {

    @Provides
    CreationDetailsAdapter provideCreationDetailsAdapter() {
        return new CreationDetailsAdapter();
    }
}
