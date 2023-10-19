package com.bytebyte6.view.player

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bytebyte6.common.BaseShareFragment
import com.bytebyte6.common.EventObserver
import com.bytebyte6.view.KEY_CACHE
import com.bytebyte6.view.KEY_VIDEO_URL
import com.bytebyte6.view.R
import com.bytebyte6.view.databinding.FragmentVideoBinding
import com.bytebyte6.viewmodel.PlayerViewModel
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.offline.DownloadRequest
import org.koin.android.viewmodel.ext.android.viewModel


class PlayerFragment : BaseShareFragment<FragmentVideoBinding>(R.layout.fragment_video) {

    companion object {
        const val TAG: String = "PlayerFragment"
        fun newInstance(bundle: Bundle) = PlayerFragment().apply {
            arguments = bundle
        }
    }

    private val viewModel by viewModel<PlayerViewModel>()

    private var errorDialog: AlertDialog? = null

    private var mobileDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = requireArguments().getParcelable(KEY_CACHE) as DownloadRequest?
        val url = requireArguments().getString(KEY_VIDEO_URL)!!
        viewModel.downloadRequest = request
        viewModel.url = url
    }

    override fun initViewBinding(view: View): FragmentVideoBinding = FragmentVideoBinding.bind(view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.playerView?.setShowPreviousButton(false)
        binding?.playerView?.setShowNextButton(false)
        viewModel.showMobileDialog.observe(viewLifecycleOwner, EventObserver {
            showMobileDialog()
        })
        viewModel.play.observe(viewLifecycleOwner, Observer {
            play(it)
        })
        viewModel.onPlayerError.observe(viewLifecycleOwner, EventObserver {
            val tip = when (it.type) {
                ExoPlaybackException.TYPE_OUT_OF_MEMORY ->
                    getString(R.string.out_of_memory)
                ExoPlaybackException.TYPE_SOURCE ->
                    getString(R.string.source_error)
                ExoPlaybackException.TYPE_TIMEOUT ->
                    getString(R.string.timeout)
                else -> it.message.toString()
            }
            showErrorDialog(tip)
        })
        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer {
            binding?.progressBar?.isVisible = it
        })
        viewModel.showNoneDialog.observe(viewLifecycleOwner, EventObserver {
            showNoneDialog()
        })
    }

    private fun showErrorDialog(tip: String) {
        if (errorDialog != null) {
            return
        }
        errorDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.tip)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                errorDialog = null
                requireActivity().finish()
            }
            .setPositiveButton(R.string.tip_play_retry) { dialog, _ ->
                dialog.dismiss()
                errorDialog = null
                viewModel.retry()
            }
            .setCancelable(false)
            .setMessage(tip)
            .create()
        errorDialog?.show()
    }

    private val handler by lazy {
        Handler()
    }

    private fun showNoneDialog() {
        // 有时候是 网络切换 短暂丢网
        handler.postDelayed({
            if (!viewModel.networkIsConnected()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.tip)
                    .setMessage(R.string.network_disconnected)
                    .setPositiveButton(R.string.enter) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }, 1000)
    }

    private fun showMobileDialog() {
        if (mobileDialog != null) {
            return
        }
        val player = binding?.playerView?.player
        player?.pause()
        mobileDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.tip)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                mobileDialog = null
                requireActivity().finish()
            }
            .setPositiveButton(R.string.enter) { dialog, _ ->
                dialog.dismiss()
                mobileDialog = null
                viewModel.playOrInit()
            }
            .setMessage(R.string.tip_play_confirm)
            .setCancelable(false)
            .create()
        mobileDialog?.show()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) {
            viewModel.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23) {
            viewModel.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            viewModel.onStop()
            stop()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            viewModel.onStop()
            stop()
        }
    }

    private fun play(player: Player) {
        binding?.apply {
            playerView.player = player
            playerView.keepScreenOn = true
            playerView.onResume()
        }
    }

    private fun stop() {
        binding?.apply {
            playerView.keepScreenOn = false
            playerView.onPause()
        }
    }

    override fun onDestroyView() {
        binding?.playerView?.player = null
        super.onDestroyView()
    }
}