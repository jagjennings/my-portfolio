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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public final class FindMeetingQuery {

  /**
  * Handles optional attendees and determines who to include in the meeting request
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();

    // Return all times if there are no attendees
    if (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    } 

    ArrayList<String> mandatoryAttendees = new ArrayList<String>(request.getAttendees());
    ArrayList<String> allAttendees = new ArrayList<String>(mandatoryAttendees);
    allAttendees.addAll(request.getOptionalAttendees());

    ArrayList<TimeRange> timeRanges = new ArrayList<TimeRange>(findAvailableTimeRanges(allAttendees, events, duration));

    // If no time ranges are available, exclude optional attendees and try again
    if (!timeRanges.isEmpty()) {
      return timeRanges;
    } else {
      timeRanges = new ArrayList<TimeRange>(findAvailableTimeRanges(mandatoryAttendees, events, duration));
    }

    // If the request has no attendees after optional attendees are excluded, return an empty collection.
    if (!mandatoryAttendees.isEmpty()) {
      return timeRanges;
    } else {
      return new ArrayList<TimeRange>();
    }
  }

  /**
  * Finds a collection of time ranges available for all of the specified attendees
  */
  public Collection<TimeRange> findAvailableTimeRanges(ArrayList<String> attendees, Collection<Event> events, long duration) {
    ArrayList<TimeRange> timeRanges = new ArrayList<>();
    ArrayList<TimeRange> badTimeRanges = new ArrayList<>();

    // Make a list of all unavailable time ranges for all attendees
    for (Event event: events) {
      for (String attendee: attendees) {
        if (event.getAttendees().contains(attendee)) {
          badTimeRanges.add(event.getWhen());
        }
      }
    }

    if (!badTimeRanges.isEmpty()) {
      Collections.sort(badTimeRanges, TimeRange.ORDER_BY_START);
    }

    int availableStartTime = TimeRange.START_OF_DAY;

    // Store all time ranges between unavailable time ranges that fit the duration of the meeting
    for (TimeRange timeRange: badTimeRanges) {
      if (timeRange.end() > availableStartTime) {
        if (timeRange.start() - availableStartTime >= duration) {
          timeRanges.add(TimeRange.fromStartEnd(availableStartTime, timeRange.start(), false));
        }

        availableStartTime = timeRange.end();
      }
    }

    // Include time from the end of the last unavailable time range to the end of the day
    if (TimeRange.END_OF_DAY - availableStartTime >= duration) {
      timeRanges.add(TimeRange.fromStartEnd(availableStartTime, TimeRange.END_OF_DAY, true));
    }

    return timeRanges;
  }
}
