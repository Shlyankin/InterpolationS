package com.heads.thinking.interpolations

import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.heads.thinking.interpolations.ui.main.InterpolationFragment
import com.heads.thinking.interpolations.ui.main.MainFragment
import com.heads.thinking.interpolations.ui.main.MainViewModel

class MainActivity : AppCompatActivity(), MainFragment.OnFragmentInteractionListener, ImageViewerFragment.OnFragmentInteractionListener, InterpolationFragment.OnFragmentInteractionListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var currentFragmentName: String
    private lateinit var currentFragment: Fragment

    override fun onFragmentBackButtonClick() {
        if(currentFragmentName == "InterpolationFragment")
            startFragment(MainFragment.newInstance())
        else if(currentFragmentName == "ImageViewerFragment")
            startFragment(InterpolationFragment.newInstance())
    }

    override fun onCalculateInterpolationClick() {
        startFragment(InterpolationFragment.newInstance())
    }

    override fun onFragmentFullscreenGraphClick() {
        startFragment(ImageViewerFragment.newInstance())
    }

    private fun startFragment(fragment : Fragment) {
        currentFragment = fragment
        if(fragment is MainFragment) {
            fragment.setOnFragmentInteractionListener(this@MainActivity)
            currentFragmentName = "MainFragment"
        }
        if(fragment is InterpolationFragment) {
            fragment.setOnFragmentInteractionListener(this@MainActivity)
            currentFragmentName = "InterpolationFragment"
        }
        if(fragment is ImageViewerFragment) {
            fragment.setOnFragmentInteractionListener(this@MainActivity)
            currentFragmentName = "ImageViewerFragment"
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        if(viewModel.currentFragmentName != null) {
            when(viewModel.currentFragmentName) {
                "MainFragment" -> {
                    startFragment(MainFragment.newInstance())
                }
                "InterpolationFragment" -> {
                    startFragment(InterpolationFragment.newInstance())
                }
                "ImageViewerFragment" -> {
                    startFragment(ImageViewerFragment.newInstance())
                }
            }
        } else {
            startFragment(MainFragment.newInstance())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.currentFragmentName = currentFragmentName
    }

    override fun onBackPressed() {
        if(currentFragmentName == "ImageViewerFragment") {
            startFragment(InterpolationFragment.newInstance())
        } else if(currentFragmentName == "InterpolationFragment") {
            startFragment(MainFragment.newInstance())
        } else if(currentFragmentName == "MainFragment") {
                AlertDialog.Builder(this)
                        .setTitle("Вы действительно хотите выйти из приложения?")
                        .setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int -> super.onBackPressed() })
                        .setNegativeButton("No", null)
                        .create()
                        .show()
        }
    }
}
