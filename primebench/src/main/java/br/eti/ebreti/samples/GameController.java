/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.eti.ebreti.samples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.MeterGaugeChartModel;

/**
 * <p>
 * {@link GameController} contains all the business logic for the application, and also
 * serves as the controller for the JSF view.
 * </p>
 * <p>
 * It contains properties for the <code>number</code> to be guessed, the current
 * <code>guess</code>, the <code>smallest</code> and <code>biggest</code>
 * numbers guessed so far (as this is a higher/lower game we can prevent them
 * entering numbers that they should know are wrong), and the number of
 * <code>remainingGuesses</code>.
 * </p>
 * <p>
 * The {@link #check()} method, and {@link #reset()} methods provide the
 * business logic whilst the
 * {@link #validateNumberRange(FacesContext, UIComponent, Object)} method
 * provides feedback to the user.
 * </p>
 *
 * @author Pete Muir
 * @contributor fmarianoc
 *
 */
@Named
@SessionScoped
public class GameController implements Serializable {

	private static final long serialVersionUID = -4922390286153874072L;

	@Inject
	private User user;

	/**
	 * Max number of tries.
	 */
	private int limite;

	/**
	 * The game state.
	 */
	private GameState estado;

	/**
	 * Scores.
	 */
	private int pontos;
	private int pontosNestaPartida;
	private int escolhas;
	private int partidas;
	private int vitorias;

	/**
	 * Timestamp.
	 */
	private Calendar t1;

	/**
	 * The number that the user needs to guess.
	 */
	private int number;

	/**
	 * The users latest guess.
	 */
	private int guess;

	/**
	 * The smallest number guessed so far (so we can track the valid guess
	 * range).
	 */
	private int smallest;

	/**
	 * The largest number guessed so far.
	 */
	private int biggest;

	/**
	 * The number of guesses remaining.
	 */
	private int remainingGuesses;

	/**
	 * The maximum number we should ask them to guess.
	 */
	@Inject
	@MaxNumber
	private int maxNumber;

	/**
	 * The random number to guess.
	 */
	@Inject
	@Random
	Instance<Integer> randomNumber;

	private MeterGaugeChartModel gaugeModel;

    private ChartSeries pontosPorPartida;
    private ChartSeries performance;

	public GameController() {
		this.pontos = 0;
		this.escolhas = 0;
		this.partidas = 0;
		this.vitorias = 0;
		this.pontosPorPartida = new ChartSeries();
        this.pontosPorPartida.setLabel("Pontos");
        this.performance = new ChartSeries();
        this.performance.setLabel("Desempenho");
	}

	/**
	 * Check whether the current guess is correct, and update the
	 * biggest/smallest guesses as needed.
	 * Give feedback to the user if they are correct.
	 */
	public void check() {
		Calendar tempo = Calendar.getInstance();
		long delta = tempo.getTimeInMillis() - this.t1.getTimeInMillis();
		this.pontos += premiaRapidez(delta); 
		++this.escolhas;
		if (guess == number) {
			FacesContext.getCurrentInstance().addMessage(null, getWinnerMessage());
			++this.vitorias;
			this.pontos += premiaPrecisao();
			this.terminaPartida();
			--this.remainingGuesses;
			return;
		}
		--this.remainingGuesses;
		if (this.remainingGuesses < 1) {
			FacesMessage finalMessage = new FacesMessage("Que pena, acabaram suas chances... Tente de novo!");
			FacesContext.getCurrentInstance().addMessage(null, finalMessage);
			this.terminaPartida();
			return;
		}
		if (this.guess > this.number) {
			this.biggest = this.guess - 1;
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Escolha um número menor!"));
		} else {
			this.smallest = this.guess + 1;
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Escolha um número maior!"));
		}
		this.t1 = Calendar.getInstance();
	}

	private long premiaRapidez(long delta) {
		if (delta <= 3500L) {
			return (long) ((5000L - delta) / 40);
		} else if (delta <= 6500L) {
			return (long)((800L + (7000L - delta) * .4) / 40);
		}
		return (long)(((delta % 1000L) * .8) / 40);
	}

	private long premiaPrecisao() {
		return (this.remainingGuesses + 1) * (this.biggest - this.smallest + 1) * 10;
	}

	private FacesMessage getWinnerMessage() {
		FacesMessage winnerMessage;
		if (this.remainingGuesses == this.limite) {
			winnerMessage = new FacesMessage("Uau! De prima! Consegue repetir?");
			this.pontos += 2000;
		} else {
			this.pontos += 100 * (10 - this.remainingGuesses);
			if (this.remainingGuesses == 1) {
				winnerMessage = new FacesMessage("Por pouco! Acertou na última. Treine mais...");
			} else if (this.remainingGuesses < this.limite / 3) {
				winnerMessage = new FacesMessage("Ufa! Acertou nas últimas!");
			} else if (this.remainingGuesses > 2 * (this.limite / 3)) {
				winnerMessage = new FacesMessage("Boa! Está pegando a manha...");
				this.pontos += 300;
			} else {
				winnerMessage = new FacesMessage("Parabéns! Você acertou na "
						+ (this.limite + 1 - this.remainingGuesses) + "ª tentativa.");
			}
		}
		return winnerMessage;
	}

