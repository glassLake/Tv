package com.bytebyte6.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bytebyte6.common.baseModule
import com.bytebyte6.common.getSuccessData
import com.bytebyte6.common.isLoading

import com.bytebyte6.data.dataModule
import com.bytebyte6.usecase.DownloadListUseCase
import com.bytebyte6.usecase.useCaseModule
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
class DownloadViewModelTest : AutoCloseKoinTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun start() {
        stopKoin()
        startKoin {
            modules(
                roomMemoryModule, dataModule, useCaseModule,
                viewModule, testExoPlayerModule, baseModule
            )
        }
        RxJavaPlugins.setIoSchedulerHandler {
            Schedulers.trampoline()
        }
    }

    @Test
    fun test_pauseInterval() {
        val viewModel = getViewModel()
        var loading=false
        viewModel.downloadListResult.observeForever {
            if (it.isLoading()){
                loading=true
            }
        }
        viewModel.pauseInterval()
        assert(loading)
        assert(viewModel.downloadListResult.getSuccessData()!!.isEmpty())
        assert(!viewModel.isStartInterval())
    }

    @Test
    fun test_startInterval(){
        val viewModel=getViewModel()
        viewModel.startInterval()
        assert(!viewModel.isStartInterval())
    }

    @Test
    fun test_loadDownloadList() {
        val viewModel = getViewModel()
        var loading=false
        viewModel.downloadListResult.observeForever {
            if (it.isLoading()){
                loading=true
            }
        }
        viewModel.loadDownloadList()
        assert(loading)
        assert(viewModel.downloadListResult.getSuccessData()!!.isEmpty())
        assert(!viewModel.isStartInterval())
    }

    private fun getViewModel(): DownloadViewModel {
        return DownloadViewModel(
            get(),
            DownloadListUseCase(get(), get())
        )
    }
}