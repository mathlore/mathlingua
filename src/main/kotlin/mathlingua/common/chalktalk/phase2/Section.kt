/*
 * Copyright 2019 Google LLC
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

package mathlingua.common.chalktalk.phase2

import mathlingua.common.ParseError
import mathlingua.common.Validation
import mathlingua.common.ValidationFailure
import mathlingua.common.ValidationSuccess
import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase1.ast.Section
import mathlingua.common.chalktalk.phase1.ast.getColumn
import mathlingua.common.chalktalk.phase1.ast.getRow

private fun appendTargetArgs(builder: StringBuilder, targets: List<Target>, indent: Int) {
    for (i in targets.indices) {
        builder.append(targets[i].toCode(true, indent))
        if (i != targets.size - 1) {
            builder.append('\n')
        }
    }
}

data class AssumingSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "assuming:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(AssumingSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateAssumingSection(node: Phase1Node): Validation<AssumingSection> {
    return validateClauseList(
        node,
        "assuming"
    ) { AssumingSection(it) }
}

data class DefinesSection(val targets: List<Target>) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        targets.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "Defines:"))
        builder.append('\n')
        appendTargetArgs(builder, targets, indent + 2)
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(DefinesSection(
            targets = targets.map { it.transform(chalkTransformer) as Target }
        ))
    }
}

fun validateDefinesSection(node: Phase1Node): Validation<DefinesSection> {
    return validateTargetList(
        node,
        "Defines"
    ) { DefinesSection(it) }
}

data class RefinesSection(val targets: List<Target>) :
    Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        targets.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "Refines:"))
        builder.append('\n')
        appendTargetArgs(builder, targets, indent + 2)
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(RefinesSection(
            targets = targets.map { it.transform(chalkTransformer) as Target }
        ))
    }
}

fun validateRefinesSection(node: Phase1Node): Validation<RefinesSection> {
    return validateTargetList(
        node,
        "Refines"
    ) { RefinesSection(it) }
}

class RepresentsSection : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        return indentedString(isArg, indent, "Represents:")
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(this)
    }
}

fun validateRepresentsSection(node: Phase1Node): Validation<RepresentsSection> {
    val errors = ArrayList<ParseError>()
    if (node !is Section) {
        errors.add(
            ParseError(
                "Expected a RepresentsSection",
                getRow(node), getColumn(node)
            )
        )
    }

    val sect = node as Section
    if (sect.args.isNotEmpty()) {
        errors.add(
            ParseError(
                "A Represents cannot have any arguments",
                getRow(node), getColumn(node)
            )
        )
    }

    if (sect.name.text != "Represents") {
        errors.add(
            ParseError(
                "Expected a section named Represents",
                getRow(node), getColumn(node)
            )
        )
    }

    return if (errors.isNotEmpty()) {
        ValidationFailure(errors)
    } else {
        ValidationSuccess(RepresentsSection())
    }
}

data class ExistsSection(val identifiers: List<Target>) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        identifiers.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "exists:"))
        builder.append('\n')
        appendTargetArgs(builder, identifiers, indent + 2)
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ExistsSection(
            identifiers = identifiers.map { it.transform(chalkTransformer) as Target }
        ))
    }
}

fun validateExistsSection(node: Phase1Node): Validation<ExistsSection> {
    return validateTargetList(
        node,
        "exists"
    ) { ExistsSection(it) }
}

data class ForSection(val targets: List<Target>) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        targets.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "for:"))
        builder.append('\n')
        appendTargetArgs(builder, targets, indent + 2)
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ForSection(
            targets = targets.map { it.transform(chalkTransformer) as Target }
        ))
    }
}

fun validateForSection(node: Phase1Node): Validation<ForSection> {
    return validateTargetList(
        node,
        "for"
    ) { ForSection(it) }
}

data class MeansSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        fn(clauses)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "means:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(MeansSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateMeansSection(node: Phase1Node): Validation<MeansSection> {
    return validateClauseList(
        node,
        "means"
    ) { MeansSection(it) }
}

data class ResultSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "Result:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ResultSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateResultSection(node: Phase1Node): Validation<ResultSection> {
    return validateClauseList(
        node,
        "Result"
    ) { ResultSection(it) }
}

data class AxiomSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "Axiom:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(AxiomSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateAxiomSection(node: Phase1Node): Validation<AxiomSection> {
    return validateClauseList(
        node,
        "Axiom"
    ) { AxiomSection(it) }
}

data class ConjectureSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "Conjecture:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ConjectureSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateConjectureSection(node: Phase1Node): Validation<ConjectureSection> {
    return validateClauseList(
        node,
        "Conjecture"
    ) { ConjectureSection(it) }
}

data class SuchThatSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "suchThat:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(SuchThatSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateSuchThatSection(node: Phase1Node): Validation<SuchThatSection> {
    return validateClauseList(
        node,
        "suchThat"
    ) { SuchThatSection(it) }
}

data class ThatSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "that:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ThatSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateThatSection(node: Phase1Node): Validation<ThatSection> {
    return validateClauseList(
        node,
        "that"
    ) { ThatSection(it) }
}

data class IfSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "if:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(IfSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateIfSection(node: Phase1Node): Validation<IfSection> {
    return validateClauseList(
        node,
        "if"
    ) { IfSection(it) }
}

data class IffSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "iff:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(IffSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateIffSection(node: Phase1Node): Validation<IffSection> {
    return validateClauseList(
        node,
        "iff"
    ) { IffSection(it) }
}

data class ThenSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "then:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ThenSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateThenSection(node: Phase1Node): Validation<ThenSection> {
    return validateClauseList(
        node,
        "then"
    ) { ThenSection(it) }
}

data class WhereSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "where:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(WhereSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateWhereSection(node: Phase1Node): Validation<WhereSection> {
    return validateClauseList(
        node,
        "where"
    ) { WhereSection(it) }
}

data class NotSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "not:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(NotSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateNotSection(node: Phase1Node): Validation<NotSection> {
    return validateClauseList(
        node,
        "not"
    ) { NotSection(it) }
}

data class OrSection(val clauses: ClauseListNode) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int): String {
        val builder = StringBuilder()
        builder.append(indentedString(isArg, indent, "or:"))
        if (clauses.clauses.isNotEmpty()) {
            builder.append('\n')
        }
        builder.append(clauses.toCode(true, indent + 2))
        return builder.toString()
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(OrSection(
            clauses = clauses.transform(chalkTransformer) as ClauseListNode
        ))
    }
}

fun validateOrSection(node: Phase1Node): Validation<OrSection> {
    return validateClauseList(
        node,
        "or"
    ) { OrSection(it) }
}
