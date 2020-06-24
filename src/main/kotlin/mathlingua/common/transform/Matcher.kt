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

package mathlingua.common.transform

import mathlingua.common.ValidationFailure
import mathlingua.common.ValidationSuccess
import mathlingua.common.chalktalk.phase2.ast.clause.IdStatement
import mathlingua.common.textalk.*
import kotlin.math.max

internal fun OperatorTexTalkNode.signature() =
    when (this.command) {
        is Command -> this.command.signature()
        is TextTexTalkNode -> this.command.text
        else -> throw RuntimeException("Cannot get a signature of an " +
            "operator with command ${this.command.toCode()}")
    }

internal fun Command.signature(): String {
    val builder = StringBuilder()
    builder.append('\\')
    for (i in this.parts.indices) {
        if (i > 0) {
            builder.append('.')
        }
        builder.append(this.parts[i].signature())
    }
    return builder.toString()
}

fun IdStatement.signature() =
    when (this.texTalkRoot) {
        is ValidationFailure -> null
        is ValidationSuccess -> {
            val root = this.texTalkRoot.value
            if (root.children.size == 1 && root.children[0] is Command) {
                (root.children[0] as Command).signature()
            } else if (root.children.size == 1 && root.children[0] is OperatorTexTalkNode) {
                (root.children[0] as OperatorTexTalkNode).signature()
            } else {
                null
            }
        }
    }

data class Substitutions(
    val doesMatch: Boolean,
    val substitutions: Map<String, List<TexTalkNode>>,
    val errors: List<String>
)

internal fun getSubstitutions(pattern: OperatorTexTalkNode, value: OperatorTexTalkNode): Substitutions {
    val subs = MutableSubstitutions(
        doesMatch = true,
        substitutions = mutableMapOf(),
        errors = mutableListOf()
    )

    if ((pattern.lhs == null) != (value.lhs == null)) {
        subs.doesMatch = false
        return subs.toImmutable()
    }

    if (pattern.lhs != null) {
        if (pattern.lhs !is TextTexTalkNode) {
            subs.doesMatch = false
            subs.errors.add("The left-hand-side of an infix operator " +
                "pattern must be an identifier but found ${pattern.lhs.toCode()}")
            return subs.toImmutable()
        }

        subs.substitutions[pattern.lhs.text] = mutableListOf(value.lhs!!)
    }

    if ((pattern.rhs == null) != (value.rhs == null)) {
        subs.doesMatch = false
        return subs.toImmutable()
    }

    if (pattern.rhs != null) {
        if (pattern.rhs !is TextTexTalkNode) {
            subs.doesMatch = false
            subs.errors.add("The right-hand-side of an infix operator " +
                "pattern must be an identifier but found ${pattern.rhs.toCode()}")
            return subs.toImmutable()
        }

        subs.substitutions[pattern.rhs.text] = mutableListOf(value.rhs!!)
    }

    if ((pattern.command is TextTexTalkNode) && (value.command is TextTexTalkNode)) {
        if (pattern.command.text != value.command.text) {
            subs.doesMatch = false
        }
        return subs.toImmutable()
    }

    if ((pattern.command is Command) && (value.command is Command)) {
        val cmdSubs = getSubstitutions(pattern.command, value.command)
        subs.errors.addAll(cmdSubs.errors)
        subs.doesMatch = subs.doesMatch && cmdSubs.doesMatch
        for ((name, values) in cmdSubs.substitutions) {
            subs.substitutions[name] = values.toMutableList()
        }
        return subs.toImmutable()
    }

    throw RuntimeException("Encountered a pattern or value operator with a " +
        "command that is an expression.  Pattern: ${pattern.toCode()}, Value: ${value.toCode()}")
}

private fun getSubstitutions(pattern: Command, value: Command): Substitutions {
    val errors = validatePattern(pattern)
    if (errors.isNotEmpty()) {
        return Substitutions(
                doesMatch = false,
                substitutions = emptyMap(),
                errors = errors
        )
    }

    val subs = MutableSubstitutions(
            doesMatch = true,
            substitutions = mutableMapOf(),
            errors = mutableListOf()
    )

    if (pattern.parts.size == value.parts.size) {
        for (i in pattern.parts.indices) {
            findSubstitutions(pattern.parts[i], value.parts[i], subs)
        }
    }

    return subs.toImmutable()
}

