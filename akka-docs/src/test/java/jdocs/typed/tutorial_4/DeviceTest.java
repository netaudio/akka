/*
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs.typed.tutorial_4;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static jdocs.typed.tutorial_4.DeviceProtocol.*;

public class DeviceTest extends JUnitSuite {

  @ClassRule
  public static final TestKitJunitResource testKit = new TestKitJunitResource();

  //#device-read-test
  @Test
  public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
    TestProbe<RespondTemperature> probe = testKit.createTestProbe(RespondTemperature.class);
    ActorRef<DeviceMessage> deviceActor = testKit.spawn(Device.createBehavior("group", "device"));
    deviceActor.tell(new ReadTemperature(42L, probe.getRef()));
    RespondTemperature response = probe.receiveOne();
    assertEquals(42L, response.requestId);
    assertEquals(Optional.empty(), response.value);
  }
  //#device-read-test

  //#device-write-read-test
  @Test
  public void testReplyWithLatestTemperatureReading() {
    TestProbe<TemperatureRecorded> recordProbe = testKit.createTestProbe(TemperatureRecorded.class);
    TestProbe<RespondTemperature> readProbe = testKit.createTestProbe(RespondTemperature.class);
    ActorRef<DeviceMessage> deviceActor = testKit.spawn(Device.createBehavior("group", "device"));

    deviceActor.tell(new RecordTemperature(1L, 24.0, recordProbe.getRef()));
    assertEquals(1L, recordProbe.receiveOne().requestId);

    deviceActor.tell(new ReadTemperature(2L, readProbe.getRef()));
    RespondTemperature response1 = readProbe.receiveOne();
    assertEquals(2L, response1.requestId);
    assertEquals(Optional.of(24.0), response1.value);

    deviceActor.tell(new RecordTemperature(3L, 55.0, recordProbe.getRef()));
    assertEquals(3L, recordProbe.receiveOne().requestId);

    deviceActor.tell(new ReadTemperature(4L, readProbe.getRef()));
    RespondTemperature response2 = readProbe.receiveOne();
    assertEquals(4L, response2.requestId);
    assertEquals(Optional.of(55.0), response2.value);
  }
  //#device-write-read-test

}
