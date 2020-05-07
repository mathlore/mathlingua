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

import mathlingua.common.MutableLocationTracker
import mathlingua.common.chalktalk.phase1.ast.Mapping
import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase2.CodeWriter
import mathlingua.common.chalktalk.phase2.ast.Phase2Node

data class MappingNode(val mapping: Mapping) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {}

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter) = toCode(writer, isArg, indent, mapping)

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) = chalkTransformer(this)
}

fun isMapping(node: Phase1Node) = node is Mapping

fun validateMappingNode(node: Phase1Node, tracker: MutableLocationTracker) = validateWrappedNode(
        tracker,
        node,
        "MappingNode",
        { it as? Mapping },
        ::MappingNode
)
