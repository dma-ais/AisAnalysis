package dk.dma.ais.analysis.coverage.data.json;

public class JsonTimeSpan {

	//from time, to time, data time, time since last timespan, accumulated time, signals, distinct ships
	public long fromTime, toTime;
	public int spanLength, timeSinceLastSpan, accumulatedTime, signals, distinctShips;
}
