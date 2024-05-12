package edu.akolomiets.bot.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Event.Reminders
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import edu.akolomiets.bot.entity.GoogleCalendarEvent
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStreamReader
import java.time.ZoneId
import java.util.*
import java.util.List

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Service
class GoogleCalendarService {

    fun createEvent(userGmail: String?, googleCalendarEvent: GoogleCalendarEvent): String {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
            .setApplicationName(APPLICATION_NAME)
            .build()
        val event = Event()
            .setSummary(googleCalendarEvent.summary)
        val startDate = googleCalendarEvent.startDate
        val startDateTime = DateTime(
            Date.from(
                startDate!!.atZone(ZoneId.systemDefault()).toInstant()
            )
        )
        val start = EventDateTime()
            .setDateTime(startDateTime)
        event.start = start
        val endDate = googleCalendarEvent.startDate
        val endDateTime = DateTime(
            Date.from(
                endDate!!.atZone(ZoneId.systemDefault()).toInstant()
            )
        )
        val end = EventDateTime()
            .setDateTime(endDateTime)
        event.end = end
        val attendees = List.of(EventAttendee().setEmail(userGmail))
        event.attendees = attendees
        val reminderOverrides = List.of(
            EventReminder()
                .setMethod("email")
                .setMinutes(24 * 60)
        )
        val reminders = Reminders()
            .setUseDefault(false)
            .setOverrides(reminderOverrides)
        event.reminders = reminders
        val calendarId = "primary"
        val createdEvent = service.events()
            .insert(calendarId, event)
            .execute()
        return createdEvent.htmlLink
    }

    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        val credentialInputStream = GoogleCalendarService::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
        val clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY,
            InputStreamReader(credentialInputStream)
        )
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .build()
        val receiver = LocalServerReceiver.Builder()
            .setPort(CALLBACK_PORT)
            .setCallbackPath(CALLBACK_PATH)
            .build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    companion object {
        private const val APPLICATION_NAME = "TaskMaster"
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()
        private val SCOPES = listOf(CalendarScopes.CALENDAR)

        private const val TOKENS_DIRECTORY_PATH = "data/tokens"
        private const val CREDENTIALS_FILE_PATH = "/client_secret.json"
        private const val CALLBACK_PATH = "/login/google"
        private const val CALLBACK_PORT = 9000
    }
}