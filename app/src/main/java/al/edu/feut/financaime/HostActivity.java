package al.edu.feut.financaime;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import al.edu.feut.financaime.model.Database;

public class HostActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        //google analitics
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.host, new HomeFragment())
                .commit();
        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        new Database(this);

        //Lambda Expression
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if(getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
        });
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
                if (getSupportFragmentManager().findFragmentById(R.id.host) instanceof YoutubeFragment) {
                    navigation.setVisibility(View.GONE);
                }
                else if (getSupportFragmentManager().findFragmentById(R.id.host) instanceof GoogleMapFragment) {
                    navigation.setVisibility(View.GONE);
                }
                else if (getSupportFragmentManager().findFragmentById(R.id.host) instanceof StatisticsFragment) {
                    navigation.setVisibility(View.GONE);
                }
                else if (getSupportFragmentManager().findFragmentById(R.id.host) instanceof SettingsFragment) {
                    navigation.setVisibility(View.GONE);
                }
                else if (getSupportFragmentManager().findFragmentById(R.id.host) instanceof ExpenseCategoriesFragment) {
                    navigation.setVisibility(View.GONE);
                }
            } else {
                navigation.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.youtube:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.host, new YoutubeFragment())
                        .addToBackStack("YOUTUBE_FRAGMENT")
                        .commit();
                break;
            case R.id.map:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.host, new GoogleMapFragment())
                        .addToBackStack("GOOGLE_MAP__FRAGMENT")
                        .commit();
                break;
            case R.id.statistics:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.host, new StatisticsFragment())
                        .addToBackStack("STATISTICS_FRAGMENT")
                        .commit();
                break;
            case R.id.settings:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.host, new SettingsFragment())
                        .addToBackStack("SETTINGS_FRAGMENT")
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.host, new HomeFragment())
                        .commit();
                return true;
            case R.id.budget:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.host, new BudgetFragment())
                        .commit();
                return true;
            case R.id.expense:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.host, new ExpenseFragment())
                        .commit();
                return true;
            case R.id.expense_log:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.host, new ExpenseLogFragment())
                        .commit();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
