package com.example.zerogrid.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.network.ContactDto
import com.example.zerogrid.network.ContactsRepository
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.ui.theme.*
import com.example.zerogrid.util.ValidationUtils
import com.zerogrid.mesh.app.ui.UserSessionManager

private val DangerRed = Color(0xFFFF3B30)
private val CardBorderColor = Color(0xFF2A2F3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    onNavigate: (Screen) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager.getInstance(context) }
    val repository = remember { ContactsRepository(sessionManager) }
    val viewModel = remember { ContactsViewModel(repository) }

    val contacts by viewModel.contacts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val addState by viewModel.addState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<ContactDto?>(null) }

    // Load contacts on first display
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    // Handle add contact state changes
    LaunchedEffect(addState) {
        when (val state = addState) {
            is AddContactState.Success -> {
                showAddDialog = false
                viewModel.resetAddState()
                snackbarHostState.showSnackbar(
                    "Added ${state.contact.contactUser.displayName} to emergency contacts",
                    duration = SnackbarDuration.Short
                )
            }
            is AddContactState.Error -> {
                // Keep dialog open to show error
            }
            else -> {}
        }
    }

    // Handle delete confirmation dialog
    if (contactToDelete != null) {
        val target = contactToDelete!!
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            containerColor = CardBackground,
            title = {
                Text("Remove Contact", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove ${target.contactUser.displayName} from your emergency contacts?",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteContact(target.id)
                    contactToDelete = null
                }) {
                    Text("Remove", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Add Contact Dialog
    if (showAddDialog) {
        AddContactDialog(
            addState = addState,
            onDismiss = {
                showAddDialog = false
                viewModel.resetAddState()
            },
            onAdd = { emailOrPhone, label ->
                viewModel.addContact(emailOrPhone, label)
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Contacts",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SOS Dispatch List",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = "Add Contact",
                            tint = PrimaryCyan
                        )
                    }
                }
                HorizontalDivider(color = DividerColor)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Info Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryCyan.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Emergency,
                                contentDescription = null,
                                tint = PrimaryCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "When you trigger an online SOS alert, these verified contacts will be notified with your distress signal and coordinates.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRUSTED CONTACTS (${contacts.size})",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New", color = PrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState is ContactsUiState.Loading && contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryCyan, strokeWidth = 2.dp)
                    }
                } else if (contacts.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(SurfaceDarker, CircleShape)
                                .border(1.dp, DividerColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContactPhone,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Contacts Configured",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add family, rescue coordinators, or peers so they can receive emergency broadcasts when you need help.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                        ) {
                            Icon(Icons.Outlined.Add, null, tint = DarkBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Emergency Contact", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(contacts, key = { it.id }) { contact ->
                            ContactItemCard(
                                contact = contact,
                                onDelete = { contactToDelete = contact }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Contact Item Card ───────────────────────────────────────────────────────

@Composable
private fun ContactItemCard(
    contact: ContactDto,
    onDelete: () -> Unit
) {
    val user = contact.contactUser
    val initials = user.displayName.take(2).uppercase().ifEmpty { "EC" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(SurfaceDarker, RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = PrimaryCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = contact.label.uppercase(),
                            color = PrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                if (!user.phoneNumber.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.phoneNumber,
                        color = TextSecondary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Remove Contact",
                    tint = DangerRed.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Add Contact Modal Dialog ───────────────────────────────────────────────

@Composable
private fun AddContactDialog(
    addState: AddContactState,
    onDismiss: () -> Unit,
    onAdd: (emailOrPhone: String, label: String) -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("Family") }
    var customLabel by remember { mutableStateOf("") }

    val presetLabels = listOf("Family", "Spouse", "Doctor", "Friend", "Teammate", "Other")

    val isInputValid = remember(emailOrPhone) {
        emailOrPhone.isBlank() || ValidationUtils.isValidEmailOrPhone(emailOrPhone)
    }

    val isLoading = addState is AddContactState.Loading
    val errorMessage = (addState as? AddContactState.Error)?.message

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text("Add Emergency Contact", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter the email or phone of a registered ZeroGrid user.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email or Phone Number", color = TextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    isError = emailOrPhone.isNotBlank() && !isInputValid,
                    leadingIcon = {
                        Icon(Icons.Outlined.PersonSearch, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDarker,
                        unfocusedContainerColor = SurfaceDarker,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = PrimaryCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (emailOrPhone.isNotBlank() && !isInputValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Must be a valid email or 7-15 digit phone",
                        color = DangerRed,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "RELATIONSHIP LABEL",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Relationship chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLabels.take(3).forEach { label ->
                        FilterChip(
                            selected = selectedLabel == label,
                            onClick = { selectedLabel = label },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryCyan.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryCyan,
                                containerColor = SurfaceDarker,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedLabel == label,
                                selectedBorderColor = PrimaryCyan,
                                borderColor = DividerColor
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLabels.drop(3).forEach { label ->
                        FilterChip(
                            selected = selectedLabel == label,
                            onClick = { selectedLabel = label },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryCyan.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryCyan,
                                containerColor = SurfaceDarker,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedLabel == label,
                                selectedBorderColor = PrimaryCyan,
                                borderColor = DividerColor
                            )
                        )
                    }
                }

                if (selectedLabel == "Other") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Custom Label (e.g. Neighbor)", color = TextSecondary, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDarker,
                            unfocusedContainerColor = SurfaceDarker,
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = PrimaryCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = DangerRed,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLabel = if (selectedLabel == "Other") {
                        customLabel.trim().ifEmpty { "Emergency Contact" }
                    } else {
                        selectedLabel
                    }
                    onAdd(emailOrPhone.trim(), finalLabel)
                },
                enabled = !isLoading && emailOrPhone.isNotBlank() && isInputValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryCyan,
                    disabledContainerColor = PrimaryCyan.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Add Contact", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
