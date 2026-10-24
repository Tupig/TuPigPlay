package com.shilapi.xcertplay.hud

import android.content.Context
import com.shilapi.xcertplay.iap2.message.Iap2Messages
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [29])
class TuPigClusterSongNoteTest {
    private val type = TuPigClusterSong::class.java
    private fun field(name: String) = type.getDeclaredField(name).apply { isAccessible = true }
    private fun get(name: String) = field(name).get(TuPigClusterSong)
    private fun set(name: String, value: Any?) = field(name).set(TuPigClusterSong, value)
    private fun wanted() = get("wanted") as ClusterSong?

    private fun song(title: String) = TuPigClusterSong.onFrame(
        Iap2Messages.buildRaw(ClusterSongState.NOW_PLAYING_UPDATE) {
            group(0) { string(1, title); string(12, "Artist") }
        },
    )

    private fun expire(app: Context, token: Any, alreadyShown: ClusterSong?) {
        // The real writer is blocked below; exercise the actual callback while avoiding ADB.
        set("shown", alreadyShown)
        type.getDeclaredMethod("endNote", Context::class.java, Any::class.java)
            .apply { isAccessible = true }.invoke(TuPigClusterSong, app, token)
    }

    private fun withWriterBlocked(check: (Context) -> Unit) {
        val app = RuntimeEnvironment.getApplication()
        val writer = get("writer") as ScheduledExecutorService
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        writer.execute { entered.countDown(); release.await() }
        assertTrue(entered.await(5, TimeUnit.SECONDS))
        try {
            TuPigClusterSong.attach(app)
            TuPigOutputSettings.setClusterSong(app, true)
            (get("state") as ClusterSongState).clear()
            set("note", null)
            set("wanted", null)
            set("shown", null)
            check(app)
        } finally {
            (get("state") as ClusterSongState).clear()
            set("note", null)
            set("wanted", null)
            set("shown", null)
            set("context", null)
            TuPigOutputSettings.setClusterSong(app, false)
            release.countDown()
            writer.submit {}.get(5, TimeUnit.SECONDS)
        }
    }

    @Test fun metadataUpdatesKeepTheNoteThenRestoreTheLatestSong() = withWriterBlocked { app ->
        song("First")
        TuPigClusterSong.note("Zoom", source = 7)
        val token = get("note")!!
        val card = wanted()
        song("Second")
        TuPigClusterSong.onFrame(Iap2Messages.buildRaw(ClusterSongState.NOW_PLAYING_UPDATE) {
            group(1) { u8(0, 1) }
        })
        assertEquals(card, wanted())
        assertEquals(7, wanted()!!.source)
        val current = TuPigClusterSong.current()
        expire(app, token, current)
        assertEquals(current, wanted())
        assertTrue(wanted()!!.playing)
    }

    @Test fun endingASessionInvalidatesItsNoteEvenAfterANewSongArrives() = withWriterBlocked { app ->
        song("Old")
        TuPigClusterSong.note("Old note")
        val token = get("note")!!
        TuPigClusterSong.end()
        assertNull(get("note"))
        assertNull(TuPigClusterSong.current())
        assertNull(wanted())
        song("New")
        TuPigClusterSong.note("New note")
        val newToken = get("note")!!
        val card = wanted()
        expire(app, token, card)
        assertSame(newToken, get("note"))
        assertEquals(card, wanted())
    }

    @Test fun anOlderNoteCannotDismissANewerNote() = withWriterBlocked { app ->
        song("Track")
        TuPigClusterSong.note("First note")
        val token = get("note")!!
        TuPigClusterSong.note("Second note")
        val newToken = get("note")!!
        val card = wanted()
        expire(app, token, card)
        assertSame(newToken, get("note"))
        assertEquals(card, wanted())
    }

    @Test fun disablingSongOutputCancelsTheNoteWithoutResurrectingACard() = withWriterBlocked { app ->
        song("Track")
        TuPigClusterSong.note("Zoom")
        val token = get("note")!!
        TuPigOutputSettings.setClusterSong(app, false)
        TuPigClusterSong.settingChanged(false)
        assertNull(get("note"))
        expire(app, token, null)
        assertNull(wanted())
    }

    @Test fun aWheelNoteStillWorksWithTheSongFeatureOffAndClearsAfterExpiry() = withWriterBlocked { app ->
        TuPigOutputSettings.setClusterSong(app, false)
        song("Track")
        TuPigClusterSong.note("Zoom")
        assertEquals("Zoom", wanted()!!.text)
        val token = get("note")!!
        expire(app, token, null)
        assertNull(wanted())
    }
}
