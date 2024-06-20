package com.bangkit.naraspeak.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.naraspeak.data.model.CorrectionModel
import com.bangkit.naraspeak.databinding.CardMistakesBinding

class ResultAdapter(private val list: List<CorrectionModel>) :
    RecyclerView.Adapter<ResultAdapter.ViewHolder>() {
    class ViewHolder(private val binding: CardMistakesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CorrectionModel) {
            binding.correct.text = data.correction
            binding.wrong.text = data.original
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CardMistakesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}