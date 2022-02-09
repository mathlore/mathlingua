/*
 * Copyright 2022 The MathLingua Authors
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

package mathlingua.frontend.chalktalk.phase2.ast.group.toplevel.specify.zero

import mathlingua.frontend.chalktalk.phase1.ast.Phase1Node
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_IS_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_ZERO_GROUP
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_ZERO_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.frontend.chalktalk.phase2.ast.common.TwoPartNode
import mathlingua.frontend.chalktalk.phase2.ast.group.toplevel.specify.IsSection
import mathlingua.frontend.chalktalk.phase2.ast.group.toplevel.specify.NumberGroup
import mathlingua.frontend.chalktalk.phase2.ast.group.toplevel.specify.validateIsSection
import mathlingua.frontend.chalktalk.phase2.ast.section.ensureNonNull
import mathlingua.frontend.chalktalk.phase2.ast.section.identifySections
import mathlingua.frontend.chalktalk.phase2.ast.track
import mathlingua.frontend.chalktalk.phase2.ast.validateGroup
import mathlingua.frontend.support.MutableLocationTracker
import mathlingua.frontend.support.ParseError

internal data class ZeroGroup(val zeroSection: ZeroSection, val isSection: IsSection) :
    TwoPartNode<ZeroSection, IsSection>(zeroSection, isSection, ::ZeroGroup), NumberGroup

internal fun isZeroGroup(node: Phase1Node) = firstSectionMatchesName(node, "zero")

internal fun validateZeroGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    track(node, tracker) {
        validateGroup(node.resolve(), errors, "zero", DEFAULT_ZERO_GROUP) { group ->
            identifySections(group, errors, DEFAULT_ZERO_GROUP, listOf("zero", "is")) { sections ->
                ZeroGroup(
                    zeroSection =
                        ensureNonNull(sections["zero"], DEFAULT_ZERO_SECTION) {
                            validateZeroSection(it, errors, tracker)
                        },
                    ensureNonNull(sections["is"], DEFAULT_IS_SECTION) {
                        validateIsSection(it, errors, tracker)
                    })
            }
        }
    }
