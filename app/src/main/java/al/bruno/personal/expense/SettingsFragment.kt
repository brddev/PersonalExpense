package al.bruno.personal.expense

import al.bruno.personal.expense.data.source.local.AppDatabase.Companion.getInstance
import al.bruno.personal.expense.dependency.injection.InjectionProvider.providerSettingsInjection
import al.bruno.personal.expense.databinding.FragmentSettingsBinding
import al.bruno.personal.expense.model.Settings
import al.bruno.personal.expense.observer.Observer
import al.bruno.personal.expense.util.ACTION_PROCESS_UPDATES
import al.bruno.personal.expense.util.ViewModelProviderFactory
import al.bruno.personal.expense.view.model.SettingsViewModel
import al.bruno.personal.expense.work.manager.WorkManagerService
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment(), Observer<Settings> {
    private val disposable : CompositeDisposable = CompositeDisposable()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentSettingsBinding: FragmentSettingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        val factory = ViewModelProviderFactory(SettingsViewModel(providerSettingsInjection(getInstance(context!!))!!))
        val settings = Settings(1)
        settings.registerObserver(this)
        disposable.add(ViewModelProviders
                .of(this, factory)[SettingsViewModel::class.java]
                .settings(1)
                .subscribeOn(Schedulers.io())
                .subscribe({ it ->
                    it.registerObserver(this)
                    fragmentSettingsBinding.settings = it
                }, {
                    fragmentSettingsBinding.settings = settings
                }))
        return fragmentSettingsBinding.root;
    }
    override fun update(t: Settings) {
        Log.i(SettingsFragment::javaClass.name, t.toString())
        if(t.auto) {
            WorkManager
                    .getInstance()
                    .enqueueUniquePeriodicWork(ACTION_PROCESS_UPDATES, ExistingPeriodicWorkPolicy.KEEP,
                            PeriodicWorkRequest
                                    .Builder(WorkManagerService::class.java, 24, TimeUnit.HOURS, 3, TimeUnit.HOURS)
                                    .addTag(ACTION_PROCESS_UPDATES)
                                    .build())
            disposable
                    .add(ViewModelProviders
                            .of(this@SettingsFragment)[SettingsViewModel::class.java]
                            .insert(t)
                            .subscribeOn(Schedulers.io()).subscribe({

                            }, {

                            }))
        } else {
            WorkManager.getInstance().cancelAllWork();
        }
    }
    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}
