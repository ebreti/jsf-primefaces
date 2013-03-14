package br.eti.ebreti.samples;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class BasicPPRController implements Serializable {

	private static final long serialVersionUID = 1663835395997218364L;

	private String milisecs;

	private String delta;

	public String updateMilisecs() {
		long actual = System.currentTimeMillis();
		long diff = 0L;
		try {
			diff = actual - Long.valueOf(this.milisecs);
		} catch (NullPointerException e) {
			diff = 0L;
		} finally {
			this.milisecs = String.valueOf(actual);
			try {
				this.delta = String.valueOf(Long.valueOf(this.delta) - diff);
			} catch (NumberFormatException e) {
				this.delta = String.valueOf(0L);
			}
		}
		return null;
	}

	public String getMilisecs() {
		return milisecs;
	}

	public void setMilisecs(String milisecs) {
		this.milisecs = milisecs;
	}

	public String getDelta() {
		return delta;
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}

}
