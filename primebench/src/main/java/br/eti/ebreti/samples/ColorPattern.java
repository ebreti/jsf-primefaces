package br.eti.ebreti.samples;

public enum ColorPattern {
	LOW(250, "FF6347", "FF6347, BBC6D0"),
	MEDIUM(750, "7CFC00", "7CFC00, BBC6D0"),
	HIGH(1000, "AFEEEE", "AFEEEE, BBC6D0");

	private ColorPattern(int limit, String gaugeColor, String stackedColors) {
		this.limit = limit;
		this.gaugeColor = gaugeColor;
		this.stackedColors = stackedColors;
	}

	private final int limit;
	private final String gaugeColor;
	private final String stackedColors;

	public int getLimit() {
		return limit;
	}

	public String getGaugeColor() {
		return gaugeColor;
	}

	public String getStackedColors() {
		return stackedColors;
	}

}
