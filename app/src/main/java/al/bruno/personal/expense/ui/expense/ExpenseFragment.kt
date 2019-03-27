package al.bruno.personal.expense.ui.expense

import al.bruno.personal.expense.R
import al.bruno.personal.expense.adapter.EditAdapter
import al.bruno.personal.expense.callback.*
import android.os.Bundle

import al.bruno.personal.expense.databinding.CategoriesSingleItemBinding
import al.bruno.personal.expense.dialog.EditCategoriesDialog
import androidx.fragment.app.Fragment

import al.bruno.personal.expense.model.Categories
import al.bruno.personal.expense.adapter.observer.Subject
import al.bruno.personal.expense.databinding.AddNewItemBinding
import al.bruno.personal.expense.databinding.FragmentExpenseBinding
import al.bruno.personal.expense.dialog.edit.expense.EditExpenseBottomSheet
import al.bruno.personal.expense.model.Expense
import al.bruno.personal.expense.observer.ExpenseObserver
import al.bruno.personal.expense.util.EXPENSES
import al.bruno.personal.expense.util.INCOMES
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import javax.inject.Inject
import kotlin.collections.ArrayList

class ExpenseFragment : Fragment(), OnItemSwipeSelectListener<Categories>, Subject<Categories>, ExpenseObserver<List<Categories>, String> {
    override fun update(t: List<Categories>, l:String) {
        when (l) {
            EXPENSES -> {
                val adapter = EditAdapter(t, R.layout.categories_single_item, expenseItemsBinding, R.layout.add_new_item, addExpenseItemsBinding)
                registerObserver(adapter)
                fragmentExpenseBinding?.customAdapter = adapter
            }
            INCOMES -> {
                val adapter = EditAdapter(t, R.layout.categories_single_item, incomesItemsBinding, R.layout.add_new_item, addIncomesItemBinding)
                registerObserver(adapter)
                fragmentExpenseBinding?.customAdapter = adapter
            }
        }
    }

