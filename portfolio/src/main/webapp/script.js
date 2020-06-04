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
  const facts = [
    'I like to make wheel-thrown pottery.', 'I love watching Survivor.',
    'I am learning Mandarin.', 'I was born on Friday the 13th.', 'I am a Libra.'
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

function getComments() {
  document.getElementById('comments-container').innerHTML = '';
  var num = document.getElementById('num');
  num = num.options[num.selectedIndex].value;
  var url = '/data?limit=' + num;

  fetch(url).then(response => response.json()).then((comments) => {
    const commentsListElement = document.getElementById('comments-container');
    comments.forEach((comment) => {
      commentsListElement.appendChild(
          createListElement(comment.name, comment.comment));
    })
  });
}

function createListElement(name, comment) {
  const liElement = document.createElement('p');
  liElement.innerText = name + ': ' + comment;
  return liElement;
}
