/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class MenuViewModel : ViewModel() {
    private val _click = MutableLiveData<Menu>()
    private val _longClick = MutableLiveData<Menu>()
    private val _visibility = MutableLiveData<Boolean>()
    private val _onResume = MutableLiveData<Unit>()

    val click: LiveData<Menu> = _click
    val longClick: LiveData<Menu> = _longClick
    val visibility: LiveData<Boolean> = _visibility
    val onResume: LiveData<Unit> = _onResume

    fun click(menu: Menu) {
        _click.postValue(menu)
    }

    fun longClick(menu: Menu) {
        _longClick.postValue(menu)
    }

    fun onResume() {
        _onResume.postValue(Unit)
    }

    fun switchVisibility(state: Boolean) {
        _visibility.postValue(state)
    }

    fun close() {
        _visibility.postValue(false)
    }
}