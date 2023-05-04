/*
 * Copyright 2023 Dominic Kramer
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

package backend

import (
	"mathlingua/internal/ast"
	"mathlingua/internal/frontend"
)

func PopulateScopes(node ast.MlgNodeKind, tracker frontend.IDiagnosticTracker) {
	switch n := node.(type) {
	case *ast.Document:
		populateDocumentScopes(n)
	}

	node.ForEach(func(subNode ast.MlgNodeKind) {
		PopulateScopes(subNode, tracker)
	})
}

////////////////////////////////////////////////////////////////////////////////////////////////////

func populateDocumentScopes(doc *ast.Document) {
	for _, item := range doc.Items {
		if specify, ok := item.(*ast.SpecifyGroup); ok {
			for _, specifyItem := range specify.Specify.Specify {
				if si, siOk := specifyItem.(*ast.ZeroGroup); siOk {
					//					si.Means.Means
					si = si
				}
			}
		}
	}
}

func getIdentifierInfos(clause ast.ClauseKind) []ast.IdentifierInfo {
	return []ast.IdentifierInfo{}
}
