package com.consultantvendor.data.models.responses.chat

data class MessageSend(

        val imageUrl: String? = null,
        val message: String? = null,
        val senderId: String? = null,
        val senderName: String? = null,
        val receiverId: String? = null,
        var messageType: String? = null,
        val request_id: String? = null,
        val sentAt: Long? = null

)
