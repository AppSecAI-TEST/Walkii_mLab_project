package com.example.alonsiwek.demomap;

/**
 * Created by dor on 1/10/2017.
 * This class is for the main screen:
 * 1) control the fragment :
 *      All we do is define each screen as a Fragment.
 *      Rather than creating one activity per screen and defining the transition
 *      animations, the FragmentActivity class will handle all of the work for us.
 * 2) swipes
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MainScreen extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
    }

    // Adapter for the view pager
    public static class PageAdapter extends FragmentActivity {

        /*
        * Identifier for the first fragment
        * Main screen
        */
        public static final int FRAGMENT_ONE_MAINSCREEN = 0;

        /*
        * Identifier for the second fragment.
        * Map
        */
        public static final int FRAGMENT_TWO_MAP = 1;

        /*
        * Identifier for the third fragment.
        * contact
        */
        public static final int FRAGMENT_THREE_SUMMARY = 2;

        /*
        * Identifier for the second fragment.
        * SPAIR
        */
        public static final int FRAGMENT_FOUR = 3;

        /******************************************************
         * add fragment to total for each screen that created *
        ******************************************************/

        // The number of Total Pages in the app
        // Note: NOT INCREMENT if there is no page added
        private static int NUM_PAGES_FRAGMENTS = 3;

        /*
         * The pager view (widget) - which handles animation and allows swiping horizontally
         * to access to access previous and next steps.
         *
         * The ViewPager that hosts the section contents
          */
        private ViewPager viewPager;

        /**
         * The pager adapter, which provides the pages to the view pager view (widget).
         */
        private PageAdapter mPagerAdapter;

        /**
         * The adapter definition of the fragments.
         */
        private FragmentPagerAdapter _fragmentPagerAdapter;

        /**
         * List of fragments.
         */
        private List<Fragment> listFragments = new ArrayList<Fragment>();


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_screen);

            // Create fragments.
            listFragments.add(FRAGMENT_ONE_MAINSCREEN, new MainPageFrag());
            listFragments.add(FRAGMENT_TWO_MAP, new MapActivityFrag());
            listFragments.add(FRAGMENT_THREE_SUMMARY, new Summary());

            /*
             * Setup the fragments, defining the number of fragments, the screens and title
             */
            _fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

                @Override
                public Fragment getItem(final int position) {

                    return listFragments.get(position);
                }

                @Override
                public int getCount() {
                    return NUM_PAGES_FRAGMENTS;
                }

                @Override
                public CharSequence getPageTitle(final int position){
                    switch (position) {
                        case FRAGMENT_ONE_MAINSCREEN:
                            return "Main Screen";
                        case FRAGMENT_TWO_MAP:
                            return "Map";
                        case FRAGMENT_THREE_SUMMARY:
                            return "Summary";
                        default:
                            return null;
                    }
                }
            };
            viewPager = (ViewPager) findViewById(R.id.page1_main);
            viewPager.setAdapter(_fragmentPagerAdapter);

        }

        /**
         * Set to the chosen fragment
         * @param item -  Item index to select
         * @param smoothScroll - True to smoothly scroll to the new item,
         *                       false to transition immediately
         */
        public void setCurrentItem (int item, boolean smoothScroll) {
            viewPager.setCurrentItem(item, smoothScroll);
        }
    }
}



