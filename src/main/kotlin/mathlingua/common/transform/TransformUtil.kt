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

package mathlingua.common.transform

import mathlingua.common.ValidationFailure
import mathlingua.common.ValidationSuccess
import mathlingua.common.chalktalk.phase2.Clause
import mathlingua.common.chalktalk.phase2.ClauseListNode
import mathlingua.common.chalktalk.phase2.DefinesGroup
import mathlingua.common.chalktalk.phase2.Document
import mathlingua.common.chalktalk.phase2.ForGroup
import mathlingua.common.chalktalk.phase2.ForSection
import mathlingua.common.chalktalk.phase2.Identifier
import mathlingua.common.chalktalk.phase2.IfGroup
import mathlingua.common.chalktalk.phase2.IfSection
import mathlingua.common.chalktalk.phase2.MeansSection
import mathlingua.common.chalktalk.phase2.Phase2Node
import mathlingua.common.chalktalk.phase2.RepresentsGroup
import mathlingua.common.chalktalk.phase2.Statement
import mathlingua.common.chalktalk.phase2.ThenSection
import mathlingua.common.chalktalk.phase2.WhereSection
import mathlingua.common.textalk.Command
import mathlingua.common.textalk.ExpressionTexTalkNode
import mathlingua.common.textalk.IsTexTalkNode
import mathlingua.common.textalk.ParametersTexTalkNode
import mathlingua.common.textalk.TexTalkNode
import mathlingua.common.textalk.TexTalkNodeType
import mathlingua.common.textalk.TextTexTalkNode
import mathlingua.common.textalk.getAncestry

fun moveInlineCommandsToIsNode(
    defs: List<DefinesGroup>,
    node: Phase2Node,
    shouldProcessChalk: (node: Phase2Node) -> Boolean,
    shouldProcessTex: (root: TexTalkNode, node: TexTalkNode) -> Boolean
): Phase2Node {
    val knownDefSigs = defs.map { it.signature }.filterNotNull().toSet()
    fun realShouldProcessTex(root: TexTalkNode, node: TexTalkNode): Boolean {
        if (!shouldProcessTex(root, node)) {
            return false
        }

        if (node is Command && !knownDefSigs.contains(getCommandSignature(node).toCode())) {
            return false
        }

        val parents = getAncestry(root, node)
        for (p in parents) {
            if (p is IsTexTalkNode) {
                return false
            }
        }

        return true
    }

    var seed = 0
    return node.transform {
        if (it is ClauseListNode) {
            val newClauses = mutableListOf<Clause>()
            for (c in it.clauses) {
                if (c is Statement) {
                    val transformed = moveStatementInlineCommandsToIsNode(
                        seed++,
                        c,
                        shouldProcessChalk,
                        ::realShouldProcessTex
                    )
                    newClauses.add(transformed)
                } else {
                    newClauses.add(c)
                }
            }
            ClauseListNode(
                clauses = newClauses
            )
        } else {
            it
        }
    }
}

