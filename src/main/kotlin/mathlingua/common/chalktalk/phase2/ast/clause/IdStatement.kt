/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mathlingua.common.chalktalk.phase2.ast.clause

import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase2.CodeWriter
import mathlingua.common.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.common.support.MutableLocationTracker
import mathlingua.common.support.Validation
import mathlingua.common.support.ValidationFailure
import mathlingua.common.support.ValidationSuccess
import mathlingua.common.support.validationFailure
import mathlingua.common.support.validationSuccess
import mathlingua.common.textalk.ExpressionTexTalkNode

data class IdStatement(
    val text: String,
    val texTalkRoot: Validation<ExpressionTexTalkNode>
) : Clause {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {}

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter): CodeWriter {
        writer.writeIndent(isArg, indent)
        writer.writeId(this)
        return writer
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) = chalkTransformer(this)

    fun toStatement() = Statement(
            text = text,
            texTalkRoot = texTalkRoot
    )
}

fun validateIdStatement(rawNode: Phase1Node, tracker: MutableLocationTracker): Validation<IdStatement> =
        when (val validation = validateStatement(rawNode, tracker)) {
            is ValidationSuccess -> validationSuccess(tracker, rawNode, IdStatement(
                    text = validation.value.text,
                    texTalkRoot = validation.value.texTalkRoot
            ))
            is ValidationFailure -> validationFailure(validation.errors)
        }
