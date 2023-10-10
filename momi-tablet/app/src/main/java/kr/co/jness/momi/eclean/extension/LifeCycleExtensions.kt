package kr.co.jness.momi.eclean.extension

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

operator fun CompositeDisposable.plusAssign(subscribe: Disposable?) {
    if (subscribe != null) {
        add(subscribe)
    }
}