fun moveStatementInlineCommandsToIsNode(
    seed: Int,
    stmt: Statement,
    shouldProcessChalk: (node: Phase2Node) -> Boolean,
    shouldProcessTex: (root: TexTalkNode, node: TexTalkNode) -> Boolean
): Clause {
    val validation = stmt.texTalkRoot
    if (validation is ValidationFailure) {
        return stmt
    }
    val root = (validation as ValidationSuccess).value

    if (!shouldProcessChalk(stmt)) {
        return stmt
    }

    fun shouldProcessTexNodes(root: TexTalkNode, node: TexTalkNode): Boolean {
        if (!shouldProcessTex(root, node)) {
            return false
        }

        return !getAncestry(root, node).any { it is IsTexTalkNode }
    }

    val commandsFound = findCommands(root)
    val cmdToReplacement = mutableMapOf<Command, String>()
    var count = seed
    for (cmd in commandsFound) {
        if (shouldProcessTex(root, cmd)) {
            cmdToReplacement[cmd] = "\$${count++}"
        }
    }

    val cmdsToProcess = cmdToReplacement.keys
    val newNode = replaceCommands(stmt, cmdToReplacement, shouldProcessChalk, ::shouldProcessTexNodes) as Clause

    if (commandsFound.isEmpty()) {
        return stmt
    }

    if (cmdsToProcess.isEmpty()) {
        return stmt
    }

    return ForGroup(
        forSection = ForSection(
            targets = cmdsToProcess.map { Identifier(name = cmdToReplacement[it]!!) }
        ),
        whereSection = WhereSection(
            clauses = ClauseListNode(
                clauses = commandsFound.map {
                    val isNode = IsTexTalkNode(
                        lhs = ParametersTexTalkNode(
                            items = listOf(
                                ExpressionTexTalkNode(
                                    children = listOf(
                                        TextTexTalkNode(
                                            type = TexTalkNodeType.Identifier,
                                            text = cmdToReplacement[it]!!
                                        )
                                    )
                                )
                            )
                        ),
                        rhs = ParametersTexTalkNode(
                            items = listOf(
                                ExpressionTexTalkNode(
                                    children = listOf(it)
                                )
                            )
                        )
                    )

                    Statement(
                        text = isNode.toCode(),
                        texTalkRoot = ValidationSuccess(
                            ExpressionTexTalkNode(
                                children = listOf(isNode)
                            )
                        )
                    )
                }
            )
        ),
        thenSection = ThenSection(
            clauses = ClauseListNode(
                clauses = listOf(newNode)
            )
        )
    )
}

fun replaceRepresents(
    node: Phase2Node,
    represents: List<RepresentsGroup>,
    filter: (node: Phase2Node) -> Boolean = { true }
): Phase2Node {
    val repMap = mutableMapOf<String, RepresentsGroup>()
    for (rep in represents) {
        val sig = rep.signature
        if (sig != null) {
            repMap[sig] = rep
        }
    }

    fun chalkTransformer(node: Phase2Node): Phase2Node {
        if (!filter(node)) {
            return node
        }

        if (node !is ClauseListNode) {
            return node
        }

        val newClauses = mutableListOf<Clause>()

        for (clause in node.clauses) {
            if (clause !is Statement) {
                newClauses.add(clause)
                continue
            }

            if (clause.texTalkRoot is ValidationFailure ||
                (clause.texTalkRoot as ValidationSuccess).value.children.size != 1 ||
                clause.texTalkRoot.value.children[0] !is Command
            ) {
                newClauses.add(clause)
                continue
            }

            val command = clause.texTalkRoot.value.children[0] as Command
            val sig = getCommandSignature(command).toCode()

            if (!repMap.containsKey(sig)) {
                return node
            }

            val rep = repMap[sig]!!
            val cmdVars = getVars(command)
            val defIndirectVars = getRepresentsIdVars(rep)

            val map = mutableMapOf<String, String>()
            for (i in cmdVars.indices) {
                map[defIndirectVars[i]] = cmdVars[i]
            }

            val ifThen = buildIfThen(rep)
            val res = if (ifThen.ifSection.clauses.clauses.isEmpty() &&
                ifThen.thenSection.clauses.clauses.size == 1) {
                ifThen.thenSection.clauses.clauses[0]
            } else {
                ifThen
            }

            newClauses.add(renameVars(res, map) as Clause)
        }

        return ClauseListNode(clauses = newClauses)
    }

    return node.transform(::chalkTransformer)
}

