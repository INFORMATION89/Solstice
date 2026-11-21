/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.Migrations
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.extensions.conditional
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.options.DarkMode
import org.breezyweather.common.snackbar.SnackbarContainer
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.databinding.ActivityMainBinding
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsChangedMessage
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.common.composables.AlertDialogConfirmOnly
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.common.composables.LocationPreference
import org.breezyweather.ui.expressive.components.SolsticeIcons
import org.breezyweather.ui.expressive.shell.ShellActions
import org.breezyweather.ui.expressive.shell.ShellController
import org.breezyweather.ui.expressive.shell.ShellMessage
import org.breezyweather.ui.expressive.shell.SolsticeShell
import org.breezyweather.ui.expressive.shell.messageIcon
import org.breezyweather.ui.main.dialogs.LicenseComplianceDialog
import org.breezyweather.ui.main.fragments.HomeFragment
import org.breezyweather.ui.main.fragments.ManagementFragment
import org.breezyweather.ui.main.fragments.PushedManagementFragment
import org.breezyweather.ui.search.SearchActivity
import org.breezyweather.ui.search.SearchViewModel
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BreezyActivity(), HomeFragment.Callback, ManagementFragment.Callback {

    @Inject
    lateinit var sourceManager: SourceManager

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private val _dialogPerLocationSettingsOpen = MutableStateFlow(false)
    val dialogPerLocationSettingsOpen = _dialogPerLocationSettingsOpen.asStateFlow()
    private val _isLocationBasedLightTheme: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val isLocationBasedLightTheme = _isLocationBasedLightTheme.asStateFlow()

    companion object {
        const val SEARCH_ACTIVITY = 4

        // Solstice: what the shell should do next time a MainActivity resumes. Process-wide, because
        // upstream recreates the activity when the location-based theme changes (e.g. selecting a
        // place where it's day while it's night here), which would drop instance fields.
        private var pendingShellPage: Int? = null
        private var pendingShellMessage: String? = null
        private var pendingShellAt = 0L

        const val ACTION_MAIN = "${BuildConfig.APPLICATION_ID}.Main"
        const val KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID"
        const val KEY_MAIN_ACTIVITY_ALERT_ID = "MAIN_ACTIVITY_ALERT_ID"

        const val ACTION_MANAGEMENT = "${BuildConfig.APPLICATION_ID}.ACTION_MANAGEMENT"
        const val ACTION_SHOW_ALERTS = "${BuildConfig.APPLICATION_ID}.ACTION_SHOW_ALERTS"

        const val ACTION_SHOW_DAILY_FORECAST = "${BuildConfig.APPLICATION_ID}.ACTION_SHOW_DAILY_FORECAST"
        const val ACTION_VIEW = "android.intent.action.VIEW"
        const val KEY_DAILY_INDEX = "DAILY_INDEX"

        private const val TAG_FRAGMENT_HOME = "fragment_main"
        private const val TAG_FRAGMENT_MANAGEMENT = "fragment_management"

        private const val PERMISSION_CODE_LOCATION_ACCESS = 1
        private const val PERMISSION_CODE_ACCESS_BACKGROUND_LOCATION = 2
    }

    private val backgroundUpdateObserver: Observer<Location> = Observer { location ->
        viewModel.updateLocationFromBackground(location)
    }

    private val openSearchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                BreezyWeather.instance.setTopActivity(this)
                val location = IntentCompat.getParcelableExtra(
                    it.data!!,
                    SearchActivity.KEY_LOCATION,
                    Location::class.java
                )
                if (location != null) {
                    if (viewModel.locationExists(location)) {
                        SnackbarHelper.showSnackbar(getString(R.string.location_message_already_exists))
                    } else {
                        viewModel.addLocation(location, null, this)
                        if (binding.solsticeShell != null) {
                            // Solstice: open it as a visit (kept only if starred).
                            viewModel.setLocation(location.formattedId)
                            shellController.visit(this, location.formattedId)
                            // Applied in onResume, on the instance actually shown (this one may be
                            // replaced while the search screen is in front).
                            pendingShellPage = ShellController.FORECAST
                            pendingShellAt = System.currentTimeMillis()
                            pendingShellMessage = getString(R.string.shell_visiting_hint, location.getPlace(this))
                        } else {
                            SnackbarHelper.showSnackbar(getString(R.string.location_message_added))
                        }
                    }
                }
            }
        }

    fun updateLocation(location: Location) {
        if (!viewModel.initCompleted.value) {
            return
        }

        // Only updates are coming here (no location added or deleted)
        // If we don't find the formattedId in the current list, it means main source was changed
        // for currently focused location
        // TODO: This shouldn't be the case anymore, as only WeatherUpdateJob comes here
        val oldLocation = viewModel.validLocationList.value.firstOrNull {
            it.formattedId == location.formattedId
        } ?: viewModel.currentLocation.value?.location

        if (viewModel.currentLocation.value?.location?.formattedId ==
            (oldLocation?.formattedId ?: location.formattedId)
        ) {
            viewModel.cancelRequest()
        }
        viewModel.updateLocation(location, oldLocation)
    }

    fun deleteLocation(location: Location) {
        if (locationListSize() > 1) {
            val position: Int = viewModel.validLocationList.value.indexOfFirst {
                it.formattedId == location.formattedId
            }
            if (position >= 0) {
                viewModel.deleteLocation(position)
                SnackbarHelper.showSnackbar(
                    this.getString(R.string.location_message_deleted)
                )
            }
        } else {
            SnackbarHelper.showSnackbar(
                this.getString(R.string.location_message_list_cannot_be_empty)
            )
        }
    }

    fun locationListSize(): Int {
        return viewModel.locationListSize()
    }

    private val fragmentsLifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?,
        ) {
            updateSystemBarStyle()
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            updateSystemBarStyle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        val isLaunch = savedInstanceState == null

        super.onCreate(savedInstanceState)

        if (isLaunch) {
            Migrations.upgrade(applicationContext, sourceManager, locationRepository, weatherRepository)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentsLifecycleCallback, false)
        setContentView(binding.root)

        initModel(savedInstanceState == null)
        if (isLaunch) viewModel.requestColdStartRefresh() // Solstice: refresh the main place (agreed 2026-11-21)
        initView()
        initShell() // Solstice

        if (viewModel.validLocationList.value.isEmpty()) {
            setManagementFragmentVisibility(true)
        } else if (viewModel.validLocationList.value.size == 1 && isLandscape && isDrawerLayoutVisible) {
            setManagementFragmentVisibility(false)
        }

        consumeIntentAction(intent)

        EventBus.instance
            .with(Location::class.java)
            .observeForever(backgroundUpdateObserver) // Only comes from WeatherUpdateJob
        EventBus.instance.with(SettingsChangedMessage::class.java).observe(this) {
            // Force refresh but with latest location used
            viewModel.init(viewModel.currentLocation.value?.location?.formattedId)

            findHomeFragment()?.updateViews()

            viewModel.refreshBackgroundViews(viewModel.validLocationList.value)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeIntentAction(getIntent())
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)
        if (resultCode == SEARCH_ACTIVITY) {
            val f = findManagementFragment()
            f?.prepareReenterTransition()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSystemBarStyle()
        updateDayNightColors()
    }

    override fun onPause() {
        super.onPause()
        SnackbarHelper.shell = null
    }

    override fun onResume() {
        super.onResume()
        // Solstice: messages shown while the shell is up use its message pill.
        if (binding.solsticeShell != null) {
            SnackbarHelper.shell = { content, action, onAction ->
                if (shellController.detailsOpen) {
                    false
                } else {
                    shellController.say(
                        ShellMessage(messageIcon(this, content), content, action, onAction ?: {})
                    )
                    true
                }
            }
        }
        // Solstice: apply what happened while another screen (e.g. search) was in front.
        if ((pendingShellPage != null || pendingShellMessage != null) &&
            System.currentTimeMillis() - pendingShellAt < 5_000L
        ) {
            shellController.show(pendingShellPage, pendingShellMessage)
            pendingShellPage = null
            pendingShellMessage = null
        }
    }

    override fun onStart() {
        super.onStart()

        if (BreezyWeather.instance.isImpersonatingBreezyWeather) {
            viewModel.emptyLocationListFake()
            LicenseComplianceDialog.show(this)
        } else {
            viewModel.checkToUpdate()
        }

        binding.root.doOnApplyWindowInsets { view, insets ->
            if (this.getResources().configuration.orientation == 2) {
                // Apply root insets in landscape mode for a consistent look across different
                // device types and navigation modes.
                view.updatePadding(
                    left = insets.left,
                    right = insets.right
                )
            } else {
                view.updatePadding(
                    left = 0,
                    right = 0
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentsLifecycleCallback)
        EventBus.instance
            .with(Location::class.java)
            .removeObserver(backgroundUpdateObserver)
    }

    override val snackbarContainer: SnackbarContainer
        get() {
            if (binding.drawerLayout != null) {
                return super.snackbarContainer
            }

            val f = if (isManagementFragmentVisible) {
                findManagementFragment()
            } else {
                findHomeFragment()
            }

            return f?.snackbarContainer ?: super.snackbarContainer
        }

    // init.

    private fun initModel(newActivity: Boolean) {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        if (!viewModel.checkIsNewInstance()) return
        if (newActivity) {
            viewModel.init(formattedId = getLocationId(intent))
        } else {
            viewModel.init()
        }
    }

    private fun getLocationId(intent: Intent?): String? {
        return intent?.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
    }

    private fun initView() {
        binding.root.post {
            if (isActivityCreated) {
                updateDayNightColors()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationPermissionsRequest.collect {
                    if (it != null &&
                        it.permissionList.isNotEmpty() &&
                        it.consume()
                    ) {
                        // only show dialog if we need request basic location permissions.
                        var showLocationPermissionDialog = false
                        for (permission in it.permissionList) {
                            if (isLocationPermission(permission)) {
                                showLocationPermissionDialog = true
                                break
                            }
                        }

                        if (showLocationPermissionDialog &&
                            !viewModel.statementManager.isLocationPermissionDialogAlreadyShown
                        ) {
                            val dialogLocationPermissionOpenState = mutableStateOf(true)
                            binding.locationPermissionDialog.setContent {
                                BreezyWeatherTheme(
                                    !ThemeManager.isLightTheme(this@MainActivity, daylight = isDaylight)
                                ) {
                                    if (dialogLocationPermissionOpenState.value) {
                                        AlertDialogConfirmOnly(
                                            title = R.string.dialog_permissions_location_title,
                                            content = R.string.dialog_permissions_location_content,
                                            confirmButtonText = R.string.action_next,
                                            onConfirm = {
                                                // mark declared.
                                                viewModel.statementManager
                                                    .setLocationPermissionDialogAlreadyShown()

                                                val request = viewModel.locationPermissionsRequest.value
                                                if (request != null &&
                                                    request.permissionList.isNotEmpty() &&
                                                    request.target != null
                                                ) {
                                                    requestPermissions(
                                                        request.permissionList.toTypedArray(),
                                                        PERMISSION_CODE_LOCATION_ACCESS
                                                    )
                                                }

                                                dialogLocationPermissionOpenState.value = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            requestPermissions(it.permissionList.toTypedArray(), PERMISSION_CODE_LOCATION_ACCESS)
                        }
                    }
                }
            }
        }
        viewModel.snackbarError.observe(this) { errors ->
            if (errors.size > 1) {
                SnackbarHelper.showSnackbar(
                    content = getString(R.string.message_multiple_refresh_errors),
                    action = getString(R.string.action_show)
                ) {
                    // Inefficient workaround to apply the correct theme while taking the location-based theme setting
                    // into account. TODO: replace with a central solution which can be used in all composables
                    _isLocationBasedLightTheme.value = if (isDrawerLayoutVisible || !isManagementFragmentVisible) {
                        viewModel.currentLocation.value?.daylight
                    } else {
                        null
                    }
                    viewModel.setRefreshErrorDetailsDialogVisible(true)
                }
            } else {
                errors.firstOrNull()?.let { error ->
                    val shortMessage = error.getMessage(this, sourceManager)
                    error.error.showDialogAction?.let { showDialogAction ->
                        SnackbarHelper.showSnackbar(
                            content = shortMessage,
                            action = getString(error.error.actionButtonMessage)
                        ) {
                            showDialogAction(this)
                        }
                    } ?: SnackbarHelper.showSnackbar(shortMessage)
                }
            }
        }

        initPerLocationSettingsView()

        binding.refreshErrorDialog.setContent {
            val isLightTheme = isLocationBasedLightTheme.collectAsState()

            BreezyWeatherTheme(
                !ThemeManager.isLightTheme(this, isLightTheme.value)
            ) {
                RefreshErrorDetails()
            }
        }
    }

    private fun initPerLocationSettingsView() {
        binding.perLocationSettings.setContent {
            val validLocation = viewModel.currentLocation.collectAsState()

            BreezyWeatherTheme(
                !ThemeManager.isLightTheme(this, validLocation.value?.daylight)
            ) {
                PerLocationSettingsDialog(location = validLocation.value?.location)
            }
        }
    }

    @Composable
    fun PerLocationSettingsDialog(
        location: Location?,
        modifier: Modifier = Modifier,
    ) {
        val dialogPerLocationSettingsOpenState = dialogPerLocationSettingsOpen.collectAsState()
        if (dialogPerLocationSettingsOpenState.value) {
            location?.let {
                val dialogDeleteLocationOpenState = remember { mutableStateOf(false) }
                AlertDialogNoPadding(
                    modifier = modifier,
                    onDismissRequest = {
                        _dialogPerLocationSettingsOpen.value = false
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.action_edit_location),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    text = {
                        LocationPreference(
                            activity = this,
                            location = it,
                            onClose = { newLocation: Location? ->
                                if (newLocation != null) {
                                    updateLocation(newLocation)
                                }
                                _dialogPerLocationSettingsOpen.value = false
                            },
                            locationExists = { loc: Location ->
                                viewModel.locationExists(loc)
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                _dialogPerLocationSettingsOpen.value = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_close),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    },
                    dismissButton = if (locationListSize() > 1) {
                        {
                            TextButton(
                                onClick = {
                                    dialogDeleteLocationOpenState.value = true
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    } else {
                        null
                    }
                )

                if (dialogDeleteLocationOpenState.value) {
                    AlertDialog(
                        onDismissRequest = {
                            dialogDeleteLocationOpenState.value = false
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.location_delete_location_dialog_title),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Text(
                                text = if (it.city.isNotEmpty()) {
                                    stringResource(
                                        R.string.location_delete_location_dialog_message,
                                        it.city
                                    )
                                } else {
                                    stringResource(R.string.location_delete_location_dialog_message_no_name)
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogDeleteLocationOpenState.value = false
                                    _dialogPerLocationSettingsOpen.value = false
                                    deleteLocation(it)
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_confirm),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dialogDeleteLocationOpenState.value = false
                                }
                            ) {
                                Text(
                                    text = stringResource(android.R.string.cancel),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun RefreshErrorDetails(
        modifier: Modifier = Modifier,
    ) {
        val dialogRefreshErrorDetailsOpenState = viewModel.dialogRefreshErrorDetails.collectAsState()

        if (dialogRefreshErrorDetailsOpenState.value) {
            AlertDialogNoPadding(
                modifier = modifier,
                onDismissRequest = { /* do nothing */ },
                title = {
                    Column {
                        Text(stringResource(R.string.dialog_refresh_error_details_title))
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                        Text(
                            text = stringResource(R.string.dialog_refresh_error_details_content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.setRefreshErrorDetailsDialogVisible(false) },
                        content = {
                            Text(stringResource(R.string.action_close))
                        }
                    )
                },
                text = {
                    LazyColumn {
                        items(viewModel.snackbarError.value!!) {
                            val source = it.getSourceWithOptionalFeature(this@MainActivity, sourceManager)
                            val message = this@MainActivity.getString(it.error.shortMessage)

                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                                ),
                                modifier = Modifier
                                    .conditional(it.error.showDialogAction != null, {
                                        clickable { it.error.showDialogAction!!(this@MainActivity) }
                                    })
                                    .padding(vertical = dimensionResource(R.dimen.small_margin)),
                                headlineContent = {
                                    Text(
                                        text = source ?: message,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                },
                                supportingContent = {
                                    source?.let {
                                        Column {
                                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                                            Text(
                                                text = message,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                },
                                trailingContent = {
                                    it.error.showDialogAction?.let {
                                        Icon(
                                            imageVector = SolsticeIcons.Help,
                                            contentDescription = stringResource(R.string.action_help)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE_LOCATION_ACCESS &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            !viewModel.statementManager.isBackgroundLocationPermissionDialogAlreadyShown &&
            !this.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            val dialogBackgroundLocationPermissionOpenState = mutableStateOf(true)
            binding.locationPermissionDialog.setContent {
                BreezyWeatherTheme(
                    !ThemeManager.isLightTheme(this@MainActivity, daylight = isDaylight)
                ) {
                    if (dialogBackgroundLocationPermissionOpenState.value) {
                        AlertDialogConfirmOnly(
                            title = R.string.dialog_permissions_location_background_title,
                            content = R.string.dialog_permissions_location_background_content,
                            confirmButtonText = R.string.action_set,
                            onConfirm = {
                                // mark background location permission declared.
                                viewModel.statementManager
                                    .setBackgroundLocationPermissionDialogAlreadyShown()
                                // request background location permission.
                                requestPermissions(
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    PERMISSION_CODE_ACCESS_BACKGROUND_LOCATION
                                )
                                dialogBackgroundLocationPermissionOpenState.value = false
                            }
                        )
                    }
                }
            }
            return
        }

        if (requestCode == PERMISSION_CODE_LOCATION_ACCESS ||
            requestCode == PERMISSION_CODE_ACCESS_BACKGROUND_LOCATION
        ) {
            viewModel.updateWithUpdatingChecking(
                triggeredByUser = false,
                checkPermissions = false
            )
        }
    }

    private fun isLocationPermission(
        permission: String,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION || isEssentialLocationPermission(permission)
    } else {
        isEssentialLocationPermission(permission)
    }

    private fun isEssentialLocationPermission(permission: String): Boolean {
        return permission == Manifest.permission.ACCESS_COARSE_LOCATION ||
            permission == Manifest.permission.ACCESS_FINE_LOCATION
    }

    val isDaylight: Boolean
        get() = viewModel.currentLocation.value?.daylight ?: true

    private fun consumeIntentAction(intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) return
        val formattedId = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID)
        when (action) {
            ACTION_SHOW_ALERTS -> {
                val alertId = intent.getStringExtra(KEY_MAIN_ACTIVITY_ALERT_ID)
                if (!alertId.isNullOrEmpty()) {
                    IntentHelper.startAlertActivity(this, formattedId, alertId)
                } else {
                    IntentHelper.startAlertActivity(this, formattedId)
                }
            }
            ACTION_SHOW_DAILY_FORECAST -> {
                val index = intent.getIntExtra(KEY_DAILY_INDEX, 0)
                IntentHelper.startDailyWeatherActivity(this, formattedId, index)
            }
            ACTION_MANAGEMENT -> {
                setManagementFragmentVisibility(true)
            }
            ACTION_VIEW -> {
                val uri = intent.data
                if (uri?.scheme.equals("geo", ignoreCase = true) && !uri?.schemeSpecificPart.isNullOrEmpty()) {
                    if (BreezyWeather.instance.debugMode) {
                        LogHelper.log(msg = "Geo URI found")
                    }
                    val regex = Regex(
                        "^(?<lat>^[-+]?(?:[1-8]?\\d(?:\\.\\d+)?|90(?:\\.0+)?))\\s*,\\s*" +
                            "(?<lng>[-+]?(?:180(?:\\.0+)?|(?:1[0-7]\\d|[1-9]?\\d)(?:\\.\\d+)?))(.*)$"
                    )
                    val matching = regex.find(uri.schemeSpecificPart!!)
                    if (matching != null) {
                        setManagementFragmentVisibility(true)
                        viewModel.askToAddLocation(
                            context = this,
                            latitude = matching.groups[1]!!.value.toDouble(),
                            longitude = matching.groups[2]!!.value.toDouble()
                        )
                    } else {
                        if (BreezyWeather.instance.debugMode) {
                            LogHelper.log(msg = "Geo URI malformed: ${uri.schemeSpecificPart}")
                        }
                    }
                }
            }
        }
    }

    private fun updateSystemBarStyle() {
        if (binding.drawerLayout != null) {
            findHomeFragment()?.setSystemBarStyle()
            return
        }

        if (isOrWillManagementFragmentVisible) {
            findManagementFragment()?.setSystemBarStyle()
        } else {
            findHomeFragment()?.setSystemBarStyle()
        }
    }

    private fun updateDayNightColors() {
        if (this.getResources().configuration.orientation == 2) {
            // Set a black background to keep the background of the system bars black when root
            // insets are applied in landscape mode.
            binding.root.setBackgroundColor(Color.BLACK)
        } else {
            binding.root.setBackgroundColor(getThemeColor(android.R.attr.colorBackground))
        }
    }

    private val isOrWillManagementFragmentVisible: Boolean
        get() = binding.drawerLayout?.isUnfold
            ?: findManagementFragment()?.let { !it.isRemoving }
            ?: false

    val isManagementFragmentVisible: Boolean
        get() = binding.drawerLayout?.isUnfold
            ?: findManagementFragment()?.isVisible
            ?: false

    val isDrawerLayoutVisible: Boolean
        get() = binding.drawerLayout?.isVisible ?: false

    fun setManagementFragmentVisibility(visible: Boolean) {
        // Solstice: on phones, Places is a page of the shell.
        if (binding.solsticeShell != null) {
            shellController.showPlaces(visible)
            return
        }
        val drawerLayout = binding.drawerLayout
        if (drawerLayout != null) {
            drawerLayout.isUnfold = visible
            return
        }
        if (visible == isOrWillManagementFragmentVisible) return
        if (!visible) {
            supportFragmentManager.popBackStack()
            return
        }

        val transaction = supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                if (isRtl) R.anim.slide_in_right else R.anim.slide_in_left,
                if (isRtl) R.anim.slide_out_left else R.anim.slide_out_right,
                if (isRtl) R.anim.slide_in_left else R.anim.slide_in_right,
                if (isRtl) R.anim.slide_out_right else R.anim.slide_out_left
            )
            .add(
                R.id.fragment,
                PushedManagementFragment.getInstance(),
                TAG_FRAGMENT_MANAGEMENT
            )
            .addToBackStack(null)

        findHomeFragment()?.let {
            transaction.hide(it)
        }

        transaction.commit()
    }

    // Solstice: the new main screen on phones. Upstream's Forecast (HomeFragment) stays underneath,
    // hidden, and is shown as "More details" until the Compose Forecast covers everything.
    private val shellController = ShellController()

    /** True when the Solstice shell is the main screen (phones). */
    val usesShell: Boolean get() = ::binding.isInitialized && binding.solsticeShell != null
    private val closeDetailsOnBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = showDetails(false)
    }

    private fun initShell() {
        val shell = binding.solsticeShell ?: return
        // The shell places things above the keyboard itself (bottom search bar); don't let the
        // system pan the whole window when a field gets focus.
        @Suppress("DEPRECATION")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        // Solstice: light or dark comes from the phone's setting only, never from the place on screen
        // (upstream switched the activity's night mode per place, e.g. dark while looking at a city
        // where it's night). Clear any such override and read the system, not the activity.
        if (delegate.localNightMode != AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
        }
        shell.setContent {
            val systemDark = (
                applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                ) == Configuration.UI_MODE_NIGHT_YES
            val appDark = when (SettingsManager.getInstance(this@MainActivity).darkMode) {
                DarkMode.LIGHT -> false
                DarkMode.DARK -> true
                else -> systemDark
            }
            BreezyWeatherTheme(darkTheme = appDark) {
                SolsticeShell(
                    viewModel = viewModel,
                    controller = shellController,
                    sourceManager = sourceManager,
                    searchViewModel = ViewModelProvider(this@MainActivity)[SearchViewModel::class.java],
                    actions = object : ShellActions {
                        override fun openSearch() = onSearchBarClicked()
                        override fun openDetails() = showDetails(true)
                        override fun openSettings() = IntentHelper.startSettingsActivity(this@MainActivity)
                        override fun openLocationSettings() = onEditIconClicked()
                        override fun afterPick(page: Int?, message: String?) {
                            // Show it now; and if the theme flip recreates the activity, the new one
                            // picks it up in onResume (within a few seconds).
                            shellController.show(page, message)
                            pendingShellPage = page
                            pendingShellMessage = message
                            pendingShellAt = System.currentTimeMillis()
                        }
                    }
                )
            }
        }
        onBackPressedDispatcher.addCallback(this, closeDetailsOnBack)
        binding.root.post { showDetails(false) }
    }

    private fun showDetails(show: Boolean) {
        val shell = binding.solsticeShell ?: return
        shell.visibility = if (show) View.GONE else View.VISIBLE
        closeDetailsOnBack.isEnabled = show
        shellController.detailsOpen = show
        findHomeFragment()?.let {
            supportFragmentManager.beginTransaction().apply {
                if (show) show(it) else hide(it)
            }.commitAllowingStateLoss()
        }
    }

    private fun findHomeFragment(): HomeFragment? {
        return if (binding.drawerLayout == null) {
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_HOME) as HomeFragment?
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_home) as HomeFragment?
        }
    }

    private fun findManagementFragment(): ManagementFragment? {
        return if (binding.drawerLayout == null) {
            supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_MANAGEMENT) as ManagementFragment?
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_drawer) as ManagementFragment?
        }
    }

    // interface.

    // main fragment callback.
    override fun onEditIconClicked() {
        if (viewModel.loading.value) {
            SnackbarHelper.showSnackbar(getString(R.string.message_please_wait_refresh))
        } else {
            _dialogPerLocationSettingsOpen.value = true
            initPerLocationSettingsView()
        }
    }

    override fun onOpenInOtherAppIconClicked() {
        viewModel.currentLocation.value?.location?.let {
            try {
                IntentHelper.startBreezyActivity(this@MainActivity, it)
            } catch (ignored: Exception) {
                SnackbarHelper.showSnackbar(getString(R.string.action_open_in_other_app_no_app))
            }
        }
    }

    override fun onManageIconClicked() {
        setManagementFragmentVisibility(!isOrWillManagementFragmentVisible)
    }

    // management fragment callback.

    override fun onSearchBarClicked() {
        openSearchActivity.launch(
            IntentHelper.buildSearchActivityIntent(this)
        )
    }
}
