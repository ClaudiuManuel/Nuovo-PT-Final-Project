package com.example.nuovo_pt.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.nuovo_pt.MainActivity;
import com.example.nuovo_pt.R;
import com.example.nuovo_pt.db.clients.ClientFirebase;
import com.example.nuovo_pt.db.workouts.WorkoutFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeStartingPoint extends Fragment implements View.OnClickListener,ClientRecyclerViewAdapter.ItemClickListener {

    NavController navController = null;
    Button addClientButton;
    RecyclerView recyclerView;
    ClientRecyclerViewAdapter adapter;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceWorkouts;
    List<ClientFirebase> clientList = new ArrayList<>();
    String userEmail;

    public HomeStartingPoint() {
        // Required empty public constructor
    }

    // Declare Context variable at class level in Fragment
    private Context mContext;

    // Initialise it from onAttach()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clientList.clear();
                for(DataSnapshot clientSnapShot : snapshot.getChildren()) {
                    ClientFirebase client = clientSnapShot.getValue(ClientFirebase.class);
                    if(client.getUserEmail().equals(userEmail))
                        clientList.add(client);
                }

                populateClientCardviews(clientList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String removedClient = dataSnapshot.getValue(ClientFirebase.class).getName();
                databaseReferenceWorkouts.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1) {
                        for(DataSnapshot workoutSnapshot : dataSnapshot1.getChildren()) {
                            WorkoutFirebase workoutFirebase = workoutSnapshot.getValue(WorkoutFirebase.class);
                            if(workoutFirebase.getClientsName().equals(removedClient) )
                                databaseReferenceWorkouts.child(workoutFirebase.getWorkoutID()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ...
                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_starting_point, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("Clients");
        databaseReferenceWorkouts = FirebaseDatabase.getInstance().getReference("Workouts");

        recyclerView = view.findViewById(R.id.clients_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.medium_margin);
        recyclerView.addItemDecoration(new GridSpacing(2, spacingInPixels, true, 0));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        addClientButton = view.findViewById(R.id.add_client_button);
        addClientButton.setOnClickListener(this);

    }

    public void populateClientCardviews(List<ClientFirebase> clients) {
        adapter = new ClientRecyclerViewAdapter(mContext,clients);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v != null && v == addClientButton) {
            navController.navigate(R.id.action_homeStartingPoint_to_nav_add_client);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("clientName", adapter.getClient(position).getName());
        navController.navigate(R.id.action_homeStartingPoint_to_clientWorkoutsFragment, bundle);
    }
}