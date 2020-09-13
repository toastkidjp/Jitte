/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.addition

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragmentUseCase(
        private val viewLifecycleOwner: Fragment,
        private val taskConsumer: (TodoTask) -> Unit
) {

    operator fun invoke(currentTask: TodoTask? = null) {
        val taskAdditionDialogFragment = TaskAdditionDialogFragment.make(currentTask)
        taskAdditionDialogFragment.setTargetFragment(viewLifecycleOwner, 1)
        ViewModelProvider(viewLifecycleOwner).get(TaskAdditionDialogFragmentViewModel::class.java)
                .refresh
                .observe(viewLifecycleOwner, Observer {
                    val task = it?.getContentIfNotHandled() ?: return@Observer
                    taskConsumer(task)
                })
        taskAdditionDialogFragment.show(viewLifecycleOwner.parentFragmentManager, javaClass.canonicalName)
    }
}