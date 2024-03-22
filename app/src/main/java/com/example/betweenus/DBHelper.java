//////////////////////////////////////////////////////
//               Database Helper Class              //
//                                                  //
// Co-Developers: Rushika Devineni and Alex Longo   //
//                                                  //
// Description: This class handles creating and     //
// and updating the app's internal database for     //
// storing User location data and accessing the     //
// User's list of friends. The internal database    //
// stores User objects (see User class) and this    //
// class provides methods for accessing and         //
// manipulating stored User objects.                //
//////////////////////////////////////////////////////
package com.example.betweenus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    // Declare Variables for database initialization
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_LOCATION = "location";


    // Constructor
    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTableQuery = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_LOCATION + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Add a new user to the database
    public void addUser(User user)
    {
        // user already exists error handling
        User test = getUserByUsername(user.getUsername());
        if(test.getUsername().equals("empty971246756194765")) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_LOCATION, user.getLocation());
            db.insert(TABLE_USERS, null, values);
            db.close();
        }
    }

    // Retrieve a user from the database by username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_LOCATION},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            cursor.close();
            db.close();
            return user;
        } else
        {
            // No user found with the specified username
            User empty = new User(99, "empty971246756194765", "");
            return empty;
        }
    }

    // Update user information based on username
    public void updateUserByUsername(String username, User newUser)
    {
        // check if user exists
        User test = getUserByUsername(username);
        if(!test.getUsername().equals("empty971246756194765"))
        {
            // User exists, update user info
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, newUser.getUsername());
            values.put(COLUMN_LOCATION, newUser.getLocation());
            db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
            db.close();
        }
        // User does not exist, add new user
        else
        {
            addUser(newUser);
        }
    }

    // Get all users from the database
    public List<User> getAllUsers()
    {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_LOCATION},
                null, null, null, null, null);

        // Loop through the cursor and add users to the list
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                User user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                userList.add(user);
            }
            cursor.close();
        }
        db.close();
        return userList;
    }
}   // end DBHelper Class

