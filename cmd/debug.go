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

package cmd

import (
	"errors"
	"fmt"
	"mathlingua/internal/ast"
	"mathlingua/internal/frontend"
	"mathlingua/internal/frontend/phase1"
	"mathlingua/internal/frontend/phase2"
	"mathlingua/internal/frontend/phase3"
	"mathlingua/internal/frontend/phase4"
	"mathlingua/internal/frontend/phase5"
	"mathlingua/internal/mlglib"
	"os"
	"path"

	"github.com/gdamore/tcell/v2"
	"github.com/rivo/tview"
	"github.com/spf13/cobra"
)

var debugCommand = &cobra.Command{
	Use:    "debug",
	Hidden: true,
	Run: func(cmd *cobra.Command, args []string) {
		setupScreen()
	},
}

func init() {
	rootCmd.AddCommand(debugCommand)
}

func setupScreen() {
	inputFile := path.Join("testbed", "input.txt")
	testcaseFile := path.Join("testbed", "testcases.txt")

	app := tview.NewApplication()

	outputArea := tview.NewTextView()
	outputArea.SetTitle("Output").SetBorder(true)

	helpInfo := tview.NewTextView().
		SetText(" Press Ctrl-R to run, Ctrl-T to write a test case, and Ctrl-C to exit")
	errorMessage := tview.NewTextView().SetTextAlign(tview.AlignRight)

	startText := ""
	if _, err := os.Stat(inputFile); !errors.Is(err, os.ErrNotExist) {
		bytes, err := os.ReadFile(inputFile)
		if err != nil {
			errorMessage.SetText(err.Error())
			app.Stop()
		}
		startText = string(bytes)
	}

	inputArea := tview.NewTextArea()
	inputArea.SetTitle("Input").SetBorder(true)
	inputArea.SetText(startText, false)

	mainView := tview.NewGrid().
		SetRows(0, 1).
		AddItem(inputArea, 0, 0, 1, 1, 0, 0, true).
		AddItem(outputArea, 0, 1, 1, 1, 0, 0, false).
		AddItem(helpInfo, 1, 0, 1, 1, 0, 0, false).
		AddItem(errorMessage, 1, 1, 1, 1, 0, 0, false)

	app.SetInputCapture(func(event *tcell.EventKey) *tcell.EventKey {
		errorMessage.SetText("")
		if err := os.WriteFile(inputFile, []byte(inputArea.GetText()), 0644); err != nil {
			errorMessage.SetText(err.Error())
		}

		if event.Key() == tcell.KeyCtrlR || event.Key() == tcell.KeyCtrlT {
			outputArea.SetText("")

			doc, tracker := parse(inputArea.GetText())
			diagnostics := tracker.Diagnostics()
			if len(diagnostics) > 0 {
				output := ""
				for _, diag := range diagnostics {
					output += fmt.Sprintf("%s (%d, %d): %s\n",
						diag.Type,
						(diag.Position.Row + 1),
						(diag.Position.Column + 1),
						diag.Message)
				}
				outputArea.SetText(output)
			} else {
				outputArea.SetText(mlglib.PrettyPrint(doc))
			}
		}

		if event.Key() == tcell.KeyCtrlT {
			testcase, message, ok := createTestCase(inputArea.GetText())
			errorMessage.SetText(message)
			if ok {
				if err := os.WriteFile(testcaseFile, []byte(testcase), 0644); err != nil {
					errorMessage.SetText(err.Error())
				}
			}
		}

		return event
	})

	if err := app.SetRoot(mainView,
		true).EnableMouse(true).Run(); err != nil {
		panic(err)
	}
}

func parse(text string) (ast.Document, frontend.DiagnosticTracker) {
	tracker := frontend.NewDiagnosticTracker()

	lexer1 := phase1.NewLexer(text, tracker)
	lexer2 := phase2.NewLexer(lexer1, tracker)
	lexer3 := phase3.NewLexer(lexer2, tracker)

	root := phase4.Parse(lexer3, tracker)
	doc, _ := phase5.Parse(root, tracker)

	return doc, tracker
}

// The output is: (testCase, errorMessage, successOrFailure)
// errorMessage is blank if it is a success and a string if it is a failure
func createTestCase(input string) (string, string, bool) {
	doc, tracker := parse(input)
	diagnostics := tracker.Diagnostics()
	if len(diagnostics) > 0 {
		return "", "Diagnostics exist: Cannot create test case", false
	}
	expectedOutput := mlglib.PrettyPrint(doc)

	testcase := fmt.Sprintf(`
func parse(text string) (ast.Document, frontend.DiagnosticTracker) {
	tracker := frontend.NewDiagnosticTracker()

	lexer1 := phase1.NewLexer(text, tracker)
	lexer2 := phase2.NewLexer(lexer1, tracker)
	lexer3 := phase3.NewLexer(lexer2, tracker)

	root := phase4.Parse(lexer3, tracker)
	doc, _ := phase5.Parse(root, tracker)

	return doc, tracker
}

func runTest(t *testing.T, input string, expected string) {
	doc, tracker := parse(input)
	actual := mlglib.PrettyPrint(doc)

	assert.Equal(t, expected, actual)
	assert.Equal(t, 0, len(tracker.Diagnostics()))
}

func Test_____(t *testing.T) {
	input := ` + "`" + input + "`" + `
	expected := ` + "`" + expectedOutput + "`" + `
	runTest(t, input, expected)
}

`)
	return testcase, "Successfully generated test case", true
}
