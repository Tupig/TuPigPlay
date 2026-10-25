package com.shilapi.xcertplay.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class UpdateCatalogTest {
    private val releaseJson = """
        [
          {
            "tag_name": "v0.2.15",
            "draft": true,
            "assets": [
              {"name": "TuPigCarPlay-0.2.15.apk", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.15/TuPigCarPlay-0.2.15.apk"},
              {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.15/SHA256SUMS.txt"}
            ]
          },
          {
            "tag_name": "v0.2.14-rc.1",
            "draft": false,
            "prerelease": false,
            "assets": [
              {"name": "TuPigCarPlay-0.2.14-rc.1.apk", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.14-rc.1/TuPigCarPlay-0.2.14-rc.1.apk"},
              {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.14-rc.1/SHA256SUMS.txt"}
            ]
          },
          {
            "tag_name": "v0.2.14",
            "draft": false,
            "prerelease": true,
            "assets": [
              {"name": "TuPigCarPlay-0.2.14.apk", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.14/TuPigCarPlay-0.2.14.apk"},
              {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.14/SHA256SUMS.txt"}
            ]
          },
          {
            "tag_name": "v0.2.13",
            "draft": false,
            "prerelease": false,
            "assets": [
              {"name": "TuPigCarPlay-0.2.13.apk", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.13/TuPigCarPlay-0.2.13.apk"},
              {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.13/SHA256SUMS.txt"}
            ]
          }
        ]
    """.trimIndent()

    @Test
    fun skipsDraftPrereleaseAndUnstableTags() {
        val release = UpdateCatalog.parse(releaseJson)
        assertEquals("v0.2.13", release?.tagName)
        assertEquals("TuPigCarPlay-0.2.13.apk", release?.apkName)
        assertEquals(
            "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.13/TuPigCarPlay-0.2.13.apk",
            release?.apkUrl,
        )
        assertEquals(
            "https://github.com/Tupig/TuPigPlay/releases/download/v0.2.13/SHA256SUMS.txt",
            release?.checksumsUrl,
        )
    }

    @Test
    fun skipsIncompleteAssetsAndKeepsLooking() {
        val json = """
            [
              {"tag_name": "v0.2.14", "draft": false, "prerelease": false, "assets": [
                {"name": "notes.txt", "browser_download_url": "https://example.com/notes.txt"}
              ]},
              {"tag_name": "v0.2.13", "draft": false, "prerelease": false, "assets": [
                {"name": "TuPigCarPlay-0.2.13.apk", "browser_download_url": "https://example.com/TuPigCarPlay-0.2.13.apk"},
                {"name": "SHA256SUMS.txt", "browser_download_url": "https://example.com/SHA256SUMS.txt"}
              ]}
            ]
        """.trimIndent()
        assertEquals("v0.2.13", UpdateCatalog.parse(json)?.tagName)
    }

    @Test
    fun returnsNullWithoutAnApkAsset() {
        val json = """
            [
              {"tag_name": "v0.2.14", "draft": false, "prerelease": false, "assets": [
                {"name": "source.zip", "browser_download_url": "https://example.com/source.zip"}
              ]}
            ]
        """.trimIndent()
        assertNull(UpdateCatalog.parse(json))
    }

    @Test
    fun returnsNullWithoutChecksums() {
        val json = """
            [
              {"tag_name": "v0.2.14", "draft": false, "prerelease": false, "assets": [
                {"name": "TuPigCarPlay-0.2.14.apk", "browser_download_url": "https://example.com/TuPigCarPlay-0.2.14.apk"}
              ]}
            ]
        """.trimIndent()
        assertNull(UpdateCatalog.parse(json))
    }

    @Test
    fun returnsNullForAnEmptyReleaseList() {
        assertNull(UpdateCatalog.parse("[]"))
    }
}
