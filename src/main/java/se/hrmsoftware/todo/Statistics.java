package se.hrmsoftware.todo;

public class Statistics {
	private final long totalCreated;
	private final long totalCompleted;

	public Statistics(long totalCreated, long totalCompleted) {
		this.totalCreated = totalCreated;
		this.totalCompleted = totalCompleted;
	}

	public long getTotalCompleted() {
		return totalCompleted;
	}

	public long getTotalCreated() {
		return totalCreated;
	}
}