// if there is a Command -> String pattern match and 'cmd' is the command
// then the match is store in 'operatorPatternToExpansion' as
//   OperatorTexTalkNode(lhs = null, command = cmd, rhs = null) -> String
// That is Command patterns, are OperatorTexTalkNode pattern with a
// left-hand-side or right-hand-side argument.
internal fun expandAsWritten(
    node: TexTalkNode,
    operatorPatternToExpansion: Map<OperatorTexTalkNode, String>
): String {
    val sigToPatternExpansion = mutableMapOf<String, PatternExpansion>()
    for ((opPattern, expansion) in operatorPatternToExpansion) {
        sigToPatternExpansion[opPattern.signature()] = PatternExpansion(
            pattern = opPattern,
            expansion = expansion
        )
    }
    return expandAsWrittenImpl(node, sigToPatternExpansion)
}

private data class MutableSubstitutions(
    var doesMatch: Boolean,
    val substitutions: MutableMap<String, MutableList<TexTalkNode>>,
    val errors: MutableList<String>
) {
    fun toImmutable() = Substitutions(
        doesMatch = doesMatch,
        substitutions = substitutions,
        errors = errors
    )
}

private fun CommandPart.signature(): String {
    val builder = StringBuilder()
    builder.append(this.name.text)
    for (grp in this.namedGroups) {
        builder.append(':')
        builder.append(grp.name.text)
    }
    return builder.toString()
}

private fun findSubstitutions(pattern: GroupTexTalkNode?, value: GroupTexTalkNode?, subs: MutableSubstitutions) {
    if ((pattern == null) != (value == null)) {
        subs.doesMatch = false
        if (pattern == null) {
            subs.errors.add("Unexpected group '${value?.toCode()}'")
        } else {
            subs.errors.add("Unexpected a match for '${pattern.toCode()}'")
        }
        return
    }

    if ((pattern == null) || (value == null)) {
        return
    }

    // this assumes all of the parameters are TextTexTalkNode with only the last one possible variadic
    val paramNames = pattern.parameters.items.map { it.children[0] as TextTexTalkNode }.map { it.text }
    val isVariadic = pattern.parameters.items.isNotEmpty() && (pattern.parameters.items.last().children[0] as TextTexTalkNode).isVarArg

    val values = value.parameters.items
    if (isVariadic) {
        val numRequired = max(0, paramNames.size - 1)
        if (values.size < numRequired) {
            subs.doesMatch = false
            subs.errors.add("Expected at least $numRequired arguments but found ${values.size} for '${value.toCode()}'")
            return
        }

        for (i in 0 until numRequired) {
            val exps: MutableList<TexTalkNode> = mutableListOf(values[i])
            subs.substitutions[paramNames[i]] = exps
        }

        val varArgs = mutableListOf<TexTalkNode>()
        for (i in numRequired until values.size) {
            varArgs.add(values[i])
        }
        subs.substitutions[paramNames.last()] = varArgs
    } else {
        val numRequired = paramNames.size
        if (values.size != numRequired) {
            subs.doesMatch = false
            subs.errors.add("Expected exactly $numRequired arguments but found ${values.size} for '${value.toCode()}'")
            return
        }

        for (i in paramNames.indices) {
            val exps: MutableList<TexTalkNode> = mutableListOf(values[i])
            subs.substitutions[paramNames[i]] = exps
        }
    }
}

