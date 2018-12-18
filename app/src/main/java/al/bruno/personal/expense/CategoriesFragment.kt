package al.bruno.personal.expense

import al.bruno.personal.expense.adapter.CustomAdapter
import al.bruno.personal.expense.callback.*
import android.os.Bundle

import al.bruno.personal.expense.databinding.CategoriesSingleItemBinding
import al.bruno.personal.expense.databinding.FragmentCategoriesBinding
import al.bruno.personal.expense.dialog.EditCategoriesDialog
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import al.bruno.personal.expense.model.Categories
import al.bruno.personal.expense.adapter.observer.Subject
import al.bruno.personal.expense.view.model.CategoriesViewModel
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlin.collections.ArrayList

class CategoriesFragment : Fragment(), OnEditListeners<Categories>,OnClickListener<MutableList<Any?>>, OnItemSwipeSelectListener<Categories>, Subject<Categories> {
    //https://medium.com/fueled-engineering/swipe-drag-bind-recyclerview-817408125530
    private val disposable : CompositeDisposable  = CompositeDisposable()
    private val registry = ArrayList<al.bruno.personal.expense.adapter.observer.Observer<Categories> >()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentCategoriesBinding = DataBindingUtil.inflate<FragmentCategoriesBinding>(inflater, R.layout.fragment_categories, container, false);
        disposable.add(ViewModelProviders
                .of(this)
                .get(CategoriesViewModel::class.java)
                .categories()
                .subscribeOn(Schedulers.io())
                .subscribe({
                    val adapter = CustomAdapter(it, R.layout.categories_single_item, object : BindingData<Categories, CategoriesSingleItemBinding> {
                        override fun bindData(t: Categories, vm: CategoriesSingleItemBinding) {
                            vm.categories = t
                            vm.onItemClickListener = object : OnItemClickListener<Categories> {
                                override fun onItemClick(t: Categories) {
                                    ExpenseBottomSheet
                                            .Companion
                                            .Builder()
                                            .setCategories(categories = t)
                                            .build()
                                            .show(fragmentManager, "EXPENSE_BOTTON_SHEET")
                                }

                                override fun onLongItemClick(t: Categories): Boolean {
                                    return false
                                }

                            }
                        }
                    })
                    registerObserver(adapter)
                    fragmentCategoriesBinding.customAdapter = adapter
                },{

                }))
        fragmentCategoriesBinding.onClick = this
        fragmentCategoriesBinding.onItemSwipeSelectListener = this
        return fragmentCategoriesBinding.root
    }

    override fun onEdit(t: Categories) {
        disposable.add(ViewModelProviders
                .of(this)
                .get(CategoriesViewModel::class.java)
                .insert(t)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    notifyObserverAdd(t)
                }.subscribe())
    }
    override fun onDismiss(t: Categories) {
        notifyObserverChanged(t)
    }

    override fun onClick(t: MutableList<Any?>) {
        EditCategoriesDialog
                .Builder()
                .setHint(R.string.categories)
                .setTitle(R.string.add_categories)
                .setCategoriesList(t as ArrayList<Categories>)
                .build()
                .onCategoriesEditListener(this)
                .show(fragmentManager, CategoriesFragment::class.java.name)
    }


    override fun onItemSwipedLeft(t: Categories) {
        val handler = Handler()
        val runnable = Runnable {
            //disposable.add()
            ViewModelProviders
                    .of(this)
                    .get(CategoriesViewModel::class.java)
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
        EditCategoriesDialog
                .Builder()
                .setHint(R.string.categories)
                .setTitle(R.string.add_categories)
                .setCategories(t)
                .build()
                .onCategoriesEditListener(this)
                .show(fragmentManager, CategoriesFragment::class.java.name)
    }
    override fun registerObserver(o: al.bruno.personal.expense.adapter.observer.Observer<Categories>) {
        registry.add(o)
    }

    override fun removeObserver(o: al.bruno.personal.expense.adapter.observer.Observer<Categories>) {
        if(registry.indexOf(o) >= 0)
            registry.remove(o);
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
    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}
