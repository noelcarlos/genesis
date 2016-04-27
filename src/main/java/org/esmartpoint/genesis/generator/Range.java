package org.esmartpoint.genesis.generator;

public class Range{
    private Integer from;
    private Integer to;
    private Integer step;
    
    public Range() {
        this.setFrom(null);
        this.setTo(null);
        this.setStep(null);
    }
    
    public Range(Integer from, Integer to) {
        this.setFrom(from);
        this.setTo(to);
        this.setStep(null);
    }
    
    public Range(Integer from, Integer to, Integer step) {
        this.setFrom(from);
        this.setTo(to);
        this.setStep(step);
    }

	public Integer getFrom() {
		return from;
	}

	public void setFrom(Integer from) {
		this.from = from;
	}

	public Integer getTo() {
		return to;
	}

	public void setTo(Integer to) {
		this.to = to;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}
}
