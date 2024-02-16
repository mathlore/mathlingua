/*
 * Copyright 2022 Dominic Kramer
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

package ast

type TokenType string

const (
	Name                  TokenType = "Name"
	Colon                 TokenType = "Colon"
	Text                  TokenType = "Text"
	FormulationTokenType  TokenType = "FormulationTokenType"
	TextBlock             TokenType = "TextBlock"
	Indent                TokenType = "Indent"
	UnIndent              TokenType = "Unindent"
	SameIndent            TokenType = "SameIndent"
	DotSpace              TokenType = "DotSpace"
	LineBreak             TokenType = "LineBreak"
	Id                    TokenType = "Id"
	Newline               TokenType = "Newline"
	ArgumentText          TokenType = "ArgumentText"
	Comma                 TokenType = "Comma"
	Space                 TokenType = "Space"
	BeginGroup            TokenType = "BeginGroup"
	EndGroup              TokenType = "EndGroup"
	BeginSection          TokenType = "BeginSection"
	EndSection            TokenType = "EndSection"
	BeginDotSpaceArgument TokenType = "BeginDotSpaceArgument"
	EndDotSpaceArgument   TokenType = "EndDotSpaceArgument"
	BeginInlineArgument   TokenType = "BeginInlineArgument"
	EndInlineArgument     TokenType = "EndInlineArgument"
	Operator              TokenType = "Operator"
	LParen                TokenType = "LParen"
	RParen                TokenType = "RParen"
	LSquare               TokenType = "LSquare"
	RSquare               TokenType = "RSquare"
	LCurly                TokenType = "LCurly"
	RCurly                TokenType = "RCurly"
	Underscore            TokenType = "Underscore"
	Bar                   TokenType = "Bar"
	Dot                   TokenType = "Dot"
	Slash                 TokenType = "Slash"
	BackSlash             TokenType = "BackSlash"
	Semicolon             TokenType = "Semicolon"
	ColonEquals           TokenType = "ColonEquals"
	ColonDashArrow        TokenType = "ColonDashArrow"
	ColonArrow            TokenType = "ColonArrow"
	DotDotDot             TokenType = "DotDotDot"
	QuestionMark          TokenType = "QuestionMark"
	Is                    TokenType = "Is"
	Satisfies             TokenType = "Satisfies"
	Extends               TokenType = "Extends"
	As                    TokenType = "As"
	At                    TokenType = "At"
	BarRightDashArrow     TokenType = "BarRightDashArrow"
	ParenLabel            TokenType = "ParenLabel"
	LParenColon           TokenType = "LParenColon"
	ColonRParen           TokenType = "ColonRParen"
	LCurlyColon           TokenType = "LCurlyColon"
	ColonRCurly           TokenType = "ColonRCurly"
	BacktickLParen        TokenType = "BacktickLParen"
	RParenBacktick        TokenType = "RParenBacktick"
	BacktickLSquare       TokenType = "BacktickLSquare"
	RSquareBacktick       TokenType = "RSquareBacktick"
	BacktickLCurly        TokenType = "BacktickLCurly"
	RCurlyBacktick        TokenType = "RCurlyBacktick"
)

type Token struct {
	Type     TokenType
	Text     string
	Position Position
}

type Position struct {
	Offset int
	Row    int
	Column int
}
