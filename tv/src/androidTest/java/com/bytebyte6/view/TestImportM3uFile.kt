package com.bytebyte6.view

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.bytebyte6.view.main.MainActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 测试导入m3u文件功能
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TestImportM3uFile {

    companion object {
        const val AQY = "aqy.m3u"
        const val INDEX = "index.m3u"
        const val CHINA = "china.m3u8"
    }

    private val applicationContext = ApplicationProvider.getApplicationContext<Context>()
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun setup() {
        device.pressHome()
        // 启动活动
        val intent = Intent(applicationContext, MainActivity::class.java)
        applicationContext.startActivity(intent)
    }

    @Test
    fun testCHINA() {
        test(CHINA)
    }

    @Test
    fun testINDEX() {
        test(INDEX)
    }

    @Test
    fun testAQY() {
        test(AQY)
    }

    private fun test(fileName: String) {
        // 点击菜单
        val desc = applicationContext.getString(R.string.toolbar_navigation)
        val navButton = device.findObject(UiSelector().description(desc))
        navButton.click()

        // 点击导入
        val importText = applicationContext.getString(R.string.nav_import)
        val menuImport = device.findObject(By.text(importText))
        menuImport.click()

        // 点击加号导入
        val importButton =
            device.findObject(UiSelector().resourceId("com.bytebyte6.rtmp:id/me_import_file"))
        importButton.clickAndWaitForNewWindow()

        // 根据文件名找到组件并点击
        // 注意AndroidX
        val fileRecyclerView =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                UiScrollable(UiSelector().className("android.support.v7.widget.RecyclerView"))
            else
                UiScrollable(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
        val child = fileRecyclerView.getChildByText(
            UiSelector().className("android.widget.RelativeLayout"),
            fileName
        )
        child.clickAndWaitForNewWindow()

        // 等待loading消失
        val progressBar =
            device.findObject(UiSelector().resourceId("com.bytebyte6.rtmp:id/progressBar"))
        progressBar.waitUntilGone(30000)

//        val toolbar = device.findObject(UiSelector().resourceId("com.bytebyte6.rtmp:id/toolbar"))
//        val title = toolbar.getChild(UiSelector().index(1))
//        assert(title.text.contains(fileName))

        val recyclerView = device.findObject(UiSelector().className(RecyclerView::class.java))
        val child1 = recyclerView.getChild(UiSelector().text(fileName))
        assert(child1 != null)
    }
}