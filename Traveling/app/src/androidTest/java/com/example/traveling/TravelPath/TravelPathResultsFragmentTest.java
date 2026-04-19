package com.example.traveling.TravelPath;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.traveling.R;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class TravelPathResultsFragmentTest {

    @Test
    public void injectsItemsOnArrival() {
        FakeDataSource dataSource = new FakeDataSource();
        dataSource.enqueueRandomResult(listOf(
                place("Musee Fabre", "Culture"),
                place("Place de la Comedie", "Monument"),
                place("Parc du Peyrou", "Loisir")
        ));

        launchWithDataSource(dataSource);

        assertContainerState(3, "Musee Fabre");
    }

    @Test
    public void refreshButtonReplacesInjectedItems() {
        FakeDataSource dataSource = new FakeDataSource();
        dataSource.enqueueRandomResult(listOf(
                place("Lieu A", "Culture"),
                place("Lieu B", "Monument"),
                place("Lieu C", "Shopping")
        ));
        dataSource.enqueueRandomResult(listOf(
                place("Nouveau Lieu 1", "Restaurant"),
                place("Nouveau Lieu 2", "Evenements")
        ));

        launchWithDataSource(dataSource);
        onView(withId(R.id.travelpath_refresh_button)).perform(click());

        assertContainerState(2, "Nouveau Lieu 1");
    }

    @Test
    public void searchBarFiltersAndInjectsMatchingItems() {
        FakeDataSource dataSource = new FakeDataSource();
        dataSource.enqueueRandomResult(listOf(
                place("Lieu Initial", "Culture"),
                place("Lieu Initial 2", "Monument")
        ));
        dataSource.setSearchResult("musee", listOf(
                place("Musee Fabre", "Culture"),
                place("Musee d'Anatomie", "Culture")
        ));

        launchWithDataSource(dataSource);
        onView(withId(R.id.travelpath_search_view)).perform(setSearchQuery("musee"));

        assertContainerState(2, "Musee Fabre");
    }

    private FragmentScenario<TravelPathResultsFragment> launchWithDataSource(FakeDataSource dataSource) {
        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
                if (className.equals(TravelPathResultsFragment.class.getName())) {
                    TravelPathResultsFragment fragment = new TravelPathResultsFragment();
                    fragment.setMapEnabledForTest(false);
                    fragment.setPlaceDataSourceForTest(dataSource);
                    return fragment;
                }
                return super.instantiate(classLoader, className);
            }
        };

        return FragmentScenario.launchInContainer(
                TravelPathResultsFragment.class,
                null,
                R.style.Theme_Traveling,
                factory
        );
    }

    private static void assertContainerState(int expectedCount, @Nullable String expectedFirstTitle) {
        onView(withId(R.id.travelpath_results_injection_container)).check((view, noViewFoundException) -> {
            LinearLayout container = (LinearLayout) view;
            Assert.assertEquals(expectedCount, container.getChildCount());

            if (expectedCount > 0 && expectedFirstTitle != null) {
                TextView title = container.getChildAt(0).findViewById(R.id.travelpath_result_title);
                Assert.assertEquals(expectedFirstTitle, title.getText().toString());
            }
        });
    }

    private static TravelPathPlace place(String name, String theme) {
        return new TravelPathPlace(name, theme, null);
    }

    @SafeVarargs
    private static <T> List<T> listOf(T... values) {
        List<T> list = new ArrayList<>();
        for (T value : values) {
            list.add(value);
        }
        return list;
    }

    private static ViewAction setSearchQuery(String query) {
        return new ViewAction() {
            @NonNull
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(SearchView.class);
            }

            @NonNull
            @Override
            public String getDescription() {
                return "Set query on SearchView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                SearchView searchView = (SearchView) view;
                searchView.setQuery(query, true);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private static final class FakeDataSource implements TravelPathPlaceDataSource {

        private final ArrayDeque<List<TravelPathPlace>> randomResultsQueue = new ArrayDeque<>();
        private final Map<String, List<TravelPathPlace>> searchResults = new HashMap<>();

        void enqueueRandomResult(@NonNull List<TravelPathPlace> places) {
            randomResultsQueue.addLast(new ArrayList<>(places));
        }

        void setSearchResult(@NonNull String query, @NonNull List<TravelPathPlace> places) {
            searchResults.put(query.toLowerCase(), new ArrayList<>(places));
        }

        @Override
        public void loadPlaces(@NonNull TravelPathPlaceRepository.LoadCallback callback) {
            callback.onSuccess(randomResultsQueue.isEmpty() ? new ArrayList<>() : randomResultsQueue.peekFirst());
        }

        @Override
        public void searchPlacesByName(@NonNull String query, @NonNull TravelPathPlaceRepository.LoadCallback callback) {
            List<TravelPathPlace> result = searchResults.get(query.toLowerCase());
            callback.onSuccess(result == null ? new ArrayList<>() : new ArrayList<>(result));
        }

        @Override
        public void loadRandomPlaces(@Nullable String themeFilter, @NonNull TravelPathPlaceRepository.LoadCallback callback) {
            if (randomResultsQueue.isEmpty()) {
                callback.onSuccess(new ArrayList<>());
                return;
            }
            callback.onSuccess(new ArrayList<>(randomResultsQueue.removeFirst()));
        }
    }
}

