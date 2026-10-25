package com.swissnotes.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swissnotes.app.ui.theme.SwissColors

/**
 * Note editor. A minimal two-field form with a checklist toggle,
 * following the Swiss principle that typography and whitespace
 * carry the interface.
 */
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SwissColors.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = SwissColors.Black,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = viewModel::onChecklistToggle) {
                    Icon(
                        imageVector = if (uiState.isChecklist) {
                            Icons.Outlined.CheckBox
                        } else {
                            Icons.Outlined.CheckBoxOutlineBlank
                        },
                        contentDescription = "Checklist",
                        tint = if (uiState.isChecklist) SwissColors.Accent else SwissColors.Black,
                    )
                }
                IconButton(onClick = {
                    viewModel.save()
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Save",
                        tint = SwissColors.Black,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title", color = SwissColors.Grey) },
                textStyle = MaterialTheme.typography.headlineMedium,
                shape = RoundedCornerShape(0.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwissColors.Black,
                    unfocusedBorderColor = SwissColors.LightGrey,
                    focusedTextColor = SwissColors.Black,
                    unfocusedTextColor = SwissColors.Black,
                    cursorColor = SwissColors.Accent,
                ),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Write something…", color = SwissColors.Grey) },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(0.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwissColors.LightGrey,
                    unfocusedBorderColor = SwissColors.Transparent,
                    focusedTextColor = SwissColors.Black,
                    unfocusedTextColor = SwissColors.Black,
                    cursorColor = SwissColors.Accent,
                ),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
