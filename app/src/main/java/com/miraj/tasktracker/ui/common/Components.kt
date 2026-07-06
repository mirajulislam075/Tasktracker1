package com.miraj.tasktracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miraj.tasktracker.data.model.Category
import com.miraj.tasktracker.data.model.Priority
import com.miraj.tasktracker.ui.theme.Amber
import com.miraj.tasktracker.ui.theme.AuxBg
import com.miraj.tasktracker.ui.theme.AuxFg
import com.miraj.tasktracker.ui.theme.InkSoft
import com.miraj.tasktracker.ui.theme.PersonalBg
import com.miraj.tasktracker.ui.theme.PersonalFg
import com.miraj.tasktracker.ui.theme.Rust
import com.miraj.tasktracker.ui.theme.WorkBg
import com.miraj.tasktracker.ui.theme.WorkFg

@Composable
fun CategoryBadge(category: Category, modifier: Modifier = Modifier) {
    val (bg, fg) = when (category) {
        Category.WORK -> WorkBg to WorkFg
        Category.AUXILIARY -> AuxBg to AuxFg
        Category.PERSONAL -> PersonalBg to PersonalFg
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}

@Composable
fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.HIGH -> Rust
        Priority.MEDIUM -> Amber
        Priority.LOW -> InkSoft
    }
    Text(
        text = priority.label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}
