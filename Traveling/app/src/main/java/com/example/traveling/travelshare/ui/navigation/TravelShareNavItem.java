package com.example.traveling.travelshare.ui.navigation;

import androidx.fragment.app.Fragment;

public class TravelShareNavItem {

    public interface FragmentCreator {
        Fragment create();
    }

    private final int itemId;
    private final int menuResId;
    private final FragmentCreator fragmentCreator;

    public TravelShareNavItem(int itemId, int menuResId, FragmentCreator fragmentCreator) {
        this.itemId = itemId;
        this.menuResId = menuResId;
        this.fragmentCreator = fragmentCreator;
    }

    public int getItemId() {
        return itemId;
    }

    public int getMenuResId() {
        return menuResId;
    }

    public Fragment createFragment() {
        return fragmentCreator.create();
    }
}
