/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.board

import jp.toastkid.lib.ContentViewModel
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class TaskClearUseCase(
        private val tasks: MutableList<TodoTask>,
        private val contentViewModel: ContentViewModel,
        private val taskAddingUseCase: TaskAddingUseCase?,
        private val clearBoard: () -> Unit
) {
    operator fun invoke() {
        val keep = mutableListOf<TodoTask>().also {
            it.addAll(tasks)
        }
        tasks.clear()
        clearBoard()
        contentViewModel
                .snackWithAction("Clear all tasks.", "Undo") {
                    keep.forEach { taskAddingUseCase?.invoke(it) }
                }
    }
}