fun replaceIsNodes(
    node: Phase2Node,
    defs: List<DefinesGroup>,
    filter: (node: Phase2Node) -> Boolean = { true }
): Phase2Node {
    val defMap = mutableMapOf<String, DefinesGroup>()
    for (def in defs) {
        val sig = def.signature
        if (sig != null) {
            defMap[sig] = def
        }
    }

    fun chalkTransformer(node: Phase2Node): Phase2Node {
        if (!filter(node)) {
            return node
        }

        if (node !is Statement) {
            return node
        }

        if (node.texTalkRoot is ValidationFailure ||
            (node.texTalkRoot as ValidationSuccess).value.children.size != 1 ||
            node.texTalkRoot.value.children[0] !is IsTexTalkNode) {
            return node
        }

        val isNode = node.texTalkRoot.value.children[0] as IsTexTalkNode
        if (isNode.rhs.items.size != 1 ||
            isNode.rhs.items[0].children.size != 1 ||
            isNode.rhs.items[0].children[0] !is Command) {
            return node
        }

        val command = isNode.rhs.items[0].children[0] as Command
        val sig = getCommandSignature(command).toCode()

        if (!defMap.containsKey(sig)) {
            return node
        }

        val def = defMap[sig]!!
        val cmdVars = getVars(command)

        val defDirectVars = getDefinesDirectVars(def)
        val defIndirectVars = getDefinesIdVars(def)

        if (cmdVars.size != defIndirectVars.size) {
            return node
        }

        val map = mutableMapOf<String, String>()
        for (i in cmdVars.indices) {
            map[defIndirectVars[i]] = cmdVars[i]
        }

        val lhsVars = getVars(isNode.lhs)
        if (lhsVars.size != defDirectVars.size) {
            return node
        }

        for (i in lhsVars.indices) {
            map[defDirectVars[i]] = lhsVars[i]
        }

        val ifThen = buildIfThen(def)
        val res = if (ifThen.ifSection.clauses.clauses.isEmpty() &&
            ifThen.thenSection.clauses.clauses.size == 1) {
            ifThen.thenSection.clauses.clauses[0]
        } else {
            ifThen
        }

        return renameVars(res, map)
    }

    return node.transform(::chalkTransformer)
}

fun toCanonicalForm(def: DefinesGroup): DefinesGroup {
    return DefinesGroup(
        signature = def.signature,
        id = def.id,
        definesSection = def.definesSection,
        assumingSection = null,
        meansSection = MeansSection(
            clauses = ClauseListNode(
                clauses = listOf(buildIfThen(def))
            )
        ),
        aliasSection = def.aliasSection,
        metaDataSection = def.metaDataSection
    )
}

fun buildIfThen(def: DefinesGroup): IfGroup {
    return IfGroup(
        ifSection = IfSection(
            clauses = def.assumingSection?.clauses ?: ClauseListNode(emptyList())
        ),
        thenSection = ThenSection(
            clauses = def.meansSection.clauses
        )
    )
}

fun buildIfThen(rep: RepresentsGroup): IfGroup {
    return IfGroup(
        ifSection = IfSection(
            clauses = rep.assumingSection?.clauses ?: ClauseListNode(emptyList())
        ),
        thenSection = ThenSection(
            clauses = rep.thatSection.clauses
        )
    )
}

fun getDefinesDirectVars(def: DefinesGroup): List<String> {
    val vars = mutableListOf<String>()
    for (target in def.definesSection.targets) {
        vars.addAll(getVars(target))
    }
    return vars
}

fun getDefinesIdVars(def: DefinesGroup): List<String> {
    val vars = mutableListOf<String>()
    if (def.id.texTalkRoot is ValidationSuccess) {
        vars.addAll(getVars(def.id.texTalkRoot.value))
    }
    return vars
}

fun getRepresentsIdVars(rep: RepresentsGroup): List<String> {
    val vars = mutableListOf<String>()
    if (rep.id.texTalkRoot is ValidationSuccess) {
        vars.addAll(getVars(rep.id.texTalkRoot.value))
    }
    return vars
}

fun fullExpandOnce(doc: Document): Document {
    var transformed = separateIsStatements(doc)
    transformed = glueCommands(transformed)
    transformed = moveInlineCommandsToIsNode(doc.defines, transformed, { true }, { root, node -> true })
    transformed = replaceRepresents(transformed, doc.represents, { true })
    return replaceIsNodes(transformed, doc.defines, { true }) as Document
}

fun fullExpandComplete(doc: Document, maxSteps: Int = 10): Document {
    val snapshots = mutableSetOf<String>()

    var transformed = doc
    var previousCode = transformed.toCode(false, 0)
    snapshots.add(previousCode)

    for (i in 0 until maxSteps) {
        transformed = fullExpandOnce(transformed)
        val code = transformed.toCode(false, 0)
        if (snapshots.contains(code) || previousCode == code) {
            break
        }
        previousCode = code
        snapshots.add(previousCode)
    }

    return transformed
}
