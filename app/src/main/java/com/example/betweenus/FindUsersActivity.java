//////////////////////////////////////////////////////
//      FindUsersActivity: Search Friends Page      //
//                                                  //
// Co-Developers: Daniel Han and Alex Longo         //
//                                                  //
// Description: This activity handles accessing the //
// app's internal database and allowing the user to //
// search for friends' usernames to use for this    //
// app's midpoint feature. It also displays the     //
// user's recently searched friends and contains an //
// additional logout button to allow the user to    //
// switch accounts.                                 //
//////////////////////////////////////////////////////
package com.example.betweenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FindUsersActivity extends AppCompatActivity
{
    // Setup class variables for storage and access
    TextView recentFriendsList;
    DBHelper DB;
    Intent intent;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // link and initialize all variables
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);
        intent = getIntent();
        currentUser = intent.getStringExtra("CURRENT_USER");
        final SearchView searchView = findViewById(R.id.searchView);
        Button goButton = findViewById(R.id.go_button);
        MaterialButton logout_btn = (MaterialButton) findViewById(R.id.logout_find_friends_btn);
        recentFriendsList = findViewById(R.id.recentList);
        DB = new DBHelper(this);

        // update list of friends
        updateRecentFriends();

        goButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Grab searched username
                String query = searchView.getQuery().toString().trim();

                // Check that it is a Valid friend's username
                boolean acceptable_user = checkForFriend(query);
                if (acceptable_user)
                {
                    // Valid username, navigate to maps activity
                    Intent intent = new Intent(FindUsersActivity.this, MapsActivity.class);
                    intent.putExtra("CURRENT_USER", currentUser);
                    intent.putExtra("OTHER_USER", query);
                    startActivity(intent);
                }
                else if(!query.isEmpty())
                {
                    // Prompt user to enter valid username
                    Toast.makeText(FindUsersActivity.this, "Invalid user. Please try again.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Display a warning if the search query is empty
                    Toast.makeText(FindUsersActivity.this, "Please find a friend first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle when "logout" button is pressed
        logout_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Switch to Sign Up activity
                Intent intent = new Intent(FindUsersActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });   // end onClick
    }

    // Check if username provided is that of a valid friend
    private boolean checkForFriend(String enteredUsername)
    {
        // Prepare return variable
        boolean found = false;

        // Grab all users from database
        List<User> userList = DB.getAllUsers();

        // Cycle through database users
        for (User user : userList)
        {
            // Check if usernames match
            if(user.getUsername().equals(enteredUsername) && !enteredUsername.equals(currentUser))
            {
                // User found
                found = true;
            }
        }
        return found;
    }

    // Update Friends List
    private void updateRecentFriends()
    {
        // Grab all users from database
        List<User> userList = DB.getAllUsers();

        // Cycle through database users
        for (User user : userList)
        {
            // Add all users except for current one
            if(!user.getUsername().equals(currentUser))
            {
                // Add each user
                String oldList = recentFriendsList.getText().toString();
                String newList = oldList + user.getUsername() + " \n";
                recentFriendsList.setText(newList);
            }
        }
    }
}