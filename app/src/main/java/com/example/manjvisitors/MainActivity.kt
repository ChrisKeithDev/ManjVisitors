package com.example.manjvisitors

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var finalPurpose by mutableStateOf<String?>(null)

    private fun updateSelectedPurpose(purpose: String?) {
        finalPurpose = purpose
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var step by mutableIntStateOf(1) // Step 1: Scan ID, Step 2: Purpose Selection
        var scannedFirstName by mutableStateOf("")
        var scannedLastName by mutableStateOf("")
        var scannedStreet by mutableStateOf("")
        var scannedCity by mutableStateOf("")
        var scannedState by mutableStateOf("")
        var scannedSex by mutableStateOf("")
        var scannedDateBirth by mutableStateOf("")

        // ActivityResultLauncher for handling Scannr app result
        val scannrLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanData = result.data?.getStringExtra("scanData")
                if (scanData != null) {
                    try {
                        val jsonObject = JSONObject(scanData)
                        val firstName = jsonObject.optString("kPPCustomerFirstName").toTitleCase()
                        val lastName = jsonObject.optString("kPPCustomerFamilyName").toTitleCase()
                        val street = jsonObject.optString("kPPAddressStreet").toTitleCase()
                        val city = jsonObject.optString("kPPAddressCity").toTitleCase()
                        val state = jsonObject.optString("kPPAddressJurisdictionCode")
                        val sex = jsonObject.optString("kPPSex").toTitleCase()
                        val dateBirth = jsonObject.optString("kPPDateOfBirth")

                        scannedFirstName = firstName
                        scannedLastName = lastName
                        scannedStreet = street
                        scannedCity = city
                        scannedState = state
                        scannedSex = sex
                        scannedDateBirth = dateBirth
                        step = 2
                    } catch (e: Exception) {
                        // Handle parsing error
                    }
                }
            }
        }

        setContent {
            when (step) {
                1 -> {
                    ScanIdScreen(onScanClicked = {
                        val scannrIntent = Intent(Intent.ACTION_PICK, Uri.parse("scannr://"))
                        scannrLauncher.launch(scannrIntent)
                    })
                }
                2 -> {
                    PurposeSelectionScreen(
                        passedName = "$scannedFirstName $scannedLastName",
                        onPurposeOfVisitChange = { purpose->
                            updateSelectedPurpose(purpose)
                        },
                        onSubmitClicked = {
                            step = 3 // Navigate to ConfirmationScreen
                        }
                    )
                }
                3 -> {
                    ConfirmationScreen(
                        passedName = "$scannedFirstName $scannedLastName",
                        passedAddress = "$scannedStreet, $scannedCity, $scannedState",
                        finalPurpose = finalPurpose ?: "", // Use the selectedPurpose state variable
                        onConfirm = {
                            // TODO: Handle confirmation and reset to step 1
                            step = 4
                        }
                    )
                }
                4 -> {
                    ThankYouScreen(onDone = {
                        step = 1 // Reset to the first step or navigate as needed
                    })
                }
            }
        }
    }
}

