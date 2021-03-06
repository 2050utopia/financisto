package ru.orangesoftware.financisto2.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.utils.EntityEnum;
import ru.orangesoftware.financisto2.utils.EnumUtils;

@EActivity(R.layout.activity_entity_list)
public class EntityListActivity extends ListActivity {

    final Entity[] entities = Entity.values();

    @ViewById(android.R.id.list)
    protected ListView listView;

    @AfterViews
    protected void afterViews() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ListAdapter adapter = EnumUtils.createEntityEnumAdapter(this, entities);
        listView.setAdapter(adapter);
    }

    @ItemClick(android.R.id.list)
    protected void onItemClick(int position) {
        Entity entity = entities[position];
        entity.startActivity(this);
    }

    @OptionsItem(android.R.id.home)
    public void onHome() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private enum Entity implements EntityEnum {

        CURRENCIES(R.string.currencies, R.drawable.ic_action_currencies){
            @Override
            public void startActivity(Context context) {
                CurrencyListActivity_.intent(context).start();
            }
        },
        EXCHANGE_RATES(R.string.exchange_rates, R.drawable.ic_action_line_chart){
            @Override
            public void startActivity(Context context) {
                ExchangeRatesListActivity_.intent(context).start();
            }
        },
        CATEGORIES(R.string.categories, R.drawable.ic_action_category){
            @Override
            public void startActivity(Context context) {
                CategoryListActivity2_.intent(context).start();
            }
        },
        PAYEES(R.string.payees, R.drawable.ic_action_users){
            @Override
            public void startActivity(Context context) {
                PayeeListActivity_.intent(context).start();
            }
        },
        PROJECTS(R.string.projects, R.drawable.ic_action_gear){
            @Override
            public void startActivity(Context context) {
                ProjectListActivity_.intent(context).start();
            }
        };

        private final int titleId;
        private final int iconId;

        private Entity(int titleId, int iconId) {
            this.titleId = titleId;
            this.iconId = iconId;
        }

        @Override
        public int getTitleId() {
            return titleId;
        }

        @Override
        public int getIconId() {
            return iconId;
        }

        public abstract void startActivity(Context context);

    }

}
