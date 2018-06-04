package al.edu.feut.financaime.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.Date;

import al.edu.feut.financaime.util.Converters;

public class Budget implements Parcelable {
    private long id;
    private double budget;
    private double incomes;
    private Date date;
    private Expense expense;

    public Budget() {
    }

    protected Budget(Parcel in) {
        id = in.readLong();
        budget = in.readDouble();
        incomes = in.readDouble();
        expense = in.readParcelable(Expense.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeDouble(budget);
        dest.writeDouble(incomes);
        dest.writeParcelable(expense, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Budget> CREATOR = new Creator<Budget>() {
        @Override
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        @Override
        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public double getBudget()
    {
        return budget;
    }

    public void setBudget(double budget)
    {
        this.budget = budget;
    }

    public double getIncomes()
    {
        return incomes;
    }

    public void setIncomes(double incomes)
    {
        this.incomes = incomes;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public static abstract class BudgetTable implements BaseColumns{

        public static final String BUDGET_TABLE = "budget";

        public static final String BUDGET = "_budget";
        public static final String INCOMES = "_incomes";
        public static final String DATE = "_date";

        public static String CREATE_BUDGET_TABLE = "CREATE TABLE " + BUDGET_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + BUDGET + " REAL, "
                + INCOMES + " REAL, "
                + DATE + " REAL "
                + ")";

        public static ContentValues contentBudget(Budget budget) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(BUDGET, budget.getBudget());
            contentValues.put(INCOMES, budget.getIncomes());
            contentValues.put(DATE, Converters.dateToTimestamp(budget.getDate()));
            return contentValues;
        }
        public static Budget budgetCursor(Cursor cursor) {
            Budget budget = new Budget();
            budget.setId(cursor.getLong(cursor.getColumnIndex(_ID)));
            budget.setBudget(cursor.getDouble(cursor.getColumnIndex(BUDGET)));
            budget.setIncomes(cursor.getDouble(cursor.getColumnIndex(INCOMES)));
            budget.setDate(Converters.fromTimestamp(cursor.getLong(cursor.getColumnIndex(DATE))));
            return budget;
        }
    }
}
