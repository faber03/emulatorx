package link;

import data.util.PacketGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

public class STLink extends Link {
	private static final Logger log = LoggerFactory.getLogger(STLink.class);

	public STLink(long id, float length, int ffs, int speedlimit, int frc, int netclass, int fow, String routenumber,
				  String areaname, String name, String geom, int intervallo, String startDateTime) {
		super(id, length, ffs, speedlimit, frc, netclass, fow, routenumber, areaname, name, geom, intervallo, startDateTime);
	}

	public synchronized void updateAggregateTotalVehiclesTravelTime(LocalDateTime receivedDateTime, float sampleSpeed, float coverage) throws InterruptedException {
		log.info("Updating aggregated packet in ST solution...");
		this.currentDateTime = receivedDateTime;
		numVehicles++;
		assert stats != null;
		double v = ((coverage*length*FACTOR_M2KM)/sampleSpeed)*FACTORH_2SEC;
		stats.addValue(v);
	}

	public synchronized String getAggregateTotalVehiclesTravelTime() {
		String aggregateVehiclesTravelTime = null;
		if(numVehicles > 0) {
			log.info("Creating the packet and resetting the counters...");
			log.info("Number of vehicles transited is {}", numVehicles);
			double avgTravelTime = stats.getMean();
			double sdTravelTime = stats.getStandardDeviation();
			Duration d = Duration.between(startDateTime, endDateTime);
			aggregateVehiclesTravelTime = PacketGenerator.aggregateVehiclesTravelTimeSample(getId(), avgTravelTime, sdTravelTime, numVehicles,
					d, startDateTime, endDateTime);
			resetAggregateTotalVehiclesTravelTime();
		}
		/*long diff = Math.abs(Duration.between(currentDate,finalDate).toMinutes());
		int mul = (int) (diff/intervallo);
		log.info("The multiplier is {}", mul);
		mul++;*/
		updateFinalDate(1);
		return aggregateVehiclesTravelTime;
	}
}