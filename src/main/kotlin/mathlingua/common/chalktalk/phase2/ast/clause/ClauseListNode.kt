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

package mathlingua.common.chalktalk.phase2.ast.clause

import mathlingua.common.chalktalk.phase2.CodeWriter
import mathlingua.common.chalktalk.phase2.ast.Phase2Node

data class ClauseListNode(val clauses: List<Clause>) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        clauses.forEach(fn)
    }

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter): CodeWriter {
        for (i in clauses.indices) {
            writer.append(clauses[i], true, indent)
            if (i != clauses.size - 1) {
                writer.writeNewline()
            }
        }
        return writer
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node): Phase2Node {
        return chalkTransformer(ClauseListNode(
                clauses = clauses.map { it.transform(chalkTransformer) as Clause }
        ))
    }
}
