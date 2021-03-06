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

import java.util.Collection;


public final class FindMeetingQuery {

  /**
  * Determines which attendees to include and returns time ranges that accomadate those attendees
  * If there are no mandatory attendees and no available times for optional attendees, an
  * empty list will be returned. If there are available times for both the optional and mandatory
  * attendees, those available times will be returned. If there are no available times to accomodate
  * both optional and mandatory attendees, only mandatory attendees will be considered.
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
    }

    // If the request has no attendees after optional attendees are excluded, return an empty collection.
    if (!mandatoryAttendees.isEmpty()) {
      return new ArrayList<TimeRange>(findAvailableTimeRanges(mandatoryAttendees, events, duration));
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

    Collections.sort(badTimeRanges, TimeRange.ORDER_BY_START);

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
