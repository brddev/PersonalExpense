package al.bruno.personal.expense.ui.home

import al.bruno.personal.expense.ui.details.DetailsFragment
import al.bruno.personal.expense.R
import al.bruno.adapter.BindingData
import al.bruno.personal.expense.callback.OnClick
import al.bruno.personal.expense.databinding.ExpenseMasterSingleItemBinding
import al.bruno.personal.expense.databinding.FragmentHomeBinding
import al.bruno.personal.expense.entities.Chart
import al.bruno.personal.expense.entities.ChartDataObject
import al.bruno.personal.expense.entities.ExpenseChart
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import al.bruno.personal.expense.observer.Observer
import al.bruno.personal.expense.model.ExpenseDetails
import al.bruno.personal.expense.model.ExpenseMaster
import al.bruno.personal.expense.ui.expense.ExpenseFragment
import al.bruno.personal.expense.util.Utilities
import al.bruno.personal.expense.util.Utilities.month
import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class HomeFragment : Fragment(), Observer<al.bruno.month.view.Month> {
    private val disposable : CompositeDisposable = CompositeDisposable()
    private var fragmentHomeBinding: FragmentHomeBinding? = null
    private val calendar = Calendar.getInstance()

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        ViewModelProviders
                .of(this, mViewModelFactory)[HomeViewModel::class.java]
                .budgetDetails(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                .observe(this@HomeFragment, androidx.lifecycle.Observer<ExpenseDetails> {
                    fragmentHomeBinding?.expenseDetails = it
                })
        ViewModelProviders
                .of(this, mViewModelFactory)[HomeViewModel::class.java]
                .chart(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                .observe(this@HomeFragment, androidx.lifecycle.Observer<List<Chart>> {
                    fragmentHomeBinding!!.chartData = chart(it)
                })
        ViewModelProviders
                .of(this, mViewModelFactory)[HomeViewModel::class.java]
                .expenseMaster(month(calendar.get(Calendar.MONTH)), calendar[Calendar.YEAR].toString())
                .observe(this@HomeFragment, androidx.lifecycle.Observer<List<ExpenseMaster>> {
                    fragmentHomeBinding!!.logAdapter = al.bruno.adapter.CustomAdapter(it, R.layout.expense_master_single_item, object : BindingData<ExpenseMaster, ExpenseMasterSingleItemBinding> {
                        override fun bindData(t: ExpenseMaster, vm: ExpenseMasterSingleItemBinding) {
                            vm.master = t
                        }
                    })
                })
        fragmentHomeBinding?.incomesOnClick = object : OnClick {
            override fun onClick() {
                //TODO
            }
        }
        fragmentHomeBinding?.expenseOnClick = object : OnClick {
            override fun onClick() {
                fragmentManager!!
                        .beginTransaction()
                        .replace(R.id.host, DetailsFragment())
                        .addToBackStack("DETAILS_FRAGMENT")
                        .commit()
            }
        }

        fragmentHomeBinding?.addExpenseOnClick = object : OnClick {
            override fun onClick() {
                fragmentManager!!
                        .beginTransaction()
                        .replace(R.id.host, ExpenseFragment())
                        .addToBackStack("EXPENSE_CATEGORIES_FRAGMENT")
                        .commit()
            }
        }

        return fragmentHomeBinding?.root
    }

    override fun update(t: al.bruno.month.view.Month) {
        ViewModelProviders
                .of(this, mViewModelFactory)[HomeViewModel::class.java]
                .budgetDetails(month(t.calendar().get(Calendar.MONTH)), t.calendar()[Calendar.YEAR].toString())
                .observe(this@HomeFragment, androidx.lifecycle.Observer<ExpenseDetails> {
                    if(it != null)
                        fragmentHomeBinding?.expenseDetails = it
                    else
                        fragmentHomeBinding?.expenseDetails = ExpenseDetails()
                })
        ViewModelProviders
                .of(this, mViewModelFactory)[HomeViewModel::class.java]
                .chart(month(calendar.get(Calendar.MONTH)), calendar.get(Calendar.YEAR).toString())
                .observe(this@HomeFragment, androidx.lifecycle.Observer<List<Chart>> {
                    fragmentHomeBinding!!.chartData = chart(it)
                })
        ViewModelProviders
                .of(this, mViewModelFactory)[HomeViewModel::class.java]
                .expenseMaster(month(t.calendar().get(Calendar.MONTH)), t.calendar()[Calendar.YEAR].toString())
                .observe(this@HomeFragment, androidx.lifecycle.Observer<List<ExpenseMaster>> {
                    fragmentHomeBinding!!.logAdapter = al.bruno.adapter.CustomAdapter(it, R.layout.expense_master_single_item, object : BindingData<ExpenseMaster, ExpenseMasterSingleItemBinding> {
                        override fun bindData(t: ExpenseMaster, vm: ExpenseMasterSingleItemBinding) {
                            vm.master = t
                        }
                    })
                })
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
    private fun chart(charts: List<Chart>): ChartDataObject<MutableList<String>, LineData>? {
        val dataSets = ArrayList<ILineDataSet>()
        val dateXaxis = ArrayList<String>()
        for (chart in charts) {
            val entryList = ArrayList<Entry>()
            for (i in 0 until chart.expenses!!.size) {
                val expense = chart.expenses!!.get(i)
                entryList.add(Entry(i.toFloat(), expense.amount))
                dateXaxis.add(Utilities.date(expense.date!!))
            }
            //Sort
            Collections.sort(entryList, EntryXComparator())
            val lineDataSet = LineDataSet(entryList, chart.type)
            lineDataSet.color = Utilities.randomColors()!!
            lineDataSet.lineWidth = 2f
            lineDataSet.circleRadius = 2f
            dataSets.add(lineDataSet)
        }
        return ChartDataObject(dateXaxis, LineData(dataSets))
    }
}
