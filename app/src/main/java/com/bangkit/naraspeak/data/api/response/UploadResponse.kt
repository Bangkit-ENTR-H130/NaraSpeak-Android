package com.bangkit.naraspeak.data.api.response

import com.google.gson.annotations.SerializedName

data class UploadResponse(

	@field:SerializedName("upload_url")
	val uploadUrl: String? = null
)