	/**
	 * Reset the game, by putting all values back to their defaults, and getting
	 * a new random number. We also call this method when the user starts
	 * playing for the first time using {@linkplain PostConstruct
	 * @PostConstruct} to set the initial values.
	 */
	@PostConstruct
	public void reset() {
		if (user.getEstado() == UserState.CONHECIDO) {
			this.limite = user.getLevel().getLevel();
		} else {
			this.limite = LevelEnum.NORMAL.getLevel();
		}
		this.estado = GameState.PRONTO;
		this.smallest = 0;
		this.guess = 0;
		this.remainingGuesses = this.limite;
		this.biggest = maxNumber;
		this.number = randomNumber.get();
		++this.partidas;
		this.t1 = Calendar.getInstance();
		this.pontosNestaPartida = -this.pontos;
	}

	public void terminaPartida() {
		this.estado = GameState.TERMINADO;
		this.pontosNestaPartida += this.pontos;
		Number pontosDeEscolhasNestaPartida = (double) (1000 * this.remainingGuesses / this.limite);
        this.pontosPorPartida.set(String.valueOf(this.partidas), this.pontosNestaPartida);
        this.performance.set(String.valueOf(this.partidas), pontosDeEscolhasNestaPartida);
	}

	/**
	 * A JSF validation method which checks whether the guess is valid. It might
	 * not be valid because there are no guesses left, or because the guess is
	 * not in range.
	 * 
	 */
	public void validateNumberRange(FacesContext context,
			UIComponent toValidate, Object value) {
		int input = (Integer) value;

		if (input < this.smallest || input > this.biggest) {
			((UIInput) toValidate).setValid(false);

			FacesMessage message = new FacesMessage("Não serve. Tente de novo.");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

	public GameState getEstado() {
		return estado;
	}

	public int getLimite() {
		return limite;
	}

	public int getPontos() {
		return pontos;
	}

	public int getEscolhas() {
		return escolhas;
	}

	public int getPartidas() {
		return partidas;
	}

	public int getVitorias() {
		return vitorias;
	}

	public Calendar getT1() {
		return t1;
	}

	public int getNumber() {
		return number;
	}

	public int getGuess() {
		return guess;
	}

	public void setGuess(int guess) {
		this.guess = guess;
	}

	public int getSmallest() {
		return smallest;
	}

	public int getBiggest() {
		return biggest;
	}

	public int getRemainingGuesses() {
		return remainingGuesses;
	}

	public String getMediaFormatada() {
		if (this.escolhas == 0) {
			return "comece";
		} else {
			Double media = (double) (this.pontos / this.escolhas);
			return String.valueOf(media.intValue());
		}
	}

	public Number getDesempenho() {
		if (this.escolhas == 0) {
			return 1000;
		} else {
			return (double) (1000 * ((this.limite * this.partidas) - this.escolhas + this.vitorias) / (this.partidas * this.limite));
		}
	}

	@SuppressWarnings("serial")
	public MeterGaugeChartModel getGaugeModel() {
        List<Number> intervals = new ArrayList<Number>(){{
            add(ColorPattern.LOW.getLimit());
            add(ColorPattern.MEDIUM.getLimit());
            add(ColorPattern.HIGH.getLimit());
        }};
        List<Number> ticks = new ArrayList<Number>(){{
            add(0);
            add(200);
            add(400);
            add(600);
            add(800);
            add(1000);
        }};
        this.gaugeModel = new MeterGaugeChartModel(this.getDesempenho(), intervals, ticks);
		return gaugeModel;
	}

	public CartesianChartModel getChartModel() {
	    CartesianChartModel chartModel = new CartesianChartModel();
        chartModel.addSeries(this.performance);
        chartModel.addSeries(this.pontosPorPartida);
		return chartModel;
	}

	public String getGaugeRainbow() {
		return ColorPattern.LOW.getGaugeColor() + ", " +
				ColorPattern.MEDIUM.getGaugeColor() + ", " +
				ColorPattern.HIGH.getGaugeColor();
	}

	public ColorPattern getStickColors() {
		if(this.getDesempenho().intValue() <= ColorPattern.LOW.getLimit()) {
			return ColorPattern.LOW;
		} else if(this.getDesempenho().intValue() <= ColorPattern.MEDIUM.getLimit()) {
			return ColorPattern.MEDIUM;
		} else {
			return ColorPattern.HIGH;
		}
	}

}
