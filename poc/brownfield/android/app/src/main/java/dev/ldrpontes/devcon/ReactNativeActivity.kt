package dev.ldrpontes.devcon

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.callstack.reactnativebrownfield.ReactNativeFragment

class ReactNativeActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = FrameLayout(this).apply { id = View.generateViewId() }
        setContentView(container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(container.id, ReactNativeFragment.createReactNativeFragment("devconrn"))
                .commit()
        }
    }
}
