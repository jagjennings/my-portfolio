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

package com.google.sps.data;

/** An item on a comment list. */
public final class Comment {
  private final String name;
  private final String comment;
  private final long timestamp;
  private final String postTime;
  private final String sentimentScore;
  public static final String NAME_KEY = "name";
  public static final String COMMENT_KEY = "comment";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String POST_TIME_KEY = "postTime";
  public static final String SENTIMENT_SCORE_KEY = "sentimentScore";

  public Comment(
      String name, String comment, long timestamp, String postTime, String sentimentScore) {
    this.name = name;
    this.comment = comment;
    this.timestamp = timestamp;
    this.postTime = postTime;
    this.sentimentScore = sentimentScore;
  }
}
