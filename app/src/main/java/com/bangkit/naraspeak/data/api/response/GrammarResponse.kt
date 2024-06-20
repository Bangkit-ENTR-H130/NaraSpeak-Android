package com.bangkit.naraspeak.data.api.response

import com.google.gson.annotations.SerializedName

data class GrammarResponse(

	@field:SerializedName("deployedModelId")
	val deployedModelId: String? = null,

	@field:SerializedName("modelVersionId")
	val modelVersionId: String? = null,

	@field:SerializedName("model")
	val model: String? = null,

	@field:SerializedName("predictions")
	val predictions: List<String?>? = null,

	@field:SerializedName("modelDisplayName")
	val modelDisplayName: String? = null
)
