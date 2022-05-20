package com.example.clickit.models

import com.google.firebase.firestore.PropertyName

data class Posts (
    var description: String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageURL: String = "",
    @get:PropertyName("creation_time_ms") @set:PropertyName("creation_time_ms") var creationTimeMS: Long = 0, // have to translate from underscore to camel case
    var user: User? = null // owner of the posts
        )