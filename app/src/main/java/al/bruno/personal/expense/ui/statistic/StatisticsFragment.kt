package al.bruno.personal.expense.ui.statistic

import al.bruno.personal.expense.R
import al.bruno.personal.expense.databinding.FragmentStatisticsBinding
import al.bruno.personal.expense.entities.ChartDataObject
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

import al.bruno.personal.expense.model.Expense
import al.bruno.personal.expense.observer.Observer
import al.bruno.month.view.Month
import al.bruno.personal.expense.util.Utilities.month
import android.content.Context
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticsFragment : Fragment(), Observer<al.bruno.month.view.Month> {
    private val disposable : CompositeDisposable = CompositeDisposable()
    private var fragmentStatisticsBinding: FragmentStatisticsBinding? = null
    private val calendar = Calendar.getInstance()

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentStatisticsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false)
        disposable.add(ViewModelProviders
                .of(this@StatisticsFragment, mViewModelFactory)[StatisticViewModel::class.java]
                .statistics(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                .delay(50, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    fragmentStatisticsBinding!!.chartData = barData(it, calendar.timeInMillis)
                },{
                    Log.i(StatisticsFragment::class.java.name, it.message)
                }))
        return fragmentStatisticsBinding?.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }


    override fun update(t: al.bruno.month.view.Month) {
        disposable.add(ViewModelProviders
                .of(this@StatisticsFragment, mViewModelFactory)[StatisticViewModel::class.java]
                .statistics(month(t.calendar().get(Calendar.MONTH)), t.calendar().get(Calendar.YEAR).toString())
                .delay(50, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    fragmentStatisticsBinding!!.chartData = barData(it, t.calendar().timeInMillis)
                },{
                    Log.i(StatisticsFragment::class.java.name, it.message)
                }))
    }

    private fun barData(expenses: List<Expense>, date: Long): ChartDataObject<IndexAxisValueFormatter, BarData> {
        val barEntryList = ArrayList<BarEntry>()
        val xAxis = ArrayList<String>()
        for (i in expenses.indices) {
            barEntryList.add(BarEntry(i.toFloat(), expenses[i].amount.toFloat()))
            xAxis.add(expenses[i].category!!)
        }
        val barDataSet = BarDataSet(barEntryList, month(date))
        barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(barDataSet)
        return ChartDataObject(IndexAxisValueFormatter(xAxis), BarData(dataSets))
    }

}
