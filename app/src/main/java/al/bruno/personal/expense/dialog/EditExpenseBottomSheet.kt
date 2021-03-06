package al.bruno.personal.expense.dialog

import al.bruno.personal.expense.R
import al.bruno.personal.expense.callback.OnClick
import al.bruno.personal.expense.callback.OnClickListener
import al.bruno.personal.expense.callback.OnEditListener
import al.bruno.personal.expense.databinding.BottomSheetExpenseBinding
import al.bruno.personal.expense.model.Expense
import al.bruno.personal.expense.util.Utilities
import al.bruno.personal.expense.view.model.ExpenseViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime

class EditExpenseBottomSheet : BottomSheetDialogFragment() {
    private val disposable : CompositeDisposable = CompositeDisposable()
    var expense: Expense? = null
    companion object {
        class Builder {
            var expense: Expense? = null
            fun setExpense(expense: Expense): Builder {
                this.expense = expense
                return this
            }
            fun build() : EditExpenseBottomSheet {
                return newInstance(expense!!)
            }
        }

        private fun newInstance(expense: Expense): EditExpenseBottomSheet {
            val expenseBottomSheet = EditExpenseBottomSheet()
            val bundle = Bundle()
            bundle.putParcelable("EXPENSE", expense)
            expenseBottomSheet.arguments = bundle
            return expenseBottomSheet
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentExpenseBinding : BottomSheetExpenseBinding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_expense, container, false)
        expense = arguments!!.getParcelable("EXPENSE")
        fragmentExpenseBinding.expense = expense
        fragmentExpenseBinding.onClickListener = object : OnClickListener<Expense> {
            override fun onClick(t: Expense) {
                disposable.add(ViewModelProviders.of(activity!!)
                        .get(ExpenseViewModel::class.java)
                        .insert(t)
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            if (it != -1.toLong()) {
                                Log.i(EditExpenseBottomSheet::class.java.name, "Success")
                                dismiss()
                                //Toast.makeText(activity, R.string.success, Toast.LENGTH_SHORT).show()
                            } else
                                Log.i(EditExpenseBottomSheet::class.java.name, "Fail")
                            //Toast.makeText(activity, R.string.fail, Toast.LENGTH_SHORT).show()
                        }, {
                            Log.i(EditExpenseBottomSheet::class.java.name, it.message)
                            //Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                        }))
            }
        }
        fragmentExpenseBinding.onDismiss = object : OnClick {
            override fun onClick() {
                dismiss()
            }
        }
        fragmentExpenseBinding.onDate = object : OnClick {
            override fun onClick() {
                DatePickerDialog()
                        .onEditListener(object : OnEditListener<Long> {
                            override fun onEdit(t: Long) {
                                expense?.date = DateTime(t).withTime(1,0,0,0)
                            }
                        }).show(fragmentManager, "DATE_PICKER")
            }
        }
        return fragmentExpenseBinding.root
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}
