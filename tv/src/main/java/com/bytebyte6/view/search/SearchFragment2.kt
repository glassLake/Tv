package com.bytebyte6.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytebyte6.common.*
import com.bytebyte6.data.entity.Tv
import com.bytebyte6.utils.GridSpaceDecoration
import com.bytebyte6.utils.ListFragment
import com.bytebyte6.view.R
import com.bytebyte6.view.adapter.ButtonClickListener
import com.bytebyte6.view.adapter.TvAdapter
import com.bytebyte6.view.setupToolbarArrowBack
import com.bytebyte6.view.toPlayer
import com.bytebyte6.viewmodel.SearchViewModel2
import org.koin.android.viewmodel.ext.android.viewModel


class SearchFragment2 : ListFragment() {

    companion object {
        const val TAG = "SearchFragment2"
        fun newInstance(transName: String): SearchFragment2 {
            return SearchFragment2().apply {
                arguments = Bundle().apply {
                    putString(KEY_TRANS_NAME, transName)
                }
            }
        }
    }

    private val viewModel: SearchViewModel2 by viewModel()

    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doOnSharedElementReturnTransitionEnd {
            clearRecyclerView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.appbar?.toolbar?.let {
            val searchView = LayoutInflater.from(view.context)
                .inflate(R.layout.view_search, it, false)
            it.addView(searchView)
            etSearch = searchView.findViewById(R.id.etSearch)
            btnClear = searchView.findViewById(R.id.btnClear)
            etSearch.doOnTextChanged { text, _, _, _ ->
                viewModel.search(text)
                btnClear.isVisible = !text.isNullOrEmpty()
            }
            btnClear.setOnClickListener {
                etSearch.setText("")
            }
        }
        disEnabledSwipeRefreshLayout()
        setupToolbarArrowBack { KeyboardUtils.hideSoftInput(requireActivity()) }
        val adapter = TvAdapter(object : ButtonClickListener {
            override fun onClick(position: Int, tv: Tv) {
                viewModel.fav(position)
            }
        }).apply {
            onItemClick = { pos, _: View ->
                toPlayer(currentList[pos].url)
            }
        }
        imageClearHelper = adapter
        recyclerView = binding?.recyclerView

        binding?.apply {
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(GridSpaceDecoration())
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        KeyboardUtils.hideSoftInput(requireActivity())
                    }
                }
            })
        }

        KeyboardUtils.showSoftInput(etSearch, requireContext())

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer { result ->
            result.isSuccess()?.apply {
                adapter.currentList[pos].favorite = tv.favorite
                adapter.notifyItemChanged(pos)
            }
        })
        viewModel.searchResult.observe(viewLifecycleOwner, Observer { result ->
            result.emit({
                adapter.submitList(it.data.toList())
                end = it.end
                hideProgress()
            }, {
                hideProgress()
                view.longSnack(it.error.message.toString())
            }, {
                showProgress()
            })
        })

        viewModel.itemChanged.observe(viewLifecycleOwner, Observer { pos ->
            adapter.notifyItemChanged(pos)
        })

        viewModel.resultCount.observe(viewLifecycleOwner, Observer {
            val empty = it == 0
            binding?.emptyBox?.isVisible = empty
            recyclerView?.isVisible = !empty
        })
    }

    override fun onLoadMore() {
        viewModel.loadMore()
    }

    override fun onRefresh() = Unit
}