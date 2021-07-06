package dto

import com.google.gson.annotations.SerializedName
import generator.NetworkType

data class NetworkDTO(@SerializedName("type") val type: NetworkType, @SerializedName("nodes") val nodes: List<NodeDTO>, @SerializedName("edges") val edges: List<EdgeDTO>) {
}