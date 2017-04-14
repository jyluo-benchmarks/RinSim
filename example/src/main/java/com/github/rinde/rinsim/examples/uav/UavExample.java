/*
 * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rinde.rinsim.examples.uav;

import javax.measure.unit.SI;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TimeModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;

/**
 * @author Hoang Tung Dinh
 */
public final class UavExample {

  static final int SPEED_UP = 8;
  static final Point MIN_POINT = new Point(0, 0);
  static final Point MAX_POINT = new Point(6000, 6000);
  static final double MAX_SPEED = 1000d;

  private UavExample() {}

  public static void main(String[] args) {

    final Simulator sim = Simulator.builder()
      .addModel(TimeModel.builder().withTickLength(500))
      .addModel(RoadModelBuilders.plane()
        .withCollisionAvoidance()
        .withObjectRadius(1000)
        .withMinPoint(MIN_POINT)
        .withMaxPoint(MAX_POINT)
        .withDistanceUnit(SI.METER)
        .withSpeedUnit(SI.METERS_PER_SECOND)
        .withMaxSpeed(MAX_SPEED))
      .addModel(View.builder()
        .with(PlaneRoadModelRenderer.builder())
        .with(UavRenderer.builder())
        .withSpeedUp(SPEED_UP))
      .build();

    sim.register(new UavAgent(new Point(0, 0), new Point(3000, 3000)));
    sim.register(new UavAgent(new Point(5000, 5000), new Point(3000, 3000)));
    sim.start();
  }
}
