package com.bytebyte6.usecase

import com.bytebyte6.common.RxUseCase
import com.bytebyte6.data.DataManager

class FavoriteTvUseCase(private val dataManager: DataManager) :
    RxUseCase<UpdateTvParam, UpdateTvParam>() {
    override fun run(param: UpdateTvParam): UpdateTvParam {
        val tv = dataManager.getTvById(param.tv.tvId)
        tv.favorite = !tv.favorite
        dataManager.updateTv(tv)
        param.tv = tv
        return param
    }
}