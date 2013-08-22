package rinde.sim.pdptw.generator;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.nCopies;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.math.DoubleMath;

/**
 * The arrival times generated by this generator are the result of a Poisson
 * process, i.e. they have exponentially distributed inter-arrival times.
 * However, due to discretization and a strict control on the dynamism
 * properties, the resulting Poisson distribution has a slightly lower mean.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class PoissonProcessArrivalTimes implements ArrivalTimesGenerator {

  private final long length;
  private final double gai;
  private final double opa;

  // scenario length in minutes
  // intensity in announcements per hour
  public PoissonProcessArrivalTimes(long scenarioLength,
      double globalAnnouncementIntensity, double ordersPerAnnouncement) {
    length = scenarioLength;
    gai = globalAnnouncementIntensity;
    opa = ordersPerAnnouncement;
  }

  public ImmutableList<Long> generate(RandomGenerator rng) {
    // we model the announcements as a Poisson process, which means that
    // the interarrival times are exponentially distributed.
    final ExponentialDistribution ed = new ExponentialDistribution(1d / gai);
    ed.reseedRandomGenerator(rng.nextLong());
    long sum = 0;
    final List<Long> arrivalTimes = newArrayList();
    while (sum < length) {
      final long nt = DoubleMath
          .roundToLong(ed.sample() * 60d, RoundingMode.HALF_DOWN);

      // ignore values which are smaller than the time unit (one
      // minute), unless its the first value.
      if (nt > 0 || arrivalTimes.isEmpty()) {
        sum += nt;
        if (sum < length) {
          arrivalTimes.add(sum);
        } else if (arrivalTimes.isEmpty()) {
          // there is a small probability where the first
          // generated arrival time is greater than length. This
          // case is undesirable, when it happens, we just try
          // again by resetting sum.
          sum = 0;
        } else {
          break;
        }
      }
    }
    // now we know the real number of announcements.

    if (DoubleMath.isMathematicalInteger(opa)) {
      // if ordersPerAnnouncement is an integer, we can just use a
      // double for loop for setting the arrival times.
      final ImmutableList.Builder<Long> lb = ImmutableList.builder();
      for (final long arrivalTime : arrivalTimes) {
        for (int i = 0; i < opa; i++) {
          lb.add(arrivalTime);
        }
      }
      return lb.build();
    }
    // If it is not an integer, we need to use a randomized approach to
    // create a fair allocation of orders per announcement.
    // For example: if the opa value is 1.2, there will be announcements
    // with either 1 or 2 orders:
    // - 1 order [80%]
    // - 2 orders [20%]
    // The following code makes sure that this ratio is maintained as
    // far as possible.

    final int floor = DoubleMath.roundToInt(opa, RoundingMode.FLOOR);
    final int ceiling = DoubleMath.roundToInt(opa, RoundingMode.CEILING);
    final double ratio = opa - floor;

    final int floorTimes = DoubleMath.roundToInt((1 - ratio)
        * arrivalTimes.size(), RoundingMode.HALF_DOWN);
    final int ceilTimes = DoubleMath
        .roundToInt(ratio * arrivalTimes.size(), RoundingMode.HALF_UP);
    checkState(floorTimes + ceilTimes == arrivalTimes.size());

    final List<Integer> orderCountList = newArrayList();
    orderCountList.addAll(nCopies(floorTimes, floor));
    orderCountList.addAll(nCopies(ceilTimes, ceiling));
    Collections.shuffle(orderCountList, new RandomAdaptor(rng));

    final ImmutableList.Builder<Long> lb = ImmutableList.builder();
    for (int i = 0; i < arrivalTimes.size(); i++) {
      for (int j = 0; j < orderCountList.get(i); j++) {
        lb.add(arrivalTimes.get(i));
      }
    }
    return lb.build();
  }

  public long getScenarioLength() {
    return length;
  }

  public double getOrdersPerAnnouncement() {
    return opa;
  }

  public double getGlobalAnnouncementIntensity() {
    return gai;
  }
}
