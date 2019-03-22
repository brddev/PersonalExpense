package al.bruno.personal.expense.data.source

import al.bruno.personal.expense.data.source.local.dao.ExpenseMasterDao
import al.bruno.personal.expense.model.ExpenseMaster
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseMasterRepository @Inject constructor(private var expenseDataSource: ExpenseMasterDao) : ExpenseMasterDataSource {
    override fun expenseMaster(month: String, year: String): Single<List<ExpenseMaster>> {
        return expenseDataSource.expenseMaster(month, year)
    }
}