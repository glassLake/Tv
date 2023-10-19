package com.bytebyte6.view.player

import android.graphics.Color
import android.os.Bundle
import com.blankj.utilcode.util.BarUtils
import com.bytebyte6.common.BaseActivity
import com.bytebyte6.view.R
import com.bytebyte6.view.replaceNotAddToBackStack

class PlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        if (savedInstanceState == null) {
            replaceNotAddToBackStack(
                PlayerFragment.newInstance(intent.extras!!), PlayerFragment.TAG
            )
        }
        BarUtils.setNavBarColor(this, Color.BLACK)
        BarUtils.setNavBarVisibility(this,false)
    }
}