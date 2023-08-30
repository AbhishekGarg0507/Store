package com.grocery.store;

import android.view.Display;
import android.widget.Filter;

import com.grocery.store.adapters.AdapterOrderShop;
import com.grocery.store.adapters.AdapterProductSeller;
import com.grocery.store.models.ModelOrderShop;
import com.grocery.store.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

     public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList){
         this.adapter = adapter;
         this.filterList = filterList;
     }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
         FilterResults results = new FilterResults();
         //validate data
        if (charSequence != null && charSequence.length() > 0){
            //search field not empty, searching something , perform search

            //change to uppercase, to make case insensitive
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(charSequence)){
                    // add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }else {
            //search field not empty,not searchong,return original/ all
            results.count = filterList.size();
            results.values = filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
         adapter.orderShopList = (ArrayList<ModelOrderShop>) filterResults.values;
         adapter.notifyDataSetChanged();
    }
}
