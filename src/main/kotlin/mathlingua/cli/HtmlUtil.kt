/*
 * Copyright 2022 The MathLingua Authors
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

package mathlingua.cli

internal const val SHARED_HEADER =
    """
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <meta content="text/html;charset=utf-8" http-equiv="Content-Type">
    <meta content="utf-8" http-equiv="encoding">
    <meta name="description" content="A codex of mathematical knowledge">
    <meta name="keywords" content="math, maths, mathematics, knowledge, database, repository, codex, encyclopedia">
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/katex@0.11.1/dist/katex.min.css"
          integrity="sha384-zB1R0rpPzHqg7Kpt0Aljp8JPLqbXI3bhnPWROx27a9N0Ll6ZP/+DiW/UqRcLbRjq"
          crossorigin="anonymous">
    <script defer
            src="https://cdn.jsdelivr.net/npm/katex@0.11.1/dist/katex.min.js"
            integrity="sha384-y23I5Q6l+B6vatafAwxRu/0oK/79VlbSz7Q9aiSZUvyWYIYsd+qj+o24G5ZU2zJz"
            crossorigin="anonymous">
    </script>
    <script defer
            src="https://cdnjs.cloudflare.com/ajax/libs/mark.js/8.11.1/mark.min.js">
    </script>
"""

internal const val KATEX_RENDERING_JS =
    """
    function buildMathFragment(rawText) {
        let text = rawText;
        if (text[0] === '"') {
            text = text.substring(1);
        }
        if (text[text.length - 1] === '"') {
            text = text.substring(0, text.length - 1);
        }
        text = text.replace(/([a-zA-Z0-9])\?\??/g, '${'$'}1');
        const fragment = document.createDocumentFragment();
        let buffer = '';
        let i = 0;
        while (i < text.length) {
            if (text[i] === '${'$'}' && text[i+1] === '${'$'}' && text[i+2] === '${'$'}') {
                i += 3; // skip over the ${'$'}s
                fragment.appendChild(document.createTextNode(buffer));
                buffer = '';

                const span = document.createElement('span');
                let math = '';
                while (i < text.length &&
                    !(text[i] === '${'$'}' && text[i+1] === '${'$'}' && text[i+2] === '${'$'}')) {
                    math += text[i++];
                }
                if (text[i] === '${'$'}') {
                    i++; // move past the ${'$'}
                }
                if (text[i] === '${'$'}') {
                    i++; // move past the second ${'$'}
                }
                if (text[i] === '${'$'}') {
                    i++; // move past the third ${'$'}
                }
                try {
                    katex.render(math, span, {
                        throwOnError: true,
                        displayMode: false
                    });
                } catch {
                    span.appendChild(document.createTextNode(math));
                }
                fragment.appendChild(span);
            } else if (text[i] === '\\' && text[i+1] === '[') {
                i += 2; // skip over \ and [
                fragment.appendChild(document.createTextNode(buffer));
                buffer = '';

                const span = document.createElement('span');
                span.className = 'display-mode';
                let math = '';
                while (i < text.length &&
                    !(text[i] === '\\' && text[i+1] === ']')) {
                    math += text[i++];
                }
                if (text[i] === '\\') {
                    i++; // move past the \
                }
                if (text[i] === ']') {
                    i++; // move past the ]
                }
                try {
                    katex.render(math, span, {
                        throwOnError: true,
                        displayMode: true
                    });
                } catch {
                    span.appendChild(document.createTextNode(math));
                }
                fragment.appendChild(span);
            } else if (text[i] === '\\' && text[i+1] === '(') {
                i += 2; // skip over \ and ()
                fragment.appendChild(document.createTextNode(buffer));
                buffer = '';

                const span = document.createElement('span');
                let math = '';
                while (i < text.length &&
                    !(text[i] === '\\' && text[i+1] === ')')) {
                    math += text[i++];
                }
                if (text[i] === '\\') {
                    i++; // move past the \
                }
                if (text[i] === ')') {
                    i++; // move past the )
                }
                try {
                    katex.render(math, span, {
                        throwOnError: true,
                        displayMode: false
                    });
                } catch {
                    span.appendChild(document.createTextNode(math));
                }
                fragment.appendChild(span);
            } else if (text[i] === '${'$'}' && text[i+1] === '${'$'}') {
                i += 2; // skip over ${'$'} and ${'$'}
                fragment.appendChild(document.createTextNode(buffer));
                buffer = '';

                const span = document.createElement('span');
                span.className = 'display-mode';
                let math = '';
                while (i < text.length &&
                    !(text[i] === '${'$'}' && text[i+1] === '${'$'}')) {
                    math += text[i++];
                }
                if (text[i] === '${'$'}') {
                    i++; // move past the ${'$'}
                }
                if (text[i] === '${'$'}') {
                    i++; // move past the ${'$'}
                }
                try {
                    katex.render(math, span, {
                        throwOnError: true,
                        displayMode: true
                    });
                } catch {
                    span.appendChild(document.createTextNode(math));
                }
                fragment.appendChild(span);
            } else if (text[i] === '${'$'}') {
                i++; // skip over the ${'$'}
                fragment.appendChild(document.createTextNode(buffer));
                buffer = '';

                const span = document.createElement('span');
                let math = '';
                while (i < text.length &&
                     text[i] !== '${'$'}') {
                    math += text[i++];
                }
                if (text[i] === '${'$'}') {
                    i++; // move past the ${'$'}
                }
                try {
                    katex.render(math, span, {
                        throwOnError: true,
                        displayMode: false
                    });
                } catch {
                    span.appendChild(document.createTextNode(math));
                }
                fragment.appendChild(span);
            } else {
                buffer += text[i++];
            }
        }

        if (buffer.length > 0) {
            fragment.appendChild(document.createTextNode(buffer));
        }

        return fragment;
    }

    function render(node) {
        if (node.className && node.className.indexOf && node.className.indexOf('no-render') >= 0) {
            return;
        }

        let isInWritten = false;
        const parent = node.parentNode;
        if (node.className === 'mathlingua') {
            for (let i=0; i<node.childNodes.length; i++) {
                const n = node.childNodes[i];
                if (n && n.className === 'mathlingua-header' &&
                    n.textContent === 'written:') {
                    isInWritten = true;
                    break;
                }
            }
        }

        for (let i = 0; i < node.childNodes.length; i++) {
            const child = node.childNodes[i];

            // node is an element node => nodeType === 1
            // node is an attribute node => nodeType === 2
            // node is a text node => nodeType === 3
            // node is a comment node => nodeType === 8
            if (child.nodeType === 3) {
                let text = child.textContent;
                if (text.trim()) {
                    if (isInWritten) {
                        // if the text is in a written: section
                        // turn "some text" to ${'$'}${'$'}${'$'}some text${'$'}${'$'}${'$'}
                        // so the text is in math mode
                        if (text[0] === '"') {
                            text = text.substring(1);
                        }
                        if (text[text.length - 1] === '"') {
                            text = text.substring(0, text.length - 1);
                        }
                        text = '${'$'}${'$'}${'$'}' + text + '${'$'}${'$'}${'$'}';
                    }
                    const fragment = buildMathFragment(text);
                    i += fragment.childNodes.length - 1;
                    node.replaceChild(fragment, child);
                }
            } else if (child.nodeType === 1) {
                render(child);
            }
        }
    }
"""

internal const val INTERACTIVE_JS_CODE =
    """
    function flipEntity(id) {
        const renEl = document.getElementById('rendered-' + id);
        const litEl = document.getElementById('literal-' + id);
        if (renEl && litEl) {
            if (renEl.className === 'mathlingua-rendered-visible') {
                renEl.className = 'mathlingua-rendered-hidden';
                litEl.className = 'mathlingua-literal-visible';
            } else {
                renEl.className = 'mathlingua-rendered-visible';
                litEl.className = 'mathlingua-literal-hidden';
            }
        }
    }

    function toggleProof(id) {
        const proofEl = document.getElementById('proof-' + id);
        const iconEl = document.getElementById('icon-' + id);
        if (proofEl) {
            if (proofEl.className === 'mathlingua-proof-hidden') {
                proofEl.className = 'mathlingua-proof-shown';
                if (iconEl) {
                    iconEl.innerHTML = '&#9652;';
                }
            } else {
                proofEl.className = 'mathlingua-proof-hidden';
                if (iconEl) {
                    iconEl.innerHTML = '&#9662;';
                }
            }
        }
    }
"""

internal const val SHARED_CSS =
    """
    .content {
        padding-top: 1.5em;
        padding-bottom: 1em;
        margin-top: 2.5em;
        margin-bottom: 1em;
        margin-left: auto; /* for centering content */
        margin-right: auto; /* for centering content */
        font-size: 1em;
        width: 50em;
        background-color: white;
        border: solid;
        border-width: 1px;
        border-color: rgba(210, 210, 210);
        border-radius: 2px;
        box-shadow: rgba(0, 0, 0, 0.1) 0px 3px 10px,
                    inset 0  0 0 0 rgba(240, 240, 240, 0.5);
    }

    @media screen and (max-width: 500px) {
        .content {
            width: 99%;
            margin-top: 2.5vh;
            margin-bottom: 0;
        }
    }

    body {
        background-color: #f0f0f3;
        padding-bottom: 1em;
    }

    hr {
        border: 0.5px solid #efefef;
    }

    h1, h2, h3, h4 {
        color: #0055bb;
        text-align: center;
    }

    p {
        text-align: left;
        text-indent: 0;
    }

    .mathlingua-flip-icon {
        position: sticky;
        top: 1em;
        left: 100%;
        border: none;
        color: #aaaaaa;
        background: #ffffff;
        margin: 0;
        padding: 0;
        padding-right: 2em;
        font-size: 110%;
    }

    .mathlingua-rendered-visible {
        display: block;
    }

    .mathlingua-rendered-hidden {
        display: none;
    }

    .mathlingua-literal-visible {
        display: block;
    }

    .mathlingua-literal-hidden {
        display: none;
    }

    .mathlingua-home {
        width: 80%;
        display: block;
        margin-left: auto;
        margin-right: auto;
    }

    .mathlingua-dir-item-hidden {
        display: none;
    }

    .mathlingua-dir-item-shown {
        display: block;
    }

    .mathlingua-home-item {
        font-weight: bold;
        display: block;
        margin-top: -1.25ex;
        margin-bottom: -1ex;
    }

    .mathlingua-list-dir-item {
        font-weight: bold;
        display: block;
        margin-top: -0.5ex;
        margin-bottom: -0.5ex;
    }

    .mathlingua-list-file-item {
        display: block;
        margin-top: -0.5ex;
        margin-bottom: -0.5ex;
    }

    .mathlingua-top-level {
        overflow-y: hidden;
        overflow-x: auto;
        background-color: white;
        border: solid;
        border-width: 1px;
        border-radius: 2px;
        padding-top: 2em;
        padding-bottom: 1em;
        padding-left: 2em;
        padding-right: 2em;
        max-width: 75%;
        width: max-content;
        margin-left: auto; /* for centering content */
        margin-right: auto; /* for centering content */
        margin-top: 2.25em;
        margin-bottom: 2.25em;
        border-color: rgba(230, 230, 230);
        border-radius: 2px;
        box-shadow: rgba(0, 0, 0, 0.1) 0px 3px 10px,
                    inset 0  0 0 0 rgba(240, 240, 240, 0.5);
    }

    .mathlingua-block-comment {
        font-family: Georgia, 'Times New Roman', Times, serif;
    }

    .mathlingua-block-comment-top-level {
        font-family: Georgia, 'Times New Roman', Times, serif;
        line-height: 1.3;
        text-align: left;
        text-indent: 0;
        background-color: #ffffff;
        max-width: 80%;
        width: 80%;
        margin-left: auto; /* for centering content */
        margin-right: auto; /* for centering content */
    }

    .end-mathlingua-top-level {
        padding-top: 0.5em;
        margin: 0;
    }

    .mathlingua-topic-group-id {
        display: block;
        padding: 0 0 1em 0;
        font-family: monospace;
        text-align: center;
        color: #5500aa;
    }

    .mathlingua-topic-name {
        display: block;
        padding: 0 0 0.2em 0;
        font-family: Georgia, 'Times New Roman', Times, serif;
        font-weight: bold;
        text-align: center;
        color: #0055bb;
    }

    .mathlingua-topic-content {
        display: block;
        padding: 0 0 1.2em 0;
        margin: 0.2em 0 -1.2em 0;
        font-family: Georgia, 'Times New Roman', Times, serif;
        line-height: 1.3;
        color: #000000;
    }

    .mathlingua-overview {
        display: block;
        padding: 0.5em 0 0.5em 0;
        font-family: Georgia, 'Times New Roman', Times, serif;
        line-height: 1.3;
        color: #000000;
        margin-top: -2ex;
        margin-bottom: -2ex;
    }

    .mathlingua-resources-header {
        display: block;
        color: #0055bb;
        text-shadow: 0px 1px 0px rgba(255,255,255,0), 0px 0.4px 0px rgba(0,0,0,0.2);
        font-family: Georgia, 'Times New Roman', Times, serif;
        padding-top: 0.25em;
    }

    .mathlingua-resources-item {
        display: block;
        font-family: Georgia, 'Times New Roman', Times, serif;
        padding: 0.1em 0 0.1em 0;
    }
    
    .mathlingua-topics-header {
        display: block;
        color: #0055bb;
        text-shadow: 0px 1px 0px rgba(255,255,255,0), 0px 0.4px 0px rgba(0,0,0,0.2);
        font-family: Georgia, 'Times New Roman', Times, serif;
        padding-top: 0.5em;
    }

    .mathlingua-topic-item {
        display: block;
        font-family: Georgia, 'Times New Roman', Times, serif;
        padding: 0.1em 0 0.1em 0;
    }

    .mathlingua-related-header {
        display: block;
        color: #0055bb;
        text-shadow: 0px 1px 0px rgba(255,255,255,0), 0px 0.4px 0px rgba(0,0,0,0.2);
        font-family: Georgia, 'Times New Roman', Times, serif;
        padding-top: 0.5em;
    }

    .mathlingua-related-item {
        display: block;
        font-family: Georgia, 'Times New Roman', Times, serif;
        padding: 0.1em 0 0.1em 0;
    }

    .mathlingua-foundation-header {
        display: block;
        font-family: Georgia, 'Times New Roman', Times, serif;
        text-align: center;
        font-weight: normal;
        padding-bottom: 1ex;
        color: #777777;
    }

    .mathlingua {
        font-family: monospace;
    }

    .mathlingua-proof-icon {
        float:right;
        color: #aaaaaa;
        cursor: default;
    }

    .mathlingua-proof-shown {
        display: block;
        margin-top: -0.6ex;
    }

    .mathlingua-proof-hidden {
        display: none;
        margin-top: -0.6ex;
    }

    .mathlingua-proof-header {
        display: block;
        color: #0055bb;
        text-shadow: 0px 1px 0px rgba(255,255,255,0), 0px 0.4px 0px rgba(0,0,0,0.2);
        font-family: Georgia, 'Times New Roman', Times, serif;
    }

    .mathlingua-header {
        color: #0055bb;
        text-shadow: 0px 1px 0px rgba(255,255,255,0), 0px 0.4px 0px rgba(0,0,0,0.2);
        font-family: Georgia, 'Times New Roman', Times, serif;
    }

    .mathlingua-header-literal {
        color: #0055bb;
        text-shadow: 0px 1px 0px rgba(255,255,255,0), 0px 0.4px 0px rgba(0,0,0,0.2);
        font-family: monospace;
    }

    .mathlingua-whitespace {
        padding: 0;
        margin: 0;
        margin-left: 1ex;
    }

    .mathlingua-id {
        color: #5500aa;
        overflow-x: scroll;
    }

    .mathlingua-text {
        color: #000000;
        display: inline-block;
        font-family: Georgia, 'Times New Roman', Times, serif;
        margin-top: -1.6ex;
        margin-bottom: -1.6ex;
    }

    .mathlingua-text-no-render {
        color: #000000;
        display: inline-block;
        font-family: Georgia, 'Times New Roman', Times, serif;
        margin-top: -1.6ex;
        margin-bottom: -1.6ex;
    }

    .literal-mathlingua-text {
        color: #386930;
        display: inline;
        font-family: monospace;
        line-height: 1.3;
    }

    .literal-mathlingua-text-no-render {
        color: #386930;
        display: inline;
        font-family: monospace;
        line-height: 1.3;
    }

    .mathlingua-url {
        color: #0000aa;
        text-decoration: none;
        display: inline-block;
        font-family: Georgia, 'Times New Roman', Times, serif;
    }

    .mathlingua-link {
        color: #0000aa;
        text-decoration: none;
        display: inline-block;
    }

    .mathlingua-statement-no-render {
        color: #007377;
    }

    .mathlingua-statement-container {
        display: inline;
    }

    .literal-mathlingua-statement {
        color: #007377;
    }

    .mathlingua-dropdown-menu-shown {
        position: absolute;
        display: block;
        z-index: 1;
        background-color: #ffffff;
        box-shadow: rgba(0, 0, 0, 0.5) 0px 3px 10px,
                    inset 0  0 10px 0 rgba(200, 200, 200, 0.25);
        border: solid;
        border-width: 1px;
        border-radius: 0px;
        border-color: rgba(200, 200, 200);
    }

    .mathlingua-dropdown-menu-hidden {
        display: none;
    }

    .mathlingua-dropdown-menu-item {
        display: block;
        margin: 0.75ex;
    }

    .mathlingua-dropdown-menu-item:hover {
        font-style: italic;
    }

    .mathlingua-called {
        font-weight: bold;
        display: table;
        margin-top: -1em;
        margin-left: auto;
        margin-bottom: -2em;
        margin-right: auto;
    }

    .display-mode {
        margin-left: auto;
        margin-right: auto;
        width: max-content;
        display: block;
        padding-top: 2ex;
        padding-bottom: 3ex;
    }

    .katex {
        font-size: 1em;
    }

    .katex-display {
        display: contents;
    }

    .katex-display > .katex {
        display: contents;
    }

    .katex-display > .katex > .katex-html {
        display: contents;
    }
"""

internal fun buildStandaloneHtml(content: String) =
    """
<!DOCTYPE html>
<html>
    <head>
        $SHARED_HEADER
        <script>
            $KATEX_RENDERING_JS

            $INTERACTIVE_JS_CODE

            function initPage() {
                const el = document.getElementById('__main_content__');
                if (el) {
                    render(el);
                }
            }
        </script>
        <style>
            $SHARED_CSS
        </style>
    </head>
    <body onload="initPage()">
        <div class="content" id="__main_content__">
            $content
        </div>
    </body>
"""

internal fun sanitizeHtmlForJs(html: String) =
    html.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")
