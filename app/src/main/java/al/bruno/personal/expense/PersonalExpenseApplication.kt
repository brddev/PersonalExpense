package al.bruno.personal.expense

import al.bruno.personal.expense.data.source.local.AppDatabase.Companion.getInstance
import al.bruno.personal.expense.dependency.injection.InjectionProvider.providerSettingsInjection
import al.bruno.personal.expense.util.ACTION_PROCESS_UPDATES
import al.bruno.personal.expense.work.manager.WorkManagerService
import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequest
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class PersonalExpenseApplication : Application() {
    private val disposable : CompositeDisposable = CompositeDisposable()
    override fun onCreate() {
        super.onCreate()
        //log
        Log.i(PersonalExpenseApplication::class.java.name, PersonalExpenseApplication::class.java.name)
        //analytics
        FirebaseAnalytics.getInstance(this)
        Fabric.with(this, Crashlytics())

        /*WorkManager
                .getInstance()
                .beginUniqueWork(ACTION_PROCESS_UPDATES, ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.Builder(WorkManagerService::class.java).build())
                .enqueue()*/
        disposable.add(providerSettingsInjection(getInstance(this))!!.settings(1).subscribeOn(Schedulers.io()).subscribe({
            if(it.auto){
                WorkManager
                    .getInstance()
                    .enqueueUniquePeriodicWork(ACTION_PROCESS_UPDATES, ExistingPeriodicWorkPolicy.KEEP,
                            PeriodicWorkRequest
                                    .Builder(WorkManagerService::class.java, 24, TimeUnit.HOURS, 3, TimeUnit.HOURS)
                                    .addTag(ACTION_PROCESS_UPDATES)
                                    .build())
            disposable
                    .add(providerSettingsInjection(getInstance(this))
                            !!.insert(it)
                            .subscribeOn(Schedulers.io()).subscribe({

                            }, {
                                Log.i(PersonalExpenseApplication::class.java.name, it.message)
                            }))
            } else {
                WorkManager.getInstance().cancelAllWork();
            }
        },{
            Log.i(PersonalExpenseApplication::class.java.name, it.message)
        }))
    }
}