// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I like to make wheel-thrown pottery.', 'I love watching Survivor.', 'I am learning Mandarin.', 'I was born on Friday the 13th.', 'I am a Libra.'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

// function getDataUsingArrowFunctions() {
//   fetch('/data').then(response => response.json()).then((data) => {
//     document.getElementById('data-container').innerText = data;
//   });
// }

function getComments() {
  fetch('/data').then(response => response.json()).then((comment) => {
    document.getElementById('comment').innerText = comment;
  });
}

function getData() {
  fetch('/data').then(response => response.json()).then((data) => {
    document.getElementById('data-container').innerText = data;
  });
}
