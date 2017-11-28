package com.scn.dagger;

import android.app.Application;

import com.scn.app.BrickControllerApplication;
import com.scn.devicemanagement.dagger.DeviceManagementModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

/**
 * Created by steve on 2017. 11. 08..
 */

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        ApplicationModule.class,
        DeviceManagementModule.class,
        ActivityBuilderModule.class,
        ViewModelModule.class
})
public interface ApplicationComponent extends AndroidInjector<BrickControllerApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance Builder application(Application application);
        ApplicationComponent build();
    }

    void inject(BrickControllerApplication application);
}
