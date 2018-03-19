package com.layer.xdk.ui.fourpartitem;

import android.databinding.Bindable;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.query.Queryable;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.viewmodel.ItemViewModel;

import java.util.Set;

public abstract class FourPartItemViewModel<ITEM extends Queryable> extends ItemViewModel<ITEM> {

    public FourPartItemViewModel(IdentityFormatter identityFormatter) {
        super(identityFormatter);
    }

    @Bindable
    public abstract String getTitle();

    @Bindable
    public abstract String getSubtitle();

    @Bindable
    public abstract String getAccessoryText();

    @Bindable
    public abstract boolean isSecondaryState();

    @Bindable
    public abstract Set<Identity> getIdentities();
}