@Composable
fun ScanIdScreen(onScanClicked: () -> Unit) {
    BoxWithConstraints (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E4D3))
    ){
        val maxWidth = maxWidth
        val isCompact = maxWidth < 400.dp // Threshold for a compact layout
        val scaleRange = 0.9f to 1.1f // scale range for growing and shrinking
        var toggle by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000) // Duration of each scale animation cycle
                toggle = !toggle
            }
        }
        val animatedScale by animateFloatAsState(
            targetValue = if (toggle) scaleRange.first else scaleRange.second,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = ""
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isCompact) 8.dp else 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.manj_shield),
                    modifier = Modifier.size(if (isCompact) 60.dp else 100.dp),
                    contentDescription = "Manj Logo"
                )
                Text(
                    text = "Welcome to MANJ!",
                    color = Color(0xFF616160),
                    fontSize = if (isCompact) 18.sp else 34.sp
                )
            }
            // Wrap the text in a Box with a solid color
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .background(Color(0xFF6C191C), shape = RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "For the safety of our students all visitors must sign in with their ID",
                    color = Color(0xFFE6E4D3),
                    fontSize = if (isCompact) 16.sp else 48.sp,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            // Repeat for other Text elements
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.75f)
                    .background(Color(0xFF6C191C), shape = RoundedCornerShape(20.dp))
                    .padding(15.dp)
            ) {
                Text(
                    text = stringResource(R.string.scan_instruct),
                    color = Color(0xFFE6E4D3),
                    fontSize = if (isCompact) 14.sp else 48.sp,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(56.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = onScanClicked,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width((if (isCompact) maxWidth.times(0.8f) else 350.dp) * animatedScale)
                        .height((if (isCompact) 50.dp else 125.dp) * animatedScale),
                    shape = RoundedCornerShape(65.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 8.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF196C69)
                    )
                ) {
                    Text(
                        "Scan ID",
                        fontSize = if (isCompact) 20.sp else 55.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PurposeSelectionScreen(
    passedName: String,
    onSubmitClicked: (String) -> Unit,
    onPurposeOfVisitChange: (String) -> Unit
) {
    // List of strings to create buttons
    val buttonList = listOf("Tour", "Observation", "Meeting", "Pick up", "Drop off", "Other")

    // State to keep track of the selected button
    var selectedPurpose by remember { mutableStateOf<String?>(null) }
    var appendedPurpose by remember { mutableStateOf<String?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false)}
    var additionalDetails by remember { mutableStateOf("")}
    var detailsEntered by remember { mutableStateOf(false) }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = {
                showDetailsDialog = false
                additionalDetails = ""
            },
            title = { Text("Enter Details") },
            text = {
                TextField(
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    label = { Text("Details") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        var updatedPurpose = selectedPurpose ?: ""
                        if (additionalDetails.isNotEmpty()) {
                            updatedPurpose += " - $additionalDetails"
                            detailsEntered = true
                        }
                        // Use onPurposeOfVisitChange to ensure updatedPurpose is communicated back
                        onPurposeOfVisitChange(updatedPurpose)
                        // Then call onSubmitClicked with the updated purpose
                        onSubmitClicked(updatedPurpose)
                        showDetailsDialog = false
                        additionalDetails = ""
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = {
                    showDetailsDialog = false
                    additionalDetails = ""
                }) { Text("Cancel") }
            }
        )
    }

    BoxWithConstraints (
        modifier = Modifier
            .background(Color(0xFFE6E4D3))
            .fillMaxSize()
    ){
        val maxWidth = maxWidth
        val isCompact = maxWidth < 400.dp // Threshold for a compact layout
        var toggle by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000) // Duration of each scale animation cycle
                toggle = !toggle
            }
        }
        val animatedScale by animateFloatAsState(
            targetValue = if (toggle) 0.9f else 1.1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = ""
        )

        Column (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome, $passedName!",
                fontSize = 48.sp,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Select the purpose of your visit",
                fontSize = 68.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedPurpose == null){
                // Display LazyVerticalGrid when no button is selected
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(50.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(100.dp)
                ) {
                    items(buttonList) { buttonText ->
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            onClick = {
                                selectedPurpose = buttonText
                                onPurposeOfVisitChange(buttonText)
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFF6C191C)),
                            modifier = Modifier
                                .height(180.dp)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = buttonText,
                                color = Color(0xFFE6E4D3),
                                fontSize = 48.sp,
                                maxLines = 2,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Display selected button and Continue button when a button is selected
                if ((selectedPurpose == "Pick up" || selectedPurpose == "Drop off" || selectedPurpose == "Other")) {
                    showDetailsDialog = true
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            selectedPurpose = null
                            onPurposeOfVisitChange(null.toString())
                        },
                        colors = ButtonDefaults.buttonColors(
                            Color.Transparent),
                        modifier = Modifier
                            .height(180.dp)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = selectedPurpose ?: "",
                            color = Color(0xFF6C191C),
                            fontSize = 78.sp,
                            maxLines = 2,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(56.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!detailsEntered) {
                                    // Only update finalPurpose if details haven't been entered
                                    appendedPurpose = selectedPurpose ?: ""
                                }
                                onPurposeOfVisitChange(appendedPurpose!!)
                                onSubmitClicked(appendedPurpose!!)
                                // Reset states after submission
                                selectedPurpose = null
                                detailsEntered = false  // Reset the detailsEntered flag
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width((if (isCompact) maxWidth.times(0.8f) else 350.dp) * animatedScale)
                                .height((if (isCompact) 50.dp else 125.dp) * animatedScale),
                            shape = RoundedCornerShape(65.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 12.dp,
                                pressedElevation = 8.dp
                            ),
                            colors = ButtonDefaults.buttonColors(
                                Color(0xFF196C69)
                            )
                        ) {
                            Text(
                                "Continue",
                                fontSize = if (isCompact) 20.sp else 55.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ConfirmationScreen(
    passedName: String,
    passedAddress: String,
    finalPurpose: String,
    onConfirm: () -> Unit
) {
    BoxWithConstraints (
        modifier = Modifier
            .background(Color(0xFFE6E4D3))
            .fillMaxSize()
    ){
        val maxWidth = maxWidth
        val isCompact = maxWidth < 400.dp // Threshold for a compact layout
        val scaleRange = 0.9f to 1.1f // scale range for growing and shrinking
        var toggle by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000) // Duration of each scale animation cycle
                toggle = !toggle
            }
        }
        val animatedScale by animateFloatAsState(
            targetValue = if (toggle) scaleRange.first else scaleRange.second,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = ""
        )
        val (streetAddress, cityState) = splitAddressForDisplay(passedAddress)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .background(Color(0xFFE6E4D3))
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Enable scrolling for smaller screens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Please confirm your details",
                fontSize = if (isCompact) 30.sp else 60.sp, // Responsive text sizing
                modifier = Modifier.padding(bottom = 16.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = passedName,
                fontSize = if (isCompact) 24.sp else 48.sp, // Responsive text sizing
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color(0xFF6C191C)
            )
            Text(
                text = streetAddress,
                fontSize = if (isCompact) 24.sp else 48.sp, // Responsive text sizing
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = cityState,
                fontSize = if (isCompact) 24.sp else 48.sp, // Responsive text sizing
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Here for $finalPurpose",
                fontSize = if (isCompact) 24.sp else 48.sp, // Responsive text sizing
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color(0xFF6C191C)
            )
            Spacer(modifier = Modifier.height(56.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = {
                        addVisitToDb(passedName, passedAddress, finalPurpose)
                        onConfirm()
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width((if (isCompact) maxWidth.times(0.8f) else 350.dp) * animatedScale)
                        .height((if (isCompact) 50.dp else 125.dp) * animatedScale),
                    shape = RoundedCornerShape(65.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 8.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF196C69)
                    )
                ) {
                    Text(
                        "Confirm",
                        fontSize = if (isCompact) 20.sp else 55.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ThankYouScreen(onDone: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E4D3))
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Thank You!  Your visit has been logged.",
                color = Color(0xFF6C191C),
                fontSize = 48.sp, // Responsive text sizing
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
                )
            LaunchedEffect(Unit) {
                delay(7000L) // Wait for 7 seconds
                onDone() // Call onDone to navigate away or reset the flow
            }
        }
    }
}

fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { it.lowercase(Locale.getDefault()).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
    }}
}

// Utility function to split the address into two parts
fun splitAddressForDisplay(address: String): Pair<String, String> {
    val parts = address.split(", ")
    return if (parts.size >= 2) {
        // Assuming the first part is the street and the rest is city and state
        Pair(parts.first(), parts.drop(1).joinToString(" "))
    } else {
        // Fallback in case the address does not contain a comma
        Pair(address, "")
    }
}

private fun addVisitToDb(name: String, address: String, purpose: String) {
    val data = hashMapOf(
        "name" to name,
        "address" to address,
        "purpose" to purpose
    )

    FirebaseFunctions.getInstance()
        .getHttpsCallable("addVisit")
        .call(data)
        .continueWith { task ->
            val result = task.result?.data as String
            Log.d(TAG, "Result: $result")
        }
        .addOnFailureListener {
            Log.e(TAG, "Failed to call addVisit function", it)
        }
}