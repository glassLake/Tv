package com.bytebyte6.usecase

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.bytebyte6.common.Result
import com.bytebyte6.common.RxUseCase2
import com.bytebyte6.common.loge
import com.bytebyte6.data.DataManager
import com.bytebyte6.data.TypeConverter
import com.bytebyte6.data.entity.Category
import com.bytebyte6.data.entity.Country
import com.bytebyte6.data.entity.Language
import com.bytebyte6.data.entity.User
import com.bytebyte6.usecase.work.FindImageWork
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface InitAppUseCase : RxUseCase2<Unit, User>

class InitAppUseCaseImpl(
    private val dataManager: DataManager,
    private val context: Context,
    private val gson: Gson
) : InitAppUseCase {

    override val result: MutableLiveData<Result<User>> = MutableLiveData()

    override fun run(param: Unit): User {

        initCountryData()

        initCategoryData()

        initLanguageData()

        initTestData()

        return initUser()
    }

    private fun initUser(): User {
        val user = dataManager.getCurrentUserIfNotExistCreate()
        if (user.capturePic) {
            findImageLink()
        }
        return user
    }

    /**
     * 上传到Firebase测试用到
     */
    private fun initTestData() {
        try {
            if (dataManager.getTvCount() == 0) {
                val parseM3uUseCase = ParseM3uUseCase(dataManager, context)
                parseM3uUseCase.run(ParseParam(assetsFileName = "index.m3u"))
            }
        } catch (e: Exception) {
            loge("for build type labtest,if error ignore!")
        }
    }

    private fun initCategoryData() {
        if (dataManager.getCategoryCount() == 0) {
            val json = context.assets.open("categories.json")
                .bufferedReader()
                .use { it.readText() }
            val categories: List<Category> =
                gson.fromJson(json, TypeConverter.typeCategory)
            dataManager.insertCategory(categories)
        }
    }

    private fun initLanguageData() {
        if (dataManager.getLangCount() == 0) {
            val json = context.assets.open("languages.json")
                .bufferedReader()
                .use { it.readText() }
            val languages: List<Language> =
                gson.fromJson(json, TypeConverter.type)
            dataManager.insertLanguages(languages)
        }
    }

    private fun initCountryData() {
        if (dataManager.getCountryCount() == 0) {
            val json = context.assets.open("countries.json")
                .bufferedReader()
                .use { it.readText() }
            val cs: MutableList<Country> =
                gson.fromJson(json, object : TypeToken<List<Country>>() {}.type)
            cs.add(
                Country(
                    name = Country.UNSORTED,
                    nameChinese = "未知",
                    code = Country.UNSORTED_LOW
                )
            )
            dataManager.insertCountry(cs)
        }
    }

    private fun findImageLink() {
        val workRequest = OneTimeWorkRequestBuilder<FindImageWork>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(context)
            .beginUniqueWork("FindImageLink", ExistingWorkPolicy.KEEP, workRequest)
            .enqueue()
    }
}

