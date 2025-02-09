/*
 * Copyright 2024 Dominic Kramer
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

package phase4

import "mathlingua/internal/ast"

type ArgumentDataKind interface {
	Node
	ArgumentDataKind()
}

func (*Group) ArgumentDataKind()                    {}
func (*TextArgumentData) ArgumentDataKind()         {}
func (*FormulationArgumentData) ArgumentDataKind()  {}
func (*ArgumentTextArgumentData) ArgumentDataKind() {}

////////////////////////////////////////////////////////////////////////////////////////////////////

type TopLevelNodeKind interface {
	Node
	TopLevelNodeKind()
	Start() ast.Position
}

func (*TextBlock) TopLevelNodeKind() {}
func (*Group) TopLevelNodeKind()     {}
