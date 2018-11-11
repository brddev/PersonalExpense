package al.bruno.financaime.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import al.bruno.financaime.R
import al.bruno.financaime.callback.OnClickListener
import al.bruno.financaime.callback.OnEditListeners
import al.bruno.financaime.databinding.DialogEditBudgetBinding
import al.bruno.financaime.model.Budget
import al.bruno.financaime.util.Utilities.date
import androidx.databinding.DataBindingUtil
import java.util.*

class EditBudgetDialog : DialogFragment() {
    private  var budget = Budget()
    private var onEditListeners: OnEditListeners<Budget>? = null

    fun onDialogEditListeners(onEditListeners: OnEditListeners<Budget>): EditBudgetDialog {
        this.onEditListeners = onEditListeners
        return this
    }

    class Builder {
        private var budget: Budget? = null
        fun setBudget(budget: Budget): EditBudgetDialog.Builder {
            this.budget = budget
            return this
        }

        fun build(): EditBudgetDialog {
            return newInstance(budget)
        }
    }
    companion object {
        private fun newInstance(budget: Budget?): EditBudgetDialog {
            val args = Bundle()
            args.putParcelable("BUDGET", budget);
            val fragment = EditBudgetDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dialogEditBudgetBinding: DialogEditBudgetBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_edit_budget, container, false);
        dialogEditBudgetBinding.budget = arguments?.getParcelable("CATEGORY") ?: budget
        dialogEditBudgetBinding.onCancelClickListener = object : OnClickListener<Budget> {
            override fun onClick(t: Budget) {
                onEditListeners!!.onDismiss(t)
                dismiss()
            }
        }
        dialogEditBudgetBinding.onEditClickListener = object : OnClickListener<Budget> {
            override fun onClick(t: Budget) { val budget = Budget()
                budget.budget = t.amount
                budget.incomes = t.amount
                budget.date = date()
                onEditListeners!!.onEdit(budget)
                dismiss()
            }
        }
        return dialogEditBudgetBinding.root
    }

    /*
    *  R.id.save -> if (!edit!!.text!!.toString().isEmpty()) {
                if (budget == null) {
                    val data = java.lang.Double.parseDouble(edit!!.text!!.toString())
                    val calendar = Calendar.getInstance()
                    val budget = Budget()
                    budget.incomes = data
                    budget.budget = data
                    budget.date = calendar.time
                    dialogCallBack!!.onClickInsert(budget)
                    dismiss()
                } else {
                    val budgetVal = java.lang.Double.parseDouble(edit!!.text!!.toString())
                    /*if (budgetVal <= (budget.getIncomes() - budget.getExpense())) {
                            budget.setBudget(budgetVal);
                            dialogCallBack.onClickUpdate(budget);
                            dismiss();
                        } else {
                            textInputLayout.setError(getString(R.string.out_of_incomes));
                        }*/
                }
            } else {
                textInputLayout!!.error = getString(R.string.add_value)
            }
            R.id.cancel -> dismiss()*/
}