    //https://medium.com/fueled-engineering/swipe-drag-bind-recyclerview-817408125530
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val registry = ArrayList<al.bruno.personal.expense.adapter.observer.Observer<Categories>>()
    private var fragmentExpenseBinding: FragmentExpenseBinding? = null

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentExpenseBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense, container, false)
        disposable.add(ViewModelProviders
                .of(this, mViewModelFactory)
                [ExpenseViewModel::class.java]
                .categories(EXPENSES)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.i(ExpenseFragment::class.java.name, it.toString())
                    val adapter = EditAdapter(it, R.layout.categories_single_item, expenseItemsBinding, R.layout.add_new_item, addExpenseItemsBinding)
                    registerObserver(adapter)
                    fragmentExpenseBinding?.customAdapter = adapter
                }, {
                    Log.i(ExpenseFragment::class.java.name, it.message)
                    val adapter = EditAdapter(ArrayList(), R.layout.categories_single_item, expenseItemsBinding, R.layout.add_new_item, addExpenseItemsBinding)
                    registerObserver(adapter)
                    fragmentExpenseBinding?.customAdapter = adapter
                }))
        fragmentExpenseBinding?.onItemSwipeSelectListener = this
        return fragmentExpenseBinding?.root
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onItemSwipedLeft(t: Categories) {
        val handler = Handler()
        val runnable = Runnable {
            ViewModelProviders
                    .of(this, mViewModelFactory)[ExpenseViewModel::class.java]
                    .delete(t)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
                    .dispose()
        }
        Snackbar.make(activity!!.findViewById(android.R.id.content), "DELETED", Snackbar.LENGTH_LONG).setAction("UNDO") {
            handler.removeCallbacks(runnable)
            notifyObserverAdd(t)
        }.show();
        notifyObserverRemove(t)
        handler.postDelayed(runnable, 3500)
    }

    override fun onItemSwipedRight(t: Categories) {
        if(TextUtils.equals(EXPENSES, t.type)) {
            EditCategoriesDialog
                    .Builder()
                    .setHint(R.string.expenses)
                    .setTitle(R.string.expense)
                    .setCategories(t)
                    .build()
                    .onCategoriesEditListener(onEditListeners = object : OnEditListeners<Categories> {
                        override fun onEdit(t: Categories) {
                            disposable.add(ViewModelProviders
                                    .of(this@ExpenseFragment, mViewModelFactory)
                                    [ExpenseViewModel::class.java]
                                    .insert(t)
                                    .subscribeOn(Schedulers.io())
                                    .doOnSubscribe {
                                        notifyObserverAdd(t)
                                    }.subscribe())
                        }

                        override fun onDismiss(t: Categories) {
                            notifyObserverChanged(t)
                        }
                    })
                    .show(fragmentManager, ExpenseFragment::class.java.name)
        } else {
            EditCategoriesDialog
                    .Builder()
                    .setHint(R.string.incomes)
                    .setTitle(R.string.incomes)
                    .setCategories(t)
                    .build()
                    .onCategoriesEditListener(onEditListeners = object : OnEditListeners<Categories> {
                        override fun onEdit(t: Categories) {
                            disposable.add(ViewModelProviders
                                    .of(this@ExpenseFragment, mViewModelFactory)
                                    [ExpenseViewModel::class.java]
                                    .insert(t)
                                    .subscribeOn(Schedulers.io())
                                    .doOnSubscribe {
                                        notifyObserverAdd(t)
                                    }.subscribe())
                        }

                        override fun onDismiss(t: Categories) {
                            notifyObserverChanged(t)
                        }
                    })
                    .show(fragmentManager, ExpenseFragment::class.java.name)
        }
    }

    override fun registerObserver(o: al.bruno.personal.expense.adapter.observer.Observer<Categories>) {
        registry.add(o)
    }

    override fun removeObserver(o: al.bruno.personal.expense.adapter.observer.Observer<Categories>) {
        if (registry.indexOf(o) >= 0)
            registry.remove(o)
    }

    override fun notifyObserverRemove(t: Categories) {
        for (observer in registry) {
            observer.remove(t)
        }
    }

    override fun notifyObserverAdd(t: Categories) {
        for (observer in registry) {
            observer.add(t)
        }
    }

    override fun notifyObserverChanged(t: Categories) {
        for (observer in registry) {
            observer.update(t)
        }
    }

    private val incomesItemsBinding = object : BindingData<Categories, CategoriesSingleItemBinding> {
        override fun bindData(t: Categories, vm: CategoriesSingleItemBinding) {
            vm.categories = t
            vm.onItemClickListener = object : OnItemClickListener<Categories> {
                override fun onItemClick(t: Categories) {
                    val expense = Expense();
                    expense.category = t.category
                    expense.type = t.type
                    expense.date = DateTime.now().withTime(7, 0, 0, 0)
                    EditExpenseBottomSheet
                            .Companion
                            .Builder()
                            .setExpense(expense)
                            .build()
                            .show(fragmentManager, "EXPENSE_BOTTON_SHEET")
                }

                override fun onLongItemClick(t: Categories): Boolean {
                    return false
                }
            }
        }
    }

    private val expenseItemsBinding = object : BindingData<Categories, CategoriesSingleItemBinding> {
        override fun bindData(t: Categories, vm: CategoriesSingleItemBinding) {
            vm.categories = t
            vm.onItemClickListener = object : OnItemClickListener<Categories> {
                override fun onItemClick(t: Categories) {
                    val expense = Expense();
                    expense.category = t.category
                    expense.type = t.type
                    expense.date = DateTime.now().withTime(7, 0, 0, 0)
                    EditExpenseBottomSheet
                            .Companion
                            .Builder()
                            .setExpense(expense)
                            .build()
                            .show(fragmentManager, "EXPENSE_BOTTON_SHEET")
                }

                override fun onLongItemClick(t: Categories): Boolean {
                    return false
                }
            }
        }
    }
    private val addIncomesItemBinding = object : BindingData<List<Categories>, AddNewItemBinding> {
        override fun bindData(t: List<Categories>, vm: AddNewItemBinding) {
            vm.item = getString(R.string.incomes)
            vm.onClick = object : OnClick {
                override fun onClick() {
                    EditCategoriesDialog
                            .Builder()
                            .setHint(R.string.incomes)
                            .setTitle(R.string.incomes)
                            .setCategoriesList(t)
                            .build()
                            .onCategoriesEditListener(onEditListeners = object : OnEditListeners<Categories> {
                                override fun onEdit(t: Categories) {
                                    t.type = INCOMES
                                    disposable.add(ViewModelProviders
                                            .of(this@ExpenseFragment, mViewModelFactory)
                                            [ExpenseViewModel::class.java]
                                            .insert(t)
                                            .subscribeOn(Schedulers.io())
                                            .doOnSubscribe {
                                                notifyObserverAdd(t)
                                            }.subscribe())
                                }

                                override fun onDismiss(t: Categories) {
                                    notifyObserverChanged(t)
                                }
                            })
                            .show(fragmentManager, ExpenseFragment::class.java.name)
                }
            }
        }
    }

    private val addExpenseItemsBinding = object : BindingData<List<Categories>, AddNewItemBinding> {
        override fun bindData(t: List<Categories>, vm: AddNewItemBinding) {
            vm.item = getString(R.string.expenses)
            vm.onClick = object : OnClick {
                override fun onClick() {
                    EditCategoriesDialog
                            .Builder()
                            .setHint(R.string.expenses)
                            .setTitle(R.string.expense)
                            .setCategoriesList(t)
                            .build()
                            .onCategoriesEditListener(onEditListeners = object : OnEditListeners<Categories> {
                                override fun onEdit(t: Categories) {
                                    t.type = EXPENSES
                                    disposable.add(ViewModelProviders
                                            .of(this@ExpenseFragment, mViewModelFactory)
                                            [ExpenseViewModel::class.java]
                                            .insert(t)
                                            .subscribeOn(Schedulers.io())
                                            .doOnSubscribe {
                                                notifyObserverAdd(t)
                                            }.subscribe())
                                }

                                override fun onDismiss(t: Categories) {
                                    notifyObserverChanged(t)
                                }
                            })
                            .show(fragmentManager, ExpenseFragment::class.java.name)
                }
            }
        }
    }
}