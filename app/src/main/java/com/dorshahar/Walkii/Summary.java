
package com.example.alonsiwek.demomap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

    /**
     * Created by Dor on 27-Jun-17.
     * Display Summary
     */

    public class Summary extends android.support.v4.app.Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.summary,null,false);

            Button btnBack = (Button) view.findViewById(R.id.back_to_main);
            btnBack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ((MainScreen.PageAdapter)getActivity()).setCurrentItem
                            (MainScreen.PageAdapter.FRAGMENT_ONE_MAINSCREEN , true);
                }
            });



            return view;
        }
    }