private fun findSubstitutions(pattern: CommandPart, value: CommandPart, subs: MutableSubstitutions) {
    if (pattern.name != value.name) {
        subs.doesMatch = false
        subs.errors.add("Name mismatch.  Expected ${pattern.name} but found ${value.name}")
        return
    }

    findSubstitutions(pattern.square, value.square, subs)
    findSubstitutions(pattern.subSup?.sub, value.subSup?.sub, subs)
    findSubstitutions(pattern.subSup?.sup, value.subSup?.sup, subs)

    val isLastVarArg = pattern.groups.isNotEmpty() && pattern.groups.last().isVarArg
    if (isLastVarArg) {
        if (value.groups.size >= pattern.groups.size) {
            for (i in pattern.groups.indices) {
                findSubstitutions(pattern.groups[i], value.groups[i], subs)
            }

            val variadicName = (pattern.groups.last().parameters.items[0].children[0] as TextTexTalkNode).text
            // this function assumes if there is a variadic group it is only the last group and that group
            // has a single parameter that is a TextTexTalk node that is not variadic itself
            for (i in pattern.groups.size until value.groups.size) {
                val items = value.groups[i].parameters.items
                if (items.size != 1) {
                    subs.doesMatch = false
                    subs.errors.add("A variadic group can only contain a single item but found ${items.size} for '${value.groups[i]}")
                    continue
                }

                if (!subs.substitutions.containsKey(variadicName)) {
                    subs.substitutions[variadicName] = mutableListOf()
                }

                if (items.size != 1) {
                    subs.errors.add("A variadic group can only contain a single item but found ${items.size} for '${value.groups[i].toCode()}'")
                    subs.doesMatch = false
                } else {
                    subs.substitutions[variadicName]!!.add(value.groups[i].parameters.items[0])
                }
            }
        } else {
            subs.doesMatch = false
            subs.errors.add("Expected at least ${pattern.groups.size} groups but found ${value.groups.size} for '${value.toCode()}'")
        }
    } else {
        if (value.groups.size == pattern.groups.size) {
            for (i in pattern.groups.indices) {
                findSubstitutions(pattern.groups[i], value.groups[i], subs)
            }
        } else {
            subs.doesMatch = false
            subs.errors.add("Expected exactly ${pattern.groups.size} groups but found ${value.groups.size} for '${value.toCode()}'")
        }
    }

    if (pattern.namedGroups.size == value.namedGroups.size) {
        for (i in pattern.namedGroups.indices) {
            val patternGrp = pattern.namedGroups[i]
            val valGrp = value.namedGroups[i]
            if (patternGrp.name != valGrp.name) {
                subs.doesMatch = false
                subs.errors.add("Mismatched named group: Expected ${patternGrp.name} groups but found ${valGrp.name} for '${value.toCode()}'")
            }
            findSubstitutions(patternGrp.group, valGrp.group, subs)
        }
    } else {
        subs.doesMatch = false
        subs.errors.add("Expected exactly ${pattern.namedGroups.size} named groups but found ${value.namedGroups.size} for '${value.toCode()}'")
    }
}

private fun validatePatternGroupImpl(
    group: GroupTexTalkNode?,
    canBeVarArg: Boolean,
    description: String,
    errors: MutableList<String>
) {
    group ?: return

    if (group.isVarArg && !canBeVarArg) {
        errors.add("$description cannot be variadic")
    }

    if (group.isVarArg &&
            (group.parameters.items.size != 1 ||
                    (group.parameters.items[0].children.size != 1) ||
                    (group.parameters.items[0].children[0] !is TextTexTalkNode) ||
                    (group.parameters.items[0].children[0] as TextTexTalkNode).isVarArg)) {
        errors.add("A variadic group can only have a single identifier parameter that is not variadic")
        return
    }

    for (i in group.parameters.items.indices) {
        val expression = group.parameters.items[i]
        if (expression.children.size != 1) {
            errors.add("Cannot have a parameter with more than one value: '${expression.toCode()}'")
            continue
        }

        val item = expression.children[0]
        if (item !is TextTexTalkNode) {
            errors.add("Parameters can only be identifiers but found '${item.toCode()}'")
            continue
        }

        if (item.isVarArg && i != group.parameters.items.size - 1) {
            errors.add("Only the last parameter in a group can be variadic: '${item.toCode()}'")
        }
    }
}

private fun validatePatternImpl(part: CommandPart, errors: MutableList<String>) {
    validatePatternGroupImpl(part.square, false, "A square group", errors)
    validatePatternGroupImpl(part.subSup?.sub, false, "A ^ group", errors)
    validatePatternGroupImpl(part.subSup?.sup, false, "A _ group", errors)
    for (i in part.groups.indices) {
        val canBeVarArg = i == part.groups.size - 1
        val description = if (i == part.groups.size - 1) {
            "The last group"
        } else {
            "A group"
        }
        validatePatternGroupImpl(part.groups[i], canBeVarArg, description, errors)
    }
    for (i in part.namedGroups.indices) {
        validatePatternGroupImpl(part.namedGroups[i].group, false, "A named group", errors)
    }
}

