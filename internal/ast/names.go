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

package ast

const LowerEquivalentlyName = "equivalently"
const LowerAllOfName = "allOf"
const LowerAnyOfName = "anyOf"
const LowerAsName = "as"
const LowerAsQuestionName = LowerAsName + "?"
const LowerByName = "by"
const LowerByQuestionName = LowerByName + "?"
const LowerCalledName = "called"
const LowerContentName = "content"
const LowerSatisfyingName = "satisfying"
const LowerSatisfyingQuestionName = LowerSatisfyingName + "?"
const LowerExpressingName = "expressing"
const LowerExpressingQuestionName = LowerExpressingName + "?"
const LowerElseName = "else"
const LowerElseQuestionName = LowerElseName + "?"
const LowerExistsName = "exists"
const LowerExistsUniqueName = "existsUnique"
const LowerExtendsName = "extends"
const LowerExtendsQuestionName = LowerExtendsName + "?"
const LowerForAllName = "forAll"
const LowerDeclareName = "declare"
const LowerGivenName = "given"
const LowerGivenQuestionName = LowerGivenName + "?"
const LowerUsingName = "using"
const LowerUsingQuestionName = LowerUsingName + "?"
const LowerIdName = "id"
const LowerIfName = "if"
const LowerIfQuestionName = LowerIfName + "?"
const LowerElseIfName = "elseIf"
const LowerElseIfQuestionName = LowerElseIfName + "?"
const LowerIffName = "iff"
const LowerIffQuestionName = LowerIffName + "?"
const LowerIsName = "is"
const LowerLabelName = "label"
const LowerMeansName = "means"
const LowerMeansQuestionName = LowerMeansName + "?"
const LowerEquivalentToName = "equivalentTo"
const LowerEquivalentToQuestionName = LowerEquivalentToName + "?"
const LowerNegativeFloatName = "negativeFloat"
const LowerNegativeIntName = "negativeInt"
const LowerNotName = "not"
const LowerOfName = "of"
const LowerOneOfName = "oneOf"
const LowerOverviewName = "overview"
const LowerPiecewiseName = "piecewise"
const LowerPositiveFloatName = "positiveFloat"
const LowerPositiveIntName = "positiveInt"
const LowerRelatedName = "related"
const LowerStatesName = "states"
const LowerStatesQuestion = "states"
const LowerStatesQuestionName = LowerStatesName + "?"
const LowerSuchThatName = "suchThat"
const LowerSuchThatQuestionName = LowerSuchThatName + "?"
const LowerThatName = "that"
const LowerThenName = "then"
const LowerThenQuestionName = LowerThenName + "?"
const LowerThroughName = "through"
const LowerViaName = "via"
const LowerViewName = "view"
const LowerEncodingName = "encoding"
const LowerWhenName = "when"
const LowerWhenQuestionName = LowerWhenName + "?"
const LowerWhereName = "where"
const LowerWhereQuestionName = LowerWhereName + "?"
const LowerZeroName = "zero"
const UpperCapturesName = "Captures"
const UpperAxiomName = "Axiom"
const UpperConjectureName = "Conjecture"
const UpperDefinesName = "Defines"
const UpperDescribesName = "Describes"
const UpperDocumentedName = "Documented"
const UpperDocumentedQuestionName = UpperDocumentedName + "?"
const UpperJustifiedName = "Justified"
const UpperJustifiedQuestionName = UpperJustifiedName + "?"
const UpperNoteName = "Note"
const UpperProofName = "Proof"
const UpperProofQuestionName = UpperProofName + "?"
const UpperProvidesName = "Provides"
const UpperProvidesQuestionName = UpperProvidesName + "?"
const UpperReferencesName = "References"
const UpperReferencesQuestionName = UpperReferencesName + "?"
const UpperSpecifyName = "Specify"
const UpperStatesName = "States"
const UpperTheoremName = "Theorem"
const UpperViewableName = "Viewable"
const UpperViewableQuestionName = UpperViewableName + "?"
const LowerMembersName = "members"
const LowerMemberName = "member"
const LowerOperationsName = "operations"
const LowerOperationName = "operation"
const LowerSpecifyName = "specify"
const UpperAliasesName = "Aliases"
const UpperAliasesQuestionName = UpperAliasesName + "?"
const LowerIntoName = "into"
const LowerAliasesName = "aliases"
const LowerOnName = "on"
const LowerOnQuestionName = LowerOnName + "?"
const LowerTitleName = "title"
const LowerAuthorName = "author"
const LowerOffsetName = "offset"
const LowerUrlName = "url"
const LowerHomepageName = "homepage"
const LowerTypeName = "type"
const LowerEditionName = "edition"
const LowerEditorName = "editor"
const LowerInstitutionName = "institution"
const LowerJournalName = "journal"
const LowerPublisherName = "publisher"
const LowerVolumeName = "volume"
const LowerMonthName = "month"
const LowerYearName = "year"
const LowerDescriptionName = "description"
const LowerSymbolName = "symbol"
const LowerWrittenName = "written"
const LowerWrittenQuestionName = LowerWrittenName + "?"
const LowerWritingName = "writing"
const LowerWritingQuestionName = LowerWritingName + "?"
const LowerSignifiesName = "signifies"
const LowerSignifiesQuestionName = LowerSignifiesName + "?"
const LowerViewableName = "viewable"
const LowerViewableQuestionName = LowerViewableName + "?"
const LowerThroughQuestionName = LowerThroughName + "?"
const LowerNameName = "name"
const LowerBiographyName = "biography"
const UpperPersonName = "Person"
const UpperResourceName = "Resource"
const UpperIdName = "Id"
const UpperIdQuestionName = UpperIdName + "?"
const UpperCorollaryName = "Corollary"
const LowerToName = "to"
const UpperLemmaName = "Lemma"
const LowerForName = "for"
const LowerBecauseName = "because"
const LowerBecauseQuestionName = LowerBecauseName + "?"
const LowerStepwiseName = "stepwise"
const LowerSupposeName = "suppose"
const LowerBlockName = "block"
const LowerCasewiseName = "casewise"
const LowerCaseName = "case"
const LowerWithoutLossOfGeneralityName = "withoutLossOfGenerality"
const LowerContradictionName = "contradiction"
const LowerForContradictionName = "forContradiction"
const LowerForInductionName = "forInduction"
const LowerForContrapositiveName = "forContrapositive"
const LowerClaimName = "claim"
const LowerThusName = "thus"
const LowerThereforeName = "therefore"
const LowerHenceName = "hence"
const LowerNoticeName = "notice"
const LowerNextName = "next"
const LowerRemarkName = "remark"
const LowerPartwiseName = "partwise"
const LowerPartName = "part"
const LowerAbsurdName = "absurd"
const LowerDoneName = "done"
const LowerQedName = "qed"
const LowerToShowName = "toShow"
const LowerSufficesToShowName = "sufficesToShow"
const LowerObserveName = "observe"
const LowerReplacesName = "replaces"
const LowerReplacesQuestionName = LowerReplacesName + "?"
const LowerInductivelyName = "inductively"
const LowerMatchingName = "matching"
const LowerAgainstName = "against"
