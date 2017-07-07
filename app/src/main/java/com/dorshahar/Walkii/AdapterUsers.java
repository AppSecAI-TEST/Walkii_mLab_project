package com.example.alonsiwek.demomap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dor on 24-May-17.
 * Adapter to Recycle View - show the users that use the app
 */

public class AdapterUsers extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    List<UserData> dataOfUsersList = Collections.emptyList();
    UserData current_user;
    int currentPos = 0;


    // create constructor to innitilize context and data sent from MainActivity
    public AdapterUsers(Context context, List<UserData> data){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.dataOfUsersList = data;
    }

    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.users_rec_v_conteiner,parent,false);

        MyHolder holder = new MyHolder(view);

        // clic the row will add me to the walking, without swich fragment
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainPageFrag.activate_GoButton(true);
            }
        });

        return holder;
    }

    //Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder) holder;

        UserData current = dataOfUsersList.get(position);
        myHolder.textUserName.setText(current.user_name);
        myHolder.btn_V.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainPageFrag.activate_GoButton(true);
                Toast t = new Toast(context);
                t.makeText(context,
                        "Your friend is on his way! ",Toast.LENGTH_SHORT).show();
            }
        });
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return dataOfUsersList.size();
    }

    // return the list of users
    public List getList(){
        return this.dataOfUsersList;
    }

    // create constructor to get widget reference
    class MyHolder extends RecyclerView.ViewHolder{

        TextView textUserName;
        ImageButton btn_V;

        //Ctor
        public MyHolder(final View itemView) {

            super(itemView);
            textUserName = (TextView) itemView.findViewById(R.id.user_name_at_contiener);
            btn_V = (ImageButton) itemView.findViewById(R.id.button_at_contiener_V);
        }
    }
}
