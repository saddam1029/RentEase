package com.example.rentease

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        lifecycleScope.launch {
            delay(1000)

            // Check if a user is already logged in
            val currentUser = auth.currentUser

            if (currentUser != null) {
                if (isNetworkAvailable()) {
                    checkUserType(currentUser.uid)
                } else {
                    // No internet connection, proceed with cached data
                    navigateBasedOnCachedData(currentUser.uid)
                }
            } else {
                navigateToLogin()
            }
        }
    }

    private fun checkUserType(userId: String) {
        database.child("Realtor").child(userId).child("userInfo").child("type").get()
            .addOnSuccessListener { providerSnapshot ->
                if (providerSnapshot.exists()) {
                    startActivity(Intent(this@SplashActivity, RealtorActivity::class.java))
                    finish()
                } else {
                    checkClientNode(userId)
                }
            }
            .addOnFailureListener { providerError ->
                handleError("Provider node check failed: ${providerError.message}")
            }
    }

    private fun checkClientNode(userId: String) {
        database.child("Regular User").child(userId).child("userInfo").child("type").get()
            .addOnSuccessListener { clientSnapshot ->
                if (clientSnapshot.exists()) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                } else {
                    handleError("Unknown user type. Redirecting to login.")
                }
            }
            .addOnFailureListener { clientError ->
                handleError("Client node check failed: ${clientError.message}")
            }
    }

    private fun navigateBasedOnCachedData(userId: String) {
        // Check locally cached data for user type
        database.child("Realtor").child(userId).child("userInfo").child("type")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        startActivity(Intent(this@SplashActivity, RealtorActivity::class.java))
                        finish()
                    } else {
                        database.child("Regular User").child(userId).child("userInfo").child("type")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                        finish()
                                    } else {
                                        handleError("Unknown user type. Redirecting to login.")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    handleError("Client node check failed: ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    handleError("Provider node check failed: ${error.message}")
                }
            })
    }

    private fun handleError(message: String) {
        Toast.makeText(this@SplashActivity, message, Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}