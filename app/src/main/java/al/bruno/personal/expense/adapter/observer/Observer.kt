package al.bruno.personal.expense.adapter.observer

interface Observer<T> {
    fun update(l: T);
    fun remove(l: T);
    fun add(l: T)
}