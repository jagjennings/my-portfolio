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

    int limit = Integer.parseInt(request.getParameter("limit"));
    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty(Comment.NAME_KEY);
      String commentBody = (String) entity.getProperty(Comment.COMMENT_KEY);
      long timestamp = (long) entity.getProperty(Comment.TIMESTAMP_KEY);
      String postTime = (String) entity.getProperty(Comment.POST_TIME_KEY);

      comments.add(new Comment(name, commentBody, timestamp, postTime));

      if (comments.size() == limit)
        break;
    }

    response.setContentType("application/json");
    String json = GSON.toJson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, "name-input", "");
    String comment = getParameter(request, "text-input", "");
    long timestamp = System.currentTimeMillis();

    Date date = new Date(timestamp);
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss");
    String postTime = dateFormat.format(date);

    Entity commentEntity = new Entity(Comment.COMMENT_KEY);
    commentEntity.setProperty(Comment.NAME_KEY, name);
    commentEntity.setProperty(Comment.COMMENT_KEY, comment);
    commentEntity.setProperty(Comment.TIMESTAMP_KEY, timestamp);
    commentEntity.setProperty(Comment.POST_TIME_KEY, postTime);

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
