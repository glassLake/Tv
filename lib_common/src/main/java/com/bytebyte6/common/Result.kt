package com.bytebyte6.common

import androidx.lifecycle.LiveData

sealed class Result<out R> {
    data class Success<out T>(
        val data: T,
        /**加载更多的情况下使用表示数据全部加载完成*/
        val end: Boolean = false,
        var handled: Boolean = false
    ) : Result<T>()

    data class Error(
        val error: Throwable,
        var handled: Boolean = false
    ) : Result<Nothing>()

    object Loading : Result<Nothing>()
}

/**
 * Result 搭配 LiveData使用时，当配置更改或其他情况会导致LiveData重新订阅，所以定义handled变量来处理
 * 这种情况，一次性的事件，但是success的情况是有例外的，比如展示数据（在配置更改后，数据要继续显示，
 * Ui事件不需再次执行），所以应该按情况自行调用runIfNotHandled()
 * @param success 成功回调，可能会执行多次
 * @param error 错误回调，只执行一次
 * @param loading show loading
 */
inline fun <T> Result<T>.emit(
    success: ((s: Result.Success<T>) -> Unit),
    error: ((e: Result.Error) -> Unit),
    loading: ((l: Result.Loading) -> Unit)
) {
    when (this) {
        is Result.Success -> success(this)
        is Result.Error -> runIfNotHandled { error(this) }
        is Result.Loading -> loading(this)
    }
}

inline fun <T> Result<T>.emitIfNotHandled(
    success: ((s: Result.Success<T>) -> Unit),
    error: ((e: Result.Error) -> Unit),
    loading: ((l: Result.Loading) -> Unit)
) {
    when (this) {
        is Result.Success -> runIfNotHandled { success(this) }
        is Result.Error -> runIfNotHandled { error(this) }
        is Result.Loading -> loading(this)
    }
}

inline fun <T> Result.Success<T>.runIfNotHandled(doSomething: (() -> Unit)) {
    if (!this.handled) {
        handled = true
        doSomething()
    }
}

inline fun Result.Error.runIfNotHandled(doSomething: (() -> Unit)) {
    if (!this.handled) {
        handled = true
        doSomething()
    }
}

fun <T> Result<T>.isEnd(): Boolean {
    return when (this) {
        is Result.Success -> this.end
        else -> false
    }
}

fun <T> Result<T>.isSuccess(): T? {
    return when (this) {
        is Result.Success -> data
        else -> null
    }
}

fun <T> Result<T>.isLoading(): Boolean {
    return when (this) {
        is Result.Loading -> true
        else -> false
    }
}

fun <T> Result<T>.isError(): Throwable? {
    return when (this) {
        is Result.Error -> error
        else -> null
    }
}

fun <T> LiveData<Result<T>>.getSuccessData() = this.value?.isSuccess()

fun <T> LiveData<Result<T>>.end() = if (value == null) false else this.value!!.isEnd()

fun <T> LiveData<Result<T>>.getError() = this.value?.isError()