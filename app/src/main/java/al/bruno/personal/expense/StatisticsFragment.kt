package al.bruno.personal.expense

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

import al.bruno.personal.expense.model.Expense
import al.bruno.personal.expense.util.Utilities
import al.bruno.personal.expense.util.Utilities.month
import al.bruno.personal.expense.util.Utilities.monthFormat
import al.bruno.personal.expense.view.model.ExpenseViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.reactivex.schedulers.Schedulers
import java.util.*

class StatisticsFragment : Fragment() {
    private var calendar = Calendar.getInstance()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewModelProviders.of(this)[ExpenseViewModel::class.java]
                .statistics(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    onChanged(it, view.findViewById(R.id.chart))
                },{

                })
        val date = view.findViewById<AppCompatTextView>(R.id.date)
        date.text = monthFormat(calendar.timeInMillis)
        view.findViewById<View>(R.id.decrement).setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            date.text = monthFormat(calendar.timeInMillis)
            ViewModelProviders.of(this)[ExpenseViewModel::class.java]
                    .statistics(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        onChanged(it, view.findViewById(R.id.chart))
                    },{

                    })
        }
        view.findViewById<View>(R.id.increment).setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            date.text = monthFormat(calendar.timeInMillis)
            ViewModelProviders.of(this)[ExpenseViewModel::class.java]
                    .statistics(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        onChanged(it, view.findViewById(R.id.chart))
                    },{

                    })
        }
    }

    private fun onChanged(expenses: List<Expense>?, barChart: BarChart) {
        if (expenses != null) {
            if (!expenses.isEmpty()) {
                val barData = BarData(barDataSets(expenses))
                barChart.data = barData
                barChart.animateXY(2000, 2000)
                barChart.description.text = getString(R.string.app_name)
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getXAxisValues(expenses))
                barChart.invalidate()
            } else {
                barChart.data = null
                barChart.invalidate()
            }
        }
    }

    private fun barDataSets(expenses: List<Expense>): List<IBarDataSet> {
        val barEntryList = ArrayList<BarEntry>()
        var i = 1
        for (expense in expenses) {
            val barEntry = BarEntry(i.toFloat(), expense.amount.toFloat())
            barEntryList.add(barEntry)
            i++
        }
        val barDataSet = BarDataSet(barEntryList, month(expenses[0].date!!))
        barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(barDataSet)
        return dataSets
    }

    private fun getXAxisValues(expenses: List<Expense>): Array<String?> {
        val xAxis = arrayOfNulls<String>(expenses.size)
        for (i in expenses.indices) {
            xAxis[i] = expenses[i].category
        }
        return xAxis
    }
}
