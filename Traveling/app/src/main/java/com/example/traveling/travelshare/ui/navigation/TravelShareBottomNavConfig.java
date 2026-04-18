package com.example.traveling.travelshare.ui.navigation;

import android.view.Menu;
import android.view.MenuInflater;

import com.example.traveling.R;
import com.example.traveling.travelshare.ui.TravelShareAddPostFragment;
import com.example.traveling.travelshare.ui.TravelShareHomeFragment;
import com.example.traveling.travelshare.ui.TravelShareItineraryFragment;
import com.example.traveling.travelshare.ui.TravelShareMessagesFragment;
import com.example.traveling.travelshare.ui.TravelShareSearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public final class TravelShareBottomNavConfig {

    private TravelShareBottomNavConfig() {
    }

    public static List<TravelShareNavItem> buildItems() {
        return Arrays.asList(
                new TravelShareNavItem(
                        R.id.travelshare_nav_home,
                        R.menu.travelshare_nav_item_home,
                        new TravelShareNavItem.FragmentCreator() {
                            @Override
                            public androidx.fragment.app.Fragment create() {
                                return new TravelShareHomeFragment();
                            }
                        }
                ),
                new TravelShareNavItem(
                        R.id.travelshare_nav_search,
                        R.menu.travelshare_nav_item_search,
                        new TravelShareNavItem.FragmentCreator() {
                            @Override
                            public androidx.fragment.app.Fragment create() {
                                return new TravelShareSearchFragment();
                            }
                        }
                ),
                new TravelShareNavItem(
                        R.id.travelshare_nav_messages,
                        R.menu.travelshare_nav_item_messages,
                        new TravelShareNavItem.FragmentCreator() {
                            @Override
                            public androidx.fragment.app.Fragment create() {
                                return new TravelShareMessagesFragment();
                            }
                        }
                ),
                new TravelShareNavItem(
                        R.id.travelshare_nav_add,
                        R.menu.travelshare_nav_item_add,
                        new TravelShareNavItem.FragmentCreator() {
                            @Override
                            public androidx.fragment.app.Fragment create() {
                                return new TravelShareAddPostFragment();
                            }
                        }
                ),
                new TravelShareNavItem(
                        R.id.travelshare_nav_itinerary,
                        R.menu.travelshare_nav_item_itinerary,
                        new TravelShareNavItem.FragmentCreator() {
                            @Override
                            public androidx.fragment.app.Fragment create() {
                                return new TravelShareItineraryFragment();
                            }
                        }
                )
        );
    }

    public static void inflateMenu(BottomNavigationView bottomNavigationView, List<TravelShareNavItem> navItems) {
        Menu menu = bottomNavigationView.getMenu();
        menu.clear();

        MenuInflater menuInflater = new MenuInflater(bottomNavigationView.getContext());
        for (TravelShareNavItem navItem : navItems) {
            menuInflater.inflate(navItem.getMenuResId(), menu);
        }
    }
}
