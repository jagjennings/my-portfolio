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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final Gson GSON = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query =
        new Query(Comment.COMMENT_KEY).addSort(Comment.TIMESTAMP_KEY, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    UserService userService = UserServiceFactory.getUserService();
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    // Add entites from datastore to comments arraylist.
    int limit = Integer.parseInt(request.getParameter("limit"));
    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty(Comment.NAME_KEY);
      String commentBody = (String) entity.getProperty(Comment.COMMENT_KEY);
      long timestamp = (long) entity.getProperty(Comment.TIMESTAMP_KEY);
      String postTime = (String) entity.getProperty(Comment.POST_TIME_KEY);
      String sentimentScore = (String) entity.getProperty(Comment.SENTIMENT_SCORE_KEY);

      String language = request.getParameter("language");

      // Do the translation.
      Translation translation =
          translate.translate(commentBody, Translate.TranslateOption.targetLanguage(language));
      commentBody = translation.getTranslatedText();

      comments.add(new Comment(name, commentBody, timestamp, postTime, sentimentScore));

      if (comments.size() == limit)
        break;
    }

    response.setContentType("application/json");
    response.getWriter().println(GSON.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the comment form.
    String comment = getParameter(request, "text-input", "");
    long timestamp = System.currentTimeMillis();
    UserService userService = UserServiceFactory.getUserService();
    String name = userService.getCurrentUser().getEmail();

    Date date = new Date(timestamp);
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss");
    String postTime = dateFormat.format(date);

    Document doc =
        Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    String sentimentScore = String.valueOf(sentiment.getScore());
    languageService.close();

    // Create entity with the comment from the input form.
    Entity commentEntity = new Entity(Comment.COMMENT_KEY);
    commentEntity.setProperty(Comment.NAME_KEY, name);
    commentEntity.setProperty(Comment.COMMENT_KEY, comment);
    commentEntity.setProperty(Comment.TIMESTAMP_KEY, timestamp);
    commentEntity.setProperty(Comment.POST_TIME_KEY, postTime);
    commentEntity.setProperty(Comment.SENTIMENT_SCORE_KEY, sentimentScore);

    // Add the entity to the datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
