package com.tuya.smart.android.demo.login;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.widget.contact.ContactItemInterface;
import com.tuya.smart.android.demo.base.widget.contact.ContactListAdapter;

import java.util.List;

public class CountryAdpater extends ContactListAdapter {

    public CountryAdpater(Context _context, int _resource,
                          List<ContactItemInterface> _items) {
        super(_context, _resource, _items);
    }

    public void populateDataForRow(View parentView, ContactItemInterface item, int position) {
        TextView fullNameView = parentView.findViewById(R.id.nameView);

        if (item instanceof CountryViewBean) {
            CountryViewBean contactItem = (CountryViewBean) item;
            fullNameView.setText(contactItem.getCountryName());
        }

    }

}
