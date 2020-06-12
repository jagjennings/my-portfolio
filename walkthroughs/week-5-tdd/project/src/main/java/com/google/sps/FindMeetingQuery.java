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
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> timeRanges = new ArrayList<>();
    ArrayList<TimeRange> badTimeRanges = new ArrayList<>();
    ArrayList<TimeRange> badTimeRangesByEndTime = new ArrayList<>();

    ArrayList<String> attendees = new ArrayList<String>(request.getAttendees());
    long duration = request.getDuration();

    if (attendees.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    } 
    
    for (Event event: events) {
      for (String attendee: attendees) {
        if (event.getAttendees().contains(attendee)) {
          badTimeRanges.add(event.getWhen());
        }
      }
    }

    int availableStartTime = TimeRange.START_OF_DAY;

    for (TimeRange timeRange: badTimeRanges) {
      if (timeRange.end() > availableStartTime) {
        if (timeRange.start() - availableStartTime >= duration) {
          timeRanges.add(TimeRange.fromStartEnd(availableStartTime, timeRange.start(), false));
        }

        availableStartTime = timeRange.end();
      }
    }

    if (TimeRange.END_OF_DAY - availableStartTime >= duration) {
      timeRanges.add(TimeRange.fromStartEnd(availableStartTime, TimeRange.END_OF_DAY, true));
    }

    for (int i = timeRanges.size() - 1; i >= 0; i--) {
      for (TimeRange badTimeRange: badTimeRanges) {
        if (timeRanges.get(i).overlaps(badTimeRange)) {
          timeRanges.remove(timeRanges.get(i));
        }
      }
    }

    return timeRanges;
  }
}
