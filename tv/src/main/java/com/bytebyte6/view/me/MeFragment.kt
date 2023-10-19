package com.bytebyte6.view.me

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import com.bytebyte6.common.*
import com.bytebyte6.utils.LinearSpaceDecoration
import com.bytebyte6.view.*
import com.bytebyte6.view.R
import com.bytebyte6.view.adapter.PlaylistAdapter
import com.bytebyte6.view.databinding.FragmentMeBinding
import com.bytebyte6.view.main.MainActivity
import com.bytebyte6.viewmodel.MeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.koin.android.viewmodel.ext.android.viewModel


/***
 * 导入
 * 1、本地导入ok
 * 2、可从网络链接导入todo
 */
class MeFragment : BaseShareFragment<FragmentMeBinding>(R.layout.fragment_me) {

    companion object {
        const val TAG = "MeFragment"
    }

    private val viewModel: MeViewModel by viewModel()

    private lateinit var launcher: ActivityResultLauncher<String>

    private val listener = object : SimpleDrawerListener() {
        override fun onDrawerClosed(drawerView: View) {
            binding?.emptyBox?.resumeAnimation()
        }
    }

    override fun initViewBinding(view: View): FragmentMeBinding {
        return FragmentMeBinding.bind(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = ActivityResultCallback<Uri?> { result ->
            if (result != null) {
                viewModel.parseM3u(result)
            }
        }
        val getContent = ActivityResultContracts.GetContent()
        launcher = registerForActivityResult(getContent, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarMenuMode { binding?.emptyBox?.pauseAnimation() }

        val drawerHelper = (requireActivity() as MainActivity).drawerHelper
        drawerHelper.addDrawerListener(listener)

        doOnExitTransitionEndOneShot { clearRecyclerView() }
        setupOnBackPressedDispatcherBackToHome()

        val meBinding = binding!!

        //init recyclerview
        val playlistAdapter = PlaylistAdapter()
        playlistAdapter.onItemClick = { pos, itemView ->
            meToPlaylist(
                playlistAdapter.list[pos].playlistId,
                playlistAdapter.list[pos].playlistName,
                itemView
            )
        }
        this.recyclerView = meBinding.recyclerView
        meBinding.recyclerView.adapter = playlistAdapter
        meBinding.recyclerView.addItemDecoration(LinearSpaceDecoration())
        meBinding.recyclerView.setHasFixedSize(true)
        playlistAdapter.setupSelectionTracker(meBinding.recyclerView,
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val hasSelection = playlistAdapter.selectionTracker!!.hasSelection()
                    if (hasSelection) {
                        binding?.fab?.show()
                    } else {
                        binding?.fab?.hide()
                    }
                }
            })
        val selectionTracker = playlistAdapter.selectionTracker!!

        meBinding.toolbar.setOnMenuItemClickListener {
            launcher.launch("*/*")
            true
        }
        meBinding.fab.setOnClickListener {
            if (!selectionTracker.selection.isEmpty) {
                showDialog(selectionTracker)
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner, {
            it.emitIfNotHandled({
                binding?.fab?.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                    override fun onHidden(fab: FloatingActionButton?) {
                        showSnackBar(R.string.tip_del_success)
                    }
                })
                hideProgressBar()
                selectionTracker.clearSelection()
            }, {
                hideProgressBar()
                showSnackBar(R.string.tip_del_fail)
            }, {
                showProgressBar()
            })
        })

        viewModel.playlists.observe(viewLifecycleOwner, {
            playlistAdapter.replace(it)
            meBinding.emptyBox.isVisible = it.isEmpty()
        })

        viewModel.parseResult.observe(viewLifecycleOwner, { result ->
            result.emitIfNotHandled({
                hideProgressBar()
            }, {
                hideProgressBar()
                showSnackBar(0,getString(R.string.tip_parse_file_error, it.error.message.toString()))
            }, {
                showProgressBar()
            })
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val drawerHelper = (requireActivity() as MainActivity).drawerHelper
        drawerHelper.removeDrawerListener(listener)
    }

    private fun showDialog(selectionTracker: SelectionTracker<Long>) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.tip_del_playlist))
            .setMessage(getString(R.string.tip_beyond_retrieve))
            .setPositiveButton(getString(R.string.enter)) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                viewModel.delete(selectionTracker.selection)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun showSnackBar(messageId: Int, message: String? = null) {
        binding?.apply {
            if (fab.isVisible) {
                val actionSetup: Snackbar.() -> Unit = {
                    anchorView = fab
                }
                val longSnack = if (message == null)
                    fab.longSnack(messageId, actionSetup)
                else
                    fab.longSnack(message, actionSetup)
                //memory leak
                longSnack.addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        transientBottomBar?.anchorView = null
                        longSnack.removeCallback(this)
                    }
                })
            } else {
                if (message == null) {
                    requireView().longSnack(messageId)
                } else {
                    requireView().longSnack(message)
                }
            }
        }
    }

    private fun showProgressBar() {
        binding?.apply {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding?.apply {
            progressBar.visibility = View.GONE
        }
    }
}