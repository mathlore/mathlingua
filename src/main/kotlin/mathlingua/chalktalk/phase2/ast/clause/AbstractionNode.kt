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

package mathlingua.chalktalk.phase2.ast.clause

import mathlingua.chalktalk.phase1.ast.Abstraction
import mathlingua.chalktalk.phase1.ast.Phase1Node
import mathlingua.chalktalk.phase2.ast.DEFAULT_ABSTRACTION
import mathlingua.chalktalk.phase2.ast.common.ZeroPartNode
import mathlingua.chalktalk.phase2.ast.neoTrack
import mathlingua.chalktalk.phase2.ast.neoValidateByTransform
import mathlingua.support.MutableLocationTracker
import mathlingua.support.ParseError

data class AbstractionNode(val abstraction: Abstraction) : ZeroPartNode(abstraction), Target

fun isAbstraction(node: Phase1Node) = node is Abstraction

fun validateAbstractionNode(node: Phase1Node, tracker: MutableLocationTracker) =
    validateWrappedNode(tracker, node, "AbstractionNode", { it as? Abstraction }, ::AbstractionNode)

fun neoValidateAbstractionNode(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    neoTrack(node, tracker) {
        neoValidateByTransform(
            node = node.resolve(),
            errors = errors,
            default = DEFAULT_ABSTRACTION,
            message = "Expected an abstraction",
            transform = { it as? Abstraction },
            builder = ::AbstractionNode)
    }
