package com.bytebyte6.usecase.work

import android.content.Context
import androidx.work.*

/***
 * 搜索国旗和电视台logo的图片链接
 */
class FindImageWork(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
     var searchCountryImage: SearchCountryImage? = null;
     var searchTvLogo: SearchTvLogo? = null
    override fun doWork(): Result {
        try {
            searchCountryImage?.searchCountryImage()
            searchTvLogo?.searchLogo()
        } catch (e: Throwable) {
            return Result.failure()
        }
        return Result.success()
    }
}

class AppDelegatingWorkerFactory(
    searchCountryImage: SearchCountryImage,
    searchTvLogo: SearchTvLogo
) : DelegatingWorkerFactory() {
    init {
        addFactory(GetCountryFactory(searchCountryImage, searchTvLogo))
    }
}

class GetCountryFactory(
    private val searchCountryImage: SearchCountryImage,
    private val searchTvLogo: SearchTvLogo
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        var work =  FindImageWork(
            context = appContext,
            workerParams = workerParameters
        )
        work.searchCountryImage = searchCountryImage
        work.searchTvLogo = searchTvLogo
        return work
    }
}