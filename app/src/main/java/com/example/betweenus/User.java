//////////////////////////////////////////////////////
//                    User Class                    //
//                                                  //
// Author/Developer: Alex Longo                     //
//                                                  //
// Description: This class outlines a User object,  //
// which can be stored inside the app's database    //
// and accessed for use in location identification. //
// It provides methods to set and get a 'User'      //
// object's username and location.                  //
//////////////////////////////////////////////////////
package com.example.betweenus;

public class User
{
    // Set private variables for User attributes
    private int id;
    private String username;
    private String location;

    // Constructor
    public User(int id, String username, String location)
    {
        this.id = id;
        this.username = username;
        this.location = location;
    }

    // Getters and setters
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getLocation()
    {
        return location;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }
}   // end User class
