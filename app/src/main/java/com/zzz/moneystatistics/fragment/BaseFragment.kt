package com.zzz.moneystatistics.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    private val inspectLifeCycle = false
    override fun onResume() {
        super.onResume()
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onResume")
    }

    override fun onStart() {
        super.onStart()
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onStart")
    }

    override fun onDetach() {
        super.onDetach()
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onDetach")
    }

    override fun onStop() {
        super.onStop()
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onDestroy")
    }

    override fun onPause() {
        super.onPause()
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onPause")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (inspectLifeCycle)
            Log.d(javaClass.name, "onCreate")
    }
}