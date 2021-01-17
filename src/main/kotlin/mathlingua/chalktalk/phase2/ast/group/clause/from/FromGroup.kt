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

package mathlingua.chalktalk.phase2.ast.group.clause.from

import mathlingua.chalktalk.phase1.ast.Phase1Node
import mathlingua.chalktalk.phase2.CodeWriter
import mathlingua.chalktalk.phase2.ast.DEFAULT_FROM_GROUP
import mathlingua.chalktalk.phase2.ast.DEFAULT_FROM_SECTION
import mathlingua.chalktalk.phase2.ast.DEFAULT_TO_SECTION
import mathlingua.chalktalk.phase2.ast.clause.Clause
import mathlingua.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.chalktalk.phase2.ast.group.clause.mapping.FromSection
import mathlingua.chalktalk.phase2.ast.group.clause.mapping.ToSection
import mathlingua.chalktalk.phase2.ast.group.clause.mapping.validateFromSection
import mathlingua.chalktalk.phase2.ast.group.clause.mapping.validateToSection
import mathlingua.chalktalk.phase2.ast.section.ensureNonNull
import mathlingua.chalktalk.phase2.ast.section.identifySections
import mathlingua.chalktalk.phase2.ast.track
import mathlingua.chalktalk.phase2.ast.validateGroup
import mathlingua.support.MutableLocationTracker
import mathlingua.support.ParseError

data class FromGroup(val fromSection: FromSection, val toSection: ToSection) : Clause {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        fn(fromSection)
        fn(toSection)
    }

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter) =
        mathlingua.chalktalk.phase2.ast.clause.toCode(writer, isArg, indent, fromSection, toSection)

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) =
        chalkTransformer(
            FromGroup(
                fromSection = fromSection.transform(chalkTransformer) as FromSection,
                toSection = toSection.transform(chalkTransformer) as ToSection))
}

fun isFromGroup(node: Phase1Node) = firstSectionMatchesName(node, "from")

fun validateFromGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    track(node, tracker) {
        validateGroup(node.resolve(), errors, "from", DEFAULT_FROM_GROUP) { group ->
            identifySections(group, errors, DEFAULT_FROM_GROUP, listOf("from", "to")) { sections ->
                FromGroup(
                    fromSection =
                        ensureNonNull(sections["from"], DEFAULT_FROM_SECTION) {
                            validateFromSection(it, errors, tracker)
                        },
                    toSection =
                        ensureNonNull(sections["to"], DEFAULT_TO_SECTION) {
                            validateToSection(it, errors, tracker)
                        })
            }
        }
    }
