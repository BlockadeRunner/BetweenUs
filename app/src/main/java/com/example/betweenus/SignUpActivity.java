//////////////////////////////////////////////////////
//           SignUpActivity: Sign Up Page           //
//                                                  //
// Author/Developer: Alex Longo                     //
//                                                  //
// Description: This activity handles user input    //
// for the creation of a user account needed to     //
// to use this app. It works with PasswordManager   //
// class to securely save user credentials and      //
// provides error handling to ensure that users     //
// cannot interfere with existing accounts.         //
//////////////////////////////////////////////////////
package com.example.betweenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SignUpActivity extends AppCompatActivity
{
    // Create a variable to hold the application's instance of the Password Manager
    private PasswordManager passwordManager;

    // Perpare a variable to access SQLite database
    private DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Link the text input fields to variables
        TextView new_username = (TextView) findViewById(R.id.new_username);
        TextView new_password = (TextView) findViewById(R.id.new_password);

        // Setup access to the sqlite database
        DB = new DBHelper(this);

        // Link the create account button to a variable
        MaterialButton new_accnt_btn = (MaterialButton) findViewById(R.id.new_accnt_btn);

        // Link back button to a variable
        MaterialButton back_btn = (MaterialButton) findViewById(R.id.back_btn);

        // Link the application's PasswordManager instance to a variable
        passwordManager = PasswordManager.getInstance(getApplicationContext());

        // Handle when user presses the sign up button
        new_accnt_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Check that neither Username nor Password field is left blank
                if(new_username.getText().toString().equals("") || new_username.getText().equals(null) ||
                   new_username.getText() == null || new_password.getText().toString().equals("") ||
                   new_password.getText().equals(null) || new_password.getText() == null)
                {
                    // If left blank, tell user to provide credentials
                    Toast.makeText(SignUpActivity.this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Try to Save New Credentials
                    try
                    {
                        // Grab new username and password
                        String userId = new_username.getText().toString();
                        String password = new_password.getText().toString();

                        // Check that credentials are not already in use
                        boolean success = passwordManager.savePassword(userId, password);

                        // Credentials not in use, successful account creation
                        if (success)
                        {
                            // Create a new user object
                            User user = new User(0, userId, "");

                            // Add the user to the database
                            DB.addUser(user);

                            Toast.makeText(SignUpActivity.this, "SIGNUP SUCCESSFUL", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        }
                        // Username Already Taken
                        else
                        {
                            Toast.makeText(SignUpActivity.this, "Username Already In Use, Please Select A Different Username", Toast.LENGTH_LONG).show();
                        }

                    }
                    // Catch and handle errors
                    catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                           IOException | NoSuchPaddingException | InvalidKeyException |
                           InvalidAlgorithmParameterException | NoSuchProviderException |
                           UnrecoverableKeyException | IllegalBlockSizeException | BadPaddingException e)
                    {
                        // Show an error message to the user
                        Toast.makeText(SignUpActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }   // end else (fields left blank)
            }   // end onClick
        });   // end onCLickListener

        // Handle when "back" button is pressed
        back_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Switch to Sign Up activity
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });   // end onClick

    }   // end onCreate
}   // end class SignUpActivity