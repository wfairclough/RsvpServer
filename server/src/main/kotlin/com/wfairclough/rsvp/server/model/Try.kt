package com.wfairclough.rsvp.server.model

sealed class Try<out F, out S>
data class Success<S>(val value: S): Try<Nothing, S>()
data class Failure<F>(val value: F): Try<F, Nothing>()

inline fun <L, R, T> Try<L, R>.fold(failure: (L) -> T, success: (R) -> T): T =
        when (this) {
            is Failure -> failure(value)
            is Success -> success(value)
        }

inline fun <L, R> Try<L, R>.orElse(failure: (L) -> R): R =
    fold(failure, { r: R -> r })

inline fun <L, R, T> Try<L, R>.flatMap(f: (R) -> Try<L, T>): Try<L, T> =
        fold({ this as Failure }, f)

inline fun <L, R, T> Try<L, R>.map(f: (R) -> T): Try<L, T> =
        flatMap { Success(f(it)) }


inline fun <T> tryCatch(action: () -> T): Try<Throwable, T> {
    try {
        return Success(action())
    } catch (t: Throwable) {
        return Failure(t)
    }
}