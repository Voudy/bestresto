package com.bestresto.Dish;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bestresto.Database.DatabaseContract;
import com.bestresto.Database.DatabaseWork;
import com.bestresto.Database.QueryConditions;
import com.bestresto.FilterListener;
import com.bestresto.PagerActivity;
import com.bestresto.R;
import com.bestresto.Types.KitchenTypesManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by egor on 31.05.17.
 * make adapter of all dishes
 */

public class DishesFragment extends Fragment
                            implements FilterListener{

    FloatingActionButton fab;
    ListView lv;
    Fragment currentFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dish_fragment_all, container, false);
        currentFragment = this;
        lv = (ListView) view.findViewById(R.id.listDishes);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(mLastFirstVisibleItem<firstVisibleItem)
                {
                    fab.setVisibility(View.GONE);
                }
                if(mLastFirstVisibleItem>firstVisibleItem)
                {
                    fab.setVisibility(View.VISIBLE);
                }
                mLastFirstVisibleItem=firstVisibleItem;

            }
        });

        SetNewData(null, view.getContext());


        final FragmentTransaction fTrans = getFragmentManager().beginTransaction();
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DishFilterDialog dishFilterDialog = new DishFilterDialog();
                dishFilterDialog.AttachFragment(currentFragment, view.getContext());
                dishFilterDialog.show(getFragmentManager(), "tag");
            }

        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        int position = data.getIntExtra("position", 0);
        lv.smoothScrollToPosition(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.DishFragmentTitle));
    }


    @Override
    public void SetNewData(Bundle bundle, Context context) {
        final QueryConditions queryConditions = new QueryConditions();
        String whenCondition = DatabaseContract.DishesColumns.ACTIVE + " = 1";
        // getting bundle if exist
        if (!(bundle == null)){
            String caption = bundle.getString("dish_caption");
            if (caption != null && !caption.equals("")) {
                whenCondition += " AND " + DatabaseContract.DishesColumns.CAPTION + " = " + DatabaseWork.prepare(caption);
            }
            Integer price = bundle.getInt("dish_price");
            if (price != -1) {
                whenCondition += " AND " + DatabaseContract.DishesColumns.PRICE + " <= " + price;
            }
            ArrayList<String> dish_cuisines = bundle.getStringArrayList("kitchen_params");
            if (dish_cuisines != null && dish_cuisines.size() != 0){
                ArrayList<String> primeList = KitchenTypesManager.getKitchenNumbersByNames(dish_cuisines);
                whenCondition += " AND (";
                for (String prime: primeList) {
                    whenCondition += DatabaseContract.DishesColumns.KITCHEN + " % " + prime + " = 0";
                    if (!prime.equals(primeList.get(primeList.size() - 1))) {
                        whenCondition += " OR ";
                    }
                }
                whenCondition += ")";
            }
        }
        queryConditions.setWhereCondition(whenCondition);
        queryConditions.setOrderByCondition(DatabaseContract.DishesColumns.CREATEDATE + " ASC");
        queryConditions.setColumns(new String[]{
                DatabaseContract.DishesColumns.CAPTION,
                DatabaseContract.DishesColumns.PRICE,
                DatabaseContract.DishesColumns.PICTURE
        });
        queryConditions.setTableName(DatabaseContract.DishesColumns.TABLE_NAME);
        final ArrayList<HashMap<String, Object>> filter_data = DatabaseWork.makeData(queryConditions);
        lv.setAdapter(DishManager.getAdapterWithData(context, queryConditions));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getActivity(), PagerActivity.class);

                i.putExtra(QueryConditions.TAG, queryConditions);
                i.putExtra(DatabaseContract.DishesColumns.INDEXID, position);

                startActivityForResult(i, 1);
            }
        });
    }
}
