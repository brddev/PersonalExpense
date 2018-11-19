package al.bruno.financaime.work.manager

import al.bruno.financaime.dependency.injection.BudgetInjection
import al.bruno.financaime.dependency.injection.InjectionProvider
import al.bruno.financaime.model.Budget
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class WorkManagerService(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val calendar = Calendar.getInstance()
    private val disposable = CompositeDisposable()
    override fun doWork(): Result {
        if((
                        calendar[Calendar.MONTH] == 1 ||
                        calendar[Calendar.MONTH] == 2 ||
                        calendar[Calendar.MONTH] == 3 ||
                        calendar[Calendar.MONTH] == 4 ||
                        calendar[Calendar.MONTH] == 5 ||
                        calendar[Calendar.MONTH] == 6 ||
                        calendar[Calendar.MONTH] == 7 ||
                        calendar[Calendar.MONTH] == 8 ||
                        calendar[Calendar.MONTH] == 9 ||
                        calendar[Calendar.MONTH] == 10 ||
                        calendar[Calendar.MONTH] == 11 ||
                        calendar[Calendar.MONTH] == 12) && calendar[Calendar.DAY_OF_MONTH] == 1) {
            disposable.addAll(InjectionProvider.providerSettingsInjection(context)!!.settings(1).subscribeOn(Schedulers.io()).subscribe({
                val budget = Budget()
                budget.budget = it.budget
                budget.incomes = it.incomes
                budget.date = calendar.time
                disposable.add(BudgetInjection.provideBudgetInjection(context)!!.insert(budget).subscribeOn(Schedulers.io()).subscribe({

                }, {

                }))
            }, {

            }))
            return Result.SUCCESS
        } else {
            return Result.FAILURE
        }
    }

    override fun onStopped() {
        disposable.clear()
    }
}