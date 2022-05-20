package com.example.clickit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.clickit.models.Posts
import com.example.clickit.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.grpc.Context
import kotlinx.android.synthetic.main.activity_create.*

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234

class CreateActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        // As soon as creative activity starts we will query for the users inside the signed in user object

        storageReference = FirebaseStorage.getInstance().reference

        firestoreDb = FirebaseFirestore.getInstance()
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

        btnPickImage.setOnClickListener {
            Log.i(TAG, "Open up image picker on device")
            // Caller Part 1ß
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*" // specifying the type of the intent -- open any application and provide an image
//            if (imagePickerIntent.resolveActivity(packageManager) != null) { // checking if there is any application for the image
//                // Caller Part 2
////               startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
//               getResult.launch(imagePickerIntent)
//            }
            getResult.launch(imagePickerIntent)
        }
        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    // Error handling for the submit button and actions for the submit button
    private fun handleSubmitButtonClick() {
        // checking if photo is selected
        if (photoUri == null) {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        // checks if there is a description
        if (etDescription.text.isBlank()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        // this checks if there is a signed in user based on the above query
        if (signedInUser == null) {
            Toast.makeText(this, "No signed in user, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg") // Have to create an unique location for the images separate to others so we do this by using Current Time MS since that is increasing so rapidly it will keep changing rapidly and make it unique
        // Upload photo to Firebase Storage -- Going to use Tasks API from Firebase in order to handle this chain of events where they have to succeed then move on to the next step
        photoReference.putFile(photoUploadUri) // will return to us a task -- which will represent our image upload
            .continueWithTask { photoUploadTask -> // based on this task we will continue -- if successful then continue but if fail Tasks API will propagate it into the next task
                Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                // Retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask{ downloadUrlTask ->
                val post = Posts(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoreDb.collection("posts").add(post)


            }.addOnCompleteListener { postCreationTask -> // end the chain
                btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception during Firebase operations", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish() // the creation flow is transient so when you hit back the content should not be still there
            }

        // Create a post object with the image URL and add that to the posts collection
        // ALl the steps before have to work in order to be able to upload a photo

    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                photoUri = it.data?.data // user has selected an image
                Log.i(TAG, "photoUri $photoUri")
                imageView.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
            }
        }

    // Receiver
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_PHOTO_CODE) { // requestCode passed in is the same as the resultCode from the intent
//            if (resultCode == Activity.RESULT_OK) { // checking what the user actually did in the application that opened up
//                photoUri = data?.data // user has selected an image
//                Log.i(TAG, "photoUri $photoUri")
//                imageView.setImageURI(photoUri)
//
//            } else { // has not selected an image
//                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

}