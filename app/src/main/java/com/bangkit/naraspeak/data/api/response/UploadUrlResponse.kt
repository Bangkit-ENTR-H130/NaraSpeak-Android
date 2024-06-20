package com.bangkit.naraspeak.data.api.response

import com.google.gson.annotations.SerializedName

data class UploadUrlResponse(

	@field:SerializedName("Error")
	val error: Error? = null
)

data class Error(

	@field:SerializedName("Message")
	val message: String? = null,

	@field:SerializedName("RequestId")
	val requestId: String? = null,

	@field:SerializedName("ArgumentValue")
	val argumentValue: String? = null,

	@field:SerializedName("HostId")
	val hostId: String? = null,

	@field:SerializedName("Code")
	val code: String? = null,

	@field:SerializedName("ArgumentName")
	val argumentName: String? = null
)
