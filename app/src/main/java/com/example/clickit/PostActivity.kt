package com.example.clickit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickit.models.Posts
import com.example.clickit.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import kotlinx.android.synthetic.main.activity_post.*

private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Posts>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Create the layout file which will represent the one post -- DONE

        // Create the data source -- DONE
        posts = mutableListOf()

        // Create the adapter
        adapter = PostsAdapter(this, posts)

        // Lastly Bind the adapter and the layout manager to the RV which we designed in activity_post
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)
        firestoreDb = FirebaseFirestore.getInstance()

        // in this body of code we are going back into the firestoreDB and look at the user collection
        // the docuement we care about is the user id -- only can access Posts activity if you logged in
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot -> // successful listener if it succeeds then we have the user object in memory
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure fetching signed in user", exception)
            }


        var postsReference = firestoreDb
            .collection("posts") // starts at the root and looks inside posts
            .limit(20) // limits the posts to 20
            .orderBy("creation_time_ms", Query.Direction.DESCENDING) // creation time based activity -- this worked since in logcat the posts order changed

        // updating the conditions on the posts query -- when the profile activity is not null so checking a profile
        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null) {
            supportActionBar?.title = username // if not null then we update the action bar with the name of the person
            postsReference = postsReference.whereEqualTo("user.username", username) // in the database there is something called user subset username
        }


        postsReference.addSnapshotListener { snapshot, exception -> // added snapshot listener firebase will inform if any change has been made
            if (exception != null || snapshot == null) { // checking if snapshot or exception problems if there is then it will be indicated
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Posts::class.java) // snapshot allows to return the updated posts name without writing any new code
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) { // nothing went wrong so now will iterate over the result set
                Log.i(TAG, "Post ${post}") // printing out the log statement -- the ID then the Data map of those attributes
            }

        }

        fabCreate.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username) // the signin user is in the authentication and then based on that in the database we can access their name
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}