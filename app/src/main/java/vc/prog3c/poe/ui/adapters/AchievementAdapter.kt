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
import vc.prog3c.poe.data.models.AchievementDefinitions

class AchievementAdapter(
    private var achievements: List<Achievement>,
    private val onAchievementClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }

    fun filterByCategory(category: AchievementCategory?) {
        achievements = if (category == null) {
            AchievementDefinitions.achievements
        } else {
            AchievementDefinitions.achievements.filter { it.category == category }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount() = achievements.size

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
                    onAchievementClick(achievements[position])
                }
            }
        }

        fun bind(achievement: Achievement) {
            title.text = achievement.title
            description.text = achievement.description
            boosterBucksReward.text = achievement.boosterBucksReward.toString()

            // Set progress
            val progress = (achievement.progress.toFloat() / achievement.requiredProgress.toFloat() * 100).toInt()
            progressIndicator.progress = progress
            progressText.text = "${achievement.progress}/${achievement.requiredProgress}"

            // Update icon based on completion status
            icon.setImageResource(
                if (achievement.isCompleted) R.drawable.ic_achievement_completed
                else R.drawable.ic_achievement
            )

            // Update colors based on completion status
            val context = itemView.context
            if (achievement.isCompleted) {
                title.setTextColor(context.getColor(R.color.primary))
                progressIndicator.setIndicatorColor(context.getColor(R.color.primary))
            } else {
                title.setTextColor(context.getColor(R.color.text_primary))
                progressIndicator.setIndicatorColor(context.getColor(R.color.progress_background))
            }
        }
    }
} 