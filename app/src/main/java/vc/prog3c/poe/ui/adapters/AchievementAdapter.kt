package vc.prog3c.poe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Achievement
import vc.prog3c.poe.data.models.AchievementCategory
/**
 * @reference Android RecyclerView - Using ViewHolder: https://developer.android.com/develop/ui/views/layout/recyclerview#view-holder
 * @reference Material Components - LinearProgressIndicator: https://m3.material.io/components/progress-indicators/android
 */

class AchievementAdapter(
    private var fullList: List<Achievement>,
    private val onAchievementClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private var filteredList: List<Achievement> = fullList

    fun updateAchievements(newAchievements: List<Achievement>) {
        fullList = newAchievements
        filteredList = newAchievements
        notifyDataSetChanged()
    }

    fun filterByCategory(category: AchievementCategory?) {
        filteredList = if (category == null) {
            fullList
        } else {
            fullList.filter { it.category == category }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount() = filteredList.size

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.achievementIcon)
        private val title: TextView = itemView.findViewById(R.id.achievementTitle)
        private val description: TextView = itemView.findViewById(R.id.achievementDescription)
        private val progressIndicator: LinearProgressIndicator = itemView.findViewById(R.id.progressIndicator)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val boosterBucksReward: TextView = itemView.findViewById(R.id.boosterBucksReward)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAchievementClick(filteredList[position])
                }
            }
        }

        fun bind(achievement: Achievement) {
            title.text = achievement.title
            description.text = achievement.description
            boosterBucksReward.text = achievement.boosterBucksReward.toString()

            val progress = (achievement.progress.toFloat() / achievement.requiredProgress.toFloat() * 100).toInt()
            progressIndicator.progress = progress
            progressText.text = "${achievement.progress}/${achievement.requiredProgress}"

            val context = itemView.context
            icon.setImageResource(
                if (achievement.isCompleted) R.drawable.ic_achievement_completed
                else R.drawable.ic_achievement
            )
            title.setTextColor(
                context.getColor(
                    if (achievement.isCompleted) R.color.primary else R.color.text_primary
                )
            )
            progressIndicator.setIndicatorColor(
                context.getColor(
                    if (achievement.isCompleted) R.color.primary else R.color.progress_background
                )
            )
        }
    }
}
