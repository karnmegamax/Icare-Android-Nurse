package com.consultantvendor.data.models.responses

import java.io.Serializable

class Profile :Serializable{

    var id: Int? = null
    var avatar: Any? = null
    var title:String?=null
    var dob: String? = null
    var working_since: String? = null
    var qualification: Any? = null
    var country: Any? = null
    var rating: Any? = null
    var bio: String? = null
    var user_id: Int? = null
    var created_at: String? = null
    var updated_at: String? = null

    var experience: String? = null
    var call_price: String? = null
    var chat_price: String? = null

    var address: String? = null
    var city: String? = null
    var state: String? = null

    var country_id: String? = null
    var state_id: String? = null
    var city_id: String? = null
    var location_name: String? = null
    var long: String? = null
    var lat: String? = null

}