private fun validatePattern(command: Command): List<String> {
    val errors = mutableListOf<String>()
    for (part in command.parts) {
        validatePatternImpl(part, errors)
    }
    return errors
}

private data class PatternExpansion(val pattern: OperatorTexTalkNode, val expansion: String)

private fun expandAsWrittenImplImpl(cmd: Command, sigToPatternExpansion: Map<String, PatternExpansion>) =
    expandAsWrittenImplImpl(
        OperatorTexTalkNode(
        lhs = null,
        command = cmd,
        rhs = null
    ), sigToPatternExpansion)

private fun expandAsWrittenImplImpl(op: OperatorTexTalkNode, sigToPatternExpansion: Map<String, PatternExpansion>): String? {
    val patternExpansion = sigToPatternExpansion[op.signature()] ?: return null

    val subs = getSubstitutions(patternExpansion.pattern, op)
    if (!subs.doesMatch) {
        return null
    }

    var expansion = patternExpansion.expansion
    for ((name, exp) in subs.substitutions) {
        val prefixRegex = Regex("$name\\{(.*)\\.\\.\\.\\}\\?")
        val infixRegex = Regex("$name\\{(.*)\\.\\.\\.(.*)\\.\\.\\.(.*)\\}\\?")
        val suffixRegex = Regex("$name\\{\\.\\.\\.(.*)\\}\\?")

        if (infixRegex.matches(expansion)) {
            val args = exp.map { expandAsWrittenImpl(it, sigToPatternExpansion) }
            val result = infixRegex.find(expansion)
            if (result != null && result.groupValues.size >= 4) {
                val prefix = result.groupValues[1]
                val separator = result.groupValues[2]
                val suffix = result.groupValues[3]
                val joinedArgs = args.joinToString(separator)
                val pattern = result.groupValues[0]
                // pattern is of the form name{prefix...separator...suffix}?
                // Note: if 'name{...a...b}? is given, for example, then
                //       'prefix' above will be the empty string
                //       'separator' will be 'a' and
                //       'suffix' will be 'b'
                expansion = expansion.replace(pattern, joinedArgs)
                expansion = "$prefix$expansion$suffix"
            }
        } else if (prefixRegex.matches(expansion)) {
            val args = exp.map { expandAsWrittenImpl(it, sigToPatternExpansion) }
            val result = prefixRegex.find(expansion)
            if (result != null && result.groupValues.size >= 2) {
                val separator = result.groupValues[1]
                val joinedArgsBuilder = StringBuilder()
                for (a in args) {
                    joinedArgsBuilder.append(separator)
                    joinedArgsBuilder.append(a)
                }
                val joinedArgs = joinedArgsBuilder.toString()
                val pattern = result.groupValues[0]
                // pattern is of the form name{separator...}?
                expansion = expansion.replace(pattern, joinedArgs)
            }
        } else if (suffixRegex.matches(expansion)) {
            val args = exp.map { expandAsWrittenImpl(it, sigToPatternExpansion) }
            val result = suffixRegex.find(expansion)
            if (result != null && result.groupValues.size >= 2) {
                val separator = result.groupValues[1]
                val joinedArgsBuilder = StringBuilder()
                for (a in args) {
                    joinedArgsBuilder.append(a)
                    joinedArgsBuilder.append(separator)
                }
                val joinedArgs = joinedArgsBuilder.toString()
                val pattern = result.groupValues[0]
                // pattern is of the form name{...separator}?
                expansion = expansion.replace(pattern, joinedArgs)
            }
        } else if (exp.isNotEmpty()) {
            val newText = expandAsWrittenImpl(exp.first(), sigToPatternExpansion)
            expansion = expansion.replace("$name?", newText)
        }
    }

    return expansion
}

private fun expandAsWrittenImpl(node: TexTalkNode, sigToPatternExpansion: Map<String, PatternExpansion>): String {
    return node.toCode {
        when (it) {
            is Command -> expandAsWrittenImplImpl(it, sigToPatternExpansion)
            is OperatorTexTalkNode -> expandAsWrittenImplImpl(it, sigToPatternExpansion)
            else -> null
        }
    }
}
